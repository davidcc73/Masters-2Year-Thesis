/* -*- P4_16 -*- */
#include <core.p4>
#include <v1model.p4>

const bit<16> TYPE_IPV4 = 0x800;

/*************************************************************************
*********************** H E A D E R S  ***********************************
*************************************************************************/

typedef bit<9>  egressSpec_t;   //var globais para facilitar a vida
typedef bit<48> macAddr_t;
typedef bit<32> ip4Addr_t;

header ethernet_t {				       //define o header dum pacote ethernet
    macAddr_t dstAddr;
    macAddr_t srcAddr;
    bit<16>   etherType;		    //indica o tipo de protocolo encapsulado nos dados do pacote Ethernet
}

header ipv4_t {					        //representa o header do pacote IPV4 recebido
    bit<4>    version;         	// Versão do protocolo IPv4.
    bit<4>    ihl;             	// Tamanho do cabeçalho em palavras de 32 bits.
    bit<8>    diffserv;       	// Campo de Serviços Diferenciados.
    bit<16>   totalLen;       	// Comprimento total do datagrama IPv4.
    bit<16>   identification; 	// Identificação única para o datagrama.
    bit<3>    flags;          	// Flags de fragmentação (exemplo: bit de "Não Fragmentar").
    bit<13>   fragOffset;      	// Deslocamento de fragmentação.
    bit<8>    ttl;             	// Tempo de Vida (TTL) do datagrama.
    bit<8>    protocol;        	// Protocolo da camada superior (exemplo: TCP, UDP).
    bit<16>   hdrChecksum;    	// Soma de verificação do cabeçalho.
    ip4Addr_t srcAddr;         	// Endereço IP de origem.
    ip4Addr_t dstAddr;         	// Endereço IP de destino.
}

struct metadata {
    /* empty */
}

struct headers {				        //ambas as var tem muita informacao dentro delas
    ethernet_t   ethernet;
    ipv4_t       ipv4;
}

/*************************************************************************
*********************** P A R S E R  ***********************************
*************************************************************************/

parser MyParser(packet_in packet,                                  /*coloca o pacote recebido na var packet*/
                out headers hdr,									                 /*cria uma var hdr da estrutura headers que será passada a diante apos o parser*/
                inout metadata meta,								               /*cria var(meta) do tipo metadata, para guardar info extra sobre o pacote atual, usado para levar info as etapas seguintes.*/
				        inout standard_metadata_t standard_metadata) {		 /*inout diz que o parser pode ler/escrever nesta estrutura, var do tipo standard_metadata que tem info comumente usada: such as ingress port, egress port, packet length, and other standard attributes.*/

    state start {
        transition parse_ethernet;						        /*transição automatica*/
    }

    state parse_ethernet {                            /*extrair o header de ethernet do pacote*/
        packet.extract(hdr.ethernet);					        /*copia o header ethernet do pacote e store dele em hdr.ethernet*/
        transition select(hdr.ethernet.etherType) {		/*declarar if baseado no valor da atributo etherType no hdr.ethernet*/
            TYPE_IPV4: parse_ipv4;						        /*se IPv4, transicionamos para parse_ipv4*/
            default: accept;							            /*else o pacote será avançado pra análise seginte*/
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

control MyVerifyChecksum(inout headers hdr, inout metadata meta) {  /*n usado neste exe, mas verifica os checksums*/
    apply {  }
}


/*************************************************************************
**************  I N G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyIngress(inout headers hdr,
                  inout metadata meta,
                  inout standard_metadata_t standard_metadata) {        /*receber as var vindas das etapas anteriores*/

    action drop() {														                          /*define a acção(funcao) drop()*/
        mark_to_drop(standard_metadata);								                /*marca o pacote atual como futuro drop, assim as proximas etapas ja sabem o que fazer*/
    }

    action ipv4_forward(macAddr_t dstAddr, egressSpec_t port) {			    /*vamos alterar o hdr(copia do header) para depois usar como novo header do pacote, os arg vem da aplicaçao da tabela no json file*/
        standard_metadata.egress_spec = port;							              /*port para onde o pacote deve ser enviado*/
        hdr.ethernet.srcAddr = hdr.ethernet.dstAddr;					          /*mudar MAC src no header para o de dest*/
        hdr.ethernet.dstAddr = dstAddr;									                /*mudar MAC dst no header para prox dest*/
        hdr.ipv4.ttl = hdr.ipv4.ttl - 1;
    }

    table ipv4_lpm {                                                    /*acho que é uma represent abst da tabela no .json, mo caso cada switch tem a sua*/
        key = {
            hdr.ipv4.dstAddr: lpm;
        }
        actions = {                                                     /*por ser uma abstração, temos de por todas as hipoteses que podem vir dos json das tabelas*/
            ipv4_forward;
            drop;
            NoAction;
        }
        size = 1024;                                                    /*tamanho maximo da tabela chamada*/
        default_action = NoAction();
    }

    apply {									        /*bloco de controlo, onde sao tomadas as decisoes, é o que corre quando entramos na ingress*/
        if (hdr.ipv4.isValid()) {		/*avaliar a qualidade do header "hdr" aqui ainda OG*/
            ipv4_lpm.apply();				/*se o header for valido aplica-lhe a tabela ipv4_lpm*/
        }
    }
}

/*************************************************************************
****************  E G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyEgress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {
    apply {  }                      /*?????não fazemos nada*/
}

/*************************************************************************
*************   C H E C K S U M    C O M P U T A T I O N   **************
*************************************************************************/

control MyComputeChecksum(inout headers hdr, inout metadata meta) { /*atualiza os checksum com as alteracoes dos headers*/
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

control MyDeparser(packet_out packet, in headers hdr) {			/*a ordem que aplicamos as alterações é relevante*/
    apply {
        packet.emit(hdr.ethernet);	   /*copiar os dados de hdr.ethernet para o pacote de saida (neste caso para o cabelhalho ethernet)*/
        packet.emit(hdr.ipv4);		     /*copiar os dados de hdr.ipv4     para o pacote de saida (neste caso para o cabelhalho ipv4)*/
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
