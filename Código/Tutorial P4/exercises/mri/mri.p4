/* -*- P4_16 -*- */
#include <core.p4>
#include <v1model.p4>

const bit<8>  UDP_PROTOCOL = 0x11;
const bit<16> TYPE_IPV4 = 0x800;
const bit<5>  IPV4_OPTION_MRI = 31;

#define MAX_HOPS 9

/*************************************************************************
*********************** H E A D E R S  ***********************************
*************************************************************************/

typedef bit<9>  egressSpec_t;
typedef bit<48> macAddr_t;
typedef bit<32> ip4Addr_t;
typedef bit<32> switchID_t;
typedef bit<32> qdepth_t;

header ethernet_t {
    macAddr_t dstAddr;
    macAddr_t srcAddr;
    bit<16>   etherType;
}

header ipv4_t {
    bit<4>    version;
    bit<4>    ihl;
    bit<8>    diffserv;
    bit<16>   totalLen;
    bit<16>   identification;
    bit<3>    flags;
    bit<13>   fragOffset;
    bit<8>    ttl;
    bit<8>    protocol;
    bit<16>   hdrChecksum;
    ip4Addr_t srcAddr;
    ip4Addr_t dstAddr;
}

header ipv4_option_t {
    bit<1> copyFlag;
    bit<2> optClass;
    bit<5> option;
    bit<8> optionLength;
}

header mri_t {
    bit<16>  count;							/*tipo de header no pacote com so 1 valor, nº de switch pelos quais passámos*/
}

header switch_t {							/*tipo de header no pacote com 2 valores: id so switch pelo qual passamos e a queue de entrada que este tinha quando fomos processados*/
    switchID_t  swid;
    qdepth_t    qdepth;
}

struct ingress_metadata_t {
    bit<16>  count;
}

struct parser_metadata_t {
    bit<16>  remaining;
}

struct metadata {
    ingress_metadata_t   ingress_metadata;
    parser_metadata_t   parser_metadata;
}

struct headers {
    ethernet_t         ethernet;
    ipv4_t             ipv4;
    ipv4_option_t      ipv4_option;
    mri_t              mri;					/*valor do nº de switch extraido do pacote*/
    switch_t[MAX_HOPS] swtraces;			/*teremos de o processsar de forma recurssiva, este array extraido*/
}

error { IPHeaderTooShort }

/*************************************************************************
*********************** P A R S E R  ***********************************
*************************************************************************/

parser MyParser(packet_in packet,
                out headers hdr,
                inout metadata meta,
                inout standard_metadata_t standard_metadata) {

    state start {
        transition parse_ethernet;
    }

    state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.etherType) {
            TYPE_IPV4: parse_ipv4;
            default: accept;
        }
    }

    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        verify(hdr.ipv4.ihl >= 5, error.IPHeaderTooShort);
        transition select(hdr.ipv4.ihl) {
            5             : accept;
            default       : parse_ipv4_option;
        }
    }

    state parse_ipv4_option {
        /*
        * TODO: Add logic to:
        * - Extract the ipv4_option header.
        *   - If value is equal to IPV4_OPTION_MRI, transition to parse_mri.
        *   - Otherwise, accept.
        */
		packet.extract(hdr.ipv4_option);
		transition select(hdr.ipv4_option.option) {				/*.option porque queremos ver o tipo em si*/
            IPV4_OPTION_MRI: parse_mri;
            default: accept;
        }
    }

    state parse_mri {											/*extrair o numero de switchs*/
        /*
        * TODO: Add logic to:
        * - Extract hdr.mri.
        * - Set meta.parser_metadata.remaining to hdr.mri.count
        * - Select on the value of meta.parser_metadata.remaining
        *   - If the value is equal to 0, accept.
        *   - Otherwise, transition to parse_swtrace.
        */
        packet.extract(hdr.mri);
		meta.parser_metadata.remaining = hdr.mri.count;			/*saber quantos switchs pelos quais passámos*/
		transition select(meta.parser_metadata.remaining) {		/*.option porque queremos ver o tipo em si*/
            0 : accept;											/*se n ha nenhum header com id,queue, n precisamos de extrair*/
            default: parse_swtrace;	
        }
    }

    state parse_swtrace {										/*extrair de forma recurssica quais switchs passámos e as suas queues*/
        /*
        * TODO: Add logic to:
        * - Extract hdr.swtraces.next.
        * - Decrement meta.parser_metadata.remaining by 1
        * - Select on the value of meta.parser_metadata.remaining
        *   - If the value is equal to 0, accept.
        *   - Otherwise, transition to parse_swtrace.			
        */														//não há loops, entao improvissamos
																//CORREÇÃO: o packet.extract, retorna valores do header que seja o proximo na fila a extrair, A ORDEM IMPORTA
		packet.extract(hdr.swtraces.next);						//com .next ele extrai hdr.swtraces atual e faz store no seguinte slot do array swtraces na nossa estrutura hdr
		meta.parser_metadata.remaining = meta.parser_metadata.remaining - 1;
		transition select(meta.parser_metadata.remaining) {		//.option porque queremos ver o tipo em si
            0 : accept;
            default: parse_swtrace;
        }
    }
}


