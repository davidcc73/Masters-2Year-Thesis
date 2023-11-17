/* -*- P4_16 -*- */
#include <core.p4>
#include <v1model.p4>

const bit<16> TYPE_IPV4 = 0x800;
const bit<16> TYPE_SRCROUTING = 0x1234;

#define MAX_HOPS 9

/*************************************************************************
*********************** H E A D E R S  ***********************************
*************************************************************************/

typedef bit<9>  egressSpec_t;
typedef bit<48> macAddr_t;
typedef bit<32> ip4Addr_t;

header ethernet_t {
    macAddr_t dstAddr;
    macAddr_t srcAddr;
    bit<16>   etherType;
}

header srcRoute_t {
    bit<1>    bos;
    bit<15>   port;
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

struct metadata {
    /* empty */
}

struct headers {
    ethernet_t              ethernet;
    srcRoute_t[MAX_HOPS]    srcRoutes;
    ipv4_t                  ipv4;
}

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
        /*
         * TODO: Modify the next line to select on hdr.ethernet.etherType
         * If the value is TYPE_SRCROUTING transition to parse_srcRouting
         * otherwise transition to accept.
         */
		packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.etherType) {
            TYPE_SRCROUTING: parse_srcRouting;				//se no final formos o ultimo na pilha podemos remover do pacote e por um ipv4 no lugar
            default: accept;
        }
    }

    state parse_srcRouting {								//extração recursiva
        /*
         * TODO: extract the next entry of hdr.srcRoutes
         * while hdr.srcRoutes.last.bos is 0 transition to this state
         * otherwise parse ipv4
         */
		packet.extract(hdr.srcRoutes.next); 		//guardar no proximo array, n esquecer
		transition select(hdr.srcRoutes.last.bos){	//vamos ao ultimo elemento inserido no array, neste caso o topo por ser uma pilha, e vemos se é o final
			1: parse_ipv4;							//se o ultimo extraido for o ultimo da pilha n temos mais nada a extrair
            default: parse_srcRouting;
			
		}
    }

    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition accept;
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

    action srcRoute_nhop() {
        /*
         * TODO: set standard_metadata.egress_spec
         * to the port in hdr.srcRoutes[0] and
         * pop an entry from hdr.srcRoutes
         */
		standard_metadata.egress_spec = (bit<9>)hdr.srcRoutes[0].port;		/*def o porto de saida que vem no pacote*/
        hdr.srcRoutes.pop_front(1);											/*tirar o topo da pilha*/
    }

    action srcRoute_finish() {
        hdr.ethernet.etherType = TYPE_IPV4;
    }

    action update_ttl(){
        hdr.ipv4.ttl = hdr.ipv4.ttl - 1;
    }

    apply {
		/*
        * TODO: add logic to:
        * - If final srcRoutes (top of stack has bos==1):
        *   - change etherType to IP
        * - choose next hop and remove top of srcRoutes stack
        */
        if (hdr.srcRoutes[0].isValid()){		/*ver o que esta no topo da pilha, que seria para nos*/
            if (hdr.srcRoutes[0].bos == 1){		/*se for ultimo emcaminhamos de forma padrao*/
                srcRoute_finish();				/*se ultiimo, antes podemos mudar para ipv4*/
            }													
            srcRoute_nhop();					/*defenir para qual port mandar agora, mesmo o topo sendo o ultimo ele ainda é referente a nos*/
            if (hdr.ipv4.isValid()){
                update_ttl();
            }
        }else{
            drop();
        }
    }
}

/*************************************************************************
****************  E G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyEgress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {
    apply {  }
}

/*************************************************************************
*************   C H E C K S U M    C O M P U T A T I O N   **************
*************************************************************************/

control MyComputeChecksum(inout headers  hdr, inout metadata meta) {
    apply {  }
}

/*************************************************************************
***********************  D E P A R S E R  *******************************
*************************************************************************/

control MyDeparser(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.ethernet);
        packet.emit(hdr.srcRoutes);
        packet.emit(hdr.ipv4);
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