/*************************************************************************
************   C H E C K S U M    V E R I F I C A T I O N   *************
*************************************************************************/

control MyVerifyChecksum(inout headers hdr, inout metadata meta) {
    apply {  }
}


/*************************************************************************
**************  I N G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyIngress(inout headers hdr,
                  inout metadata meta,
                  inout standard_metadata_t standard_metadata) {
    action drop() {
        mark_to_drop(standard_metadata);
    }

    action ipv4_forward(macAddr_t dstAddr, egressSpec_t port) {
        standard_metadata.egress_spec = port;
        hdr.ethernet.srcAddr = hdr.ethernet.dstAddr;
        hdr.ethernet.dstAddr = dstAddr;
        hdr.ipv4.ttl = hdr.ipv4.ttl - 1;
    }

    table ipv4_lpm {
        key = {
            hdr.ipv4.dstAddr: lpm;
        }
        actions = {
            ipv4_forward;
            drop;
            NoAction;
        }
        size = 1024;
        default_action = NoAction();
    }

    apply {
        if (hdr.ipv4.isValid()) {
            ipv4_lpm.apply();
        }
    }
}

/*************************************************************************
****************  E G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyEgress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {
    action add_swtrace(switchID_t swid) {							/*como o pacote chegou a nós(um switch), temos de nos adicionar à estatística*/
        /*
        * TODO: add logic to:
        - Increment hdr.mri.count by 1
        - Add a new swtrace header by calling push_front(1) on hdr.swtraces.
        - Set hdr.swtraces[0].swid to the id parameter
        - Set hdr.swtraces[0].qdepth to (qdepth_t)standard_metadata.deq_qdepth
        - Increment hdr.ipv4.ihl by 2
        - Increment hdr.ipv4.totalLen by 8
        - Increment hdr.ipv4_option.optionLength by 8
        */
		hdr.mri.count = hdr.mri.count + 1;
		hdr.swtraces.push_front(1);		//empurramos o array com as estruturas para nos colocarmos no topo
		hdr.swtraces[0].setValid();		//According to the P4_16 spec, pushed elements are invalid, so we need to call setValid(). Older bmv2 versions would mark the new header(s)	valid automatically (P4_14 behavior), but starting with version 1.11, bmv2 conforms with the P4_16 spec.
        hdr.swtraces[0].swid = swid;										//na nova estrutura metemos o nosso id, que ja esta registado na nossa tabela
        hdr.swtraces[0].qdepth = (qdepth_t)standard_metadata.deq_qdepth;	//adicinamos o comprimento da nossa fila
    
		hdr.ipv4.ihl = hdr.ipv4.ihl + 2;									//comprimento do header ipv4, em palavras de 32 bits, aqui 2 porque a estrut tem 2 variav de 32bits
        hdr.ipv4_option.optionLength = hdr.ipv4_option.optionLength + 8;	//comprimento das opcoes em bytes, (32bits = 4 bytes), as opcoes sao opcionais, entao usamos-las para o tracking da rede, como adicionamos 2 var de 32 bits, ajustamos
        hdr.ipv4.totalLen = hdr.ipv4.totalLen + 8;							//o tamanho total do pacote IPv4, incluindo o cabeçalho e os dados em bytes, por causa da estrutura nova
	}

    table swtrace {
        actions = {
            add_swtrace;
            NoAction;
        }

        default_action =  NoAction();
    }

    apply {
        /*
        * TODO: add logic to:
        * - If hdr.mri is valid:
        *   - Apply table swtrace
        */
        if (hdr.mri.isValid()) {		/*estamos a dizer que só fazemos algo aqui se header mri (o de tracking) estiver a ser usado*/
            swtrace.apply();
        }
    }
}

/*************************************************************************
*************   C H E C K S U M    C O M P U T A T I O N   **************
*************************************************************************/

control MyComputeChecksum(inout headers hdr, inout metadata meta) {
     apply {
        update_checksum(
            hdr.ipv4.isValid(),
            { hdr.ipv4.version,
              hdr.ipv4.ihl,
              hdr.ipv4.diffserv,
              hdr.ipv4.totalLen,
              hdr.ipv4.identification,
              hdr.ipv4.flags,
              hdr.ipv4.fragOffset,
              hdr.ipv4.ttl,
              hdr.ipv4.protocol,
              hdr.ipv4.srcAddr,
              hdr.ipv4.dstAddr },
            hdr.ipv4.hdrChecksum,
            HashAlgorithm.csum16);
    }
}

/*************************************************************************
***********************  D E P A R S E R  *******************************
*************************************************************************/

control MyDeparser(packet_out packet, in headers hdr) {
    apply {
		/* TODO: emit ipv4_option, mri and swtraces headers */
        packet.emit(hdr.ethernet);
        packet.emit(hdr.ipv4);
        packet.emit(hdr.ipv4_option);			/*só adicionámos estes 3 ultimos*/
        packet.emit(hdr.mri);
        packet.emit(hdr.swtraces);
    }
}

/*************************************************************************
***********************  S W I T C H  *******************************
*************************************************************************/

V1Switch(
MyParser(),
MyVerifyChecksum(),
MyIngress(),
MyEgress(),
MyComputeChecksum(),
MyDeparser()
) main;
