/* -*- P4_16 -*- */
#include <core.p4>
#include <v1model.p4>

/* CONSTANTS */

const bit<16> TYPE_IPV4 = 0x800;
const bit<8>  TYPE_TCP  = 6;

#define BLOOM_FILTER_ENTRIES 4096
#define BLOOM_FILTER_BIT_WIDTH 1

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

header tcp_t{
    bit<16> srcPort;
    bit<16> dstPort;
    bit<32> seqNo;
    bit<32> ackNo;
    bit<4>  dataOffset;
    bit<4>  res;
    bit<1>  cwr;
    bit<1>  ece;
    bit<1>  urg;
    bit<1>  ack;
    bit<1>  psh;
    bit<1>  rst;
    bit<1>  syn;
    bit<1>  fin;
    bit<16> window;
    bit<16> checksum;
    bit<16> urgentPtr;
}

struct metadata {
    /* empty */
}

struct headers {
    ethernet_t   ethernet;
    ipv4_t       ipv4;
    tcp_t        tcp;
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
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.etherType) {
            TYPE_IPV4: parse_ipv4;
            default: accept;
        }
    }

    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition select(hdr.ipv4.protocol){
            TYPE_TCP: tcp;
            default: accept;
        }
    }

    state tcp {
       packet.extract(hdr.tcp);
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

    register<bit<BLOOM_FILTER_BIT_WIDTH>>(BLOOM_FILTER_ENTRIES) bloom_filter_1;
    register<bit<BLOOM_FILTER_BIT_WIDTH>>(BLOOM_FILTER_ENTRIES) bloom_filter_2;
    bit<32> reg_pos_one; bit<32> reg_pos_two;
    bit<1> reg_val_one; bit<1> reg_val_two;
    bit<1> direction;

    action drop() {
        mark_to_drop(standard_metadata);
    }

    action compute_hashes(ip4Addr_t ipAddr1, ip4Addr_t ipAddr2, bit<16> port1, bit<16> port2){
       //Get register position
       hash(reg_pos_one, HashAlgorithm.crc16, (bit<32>)0, {ipAddr1,								/*calcular a posicao usada pela conexao do pacote atual, para o filtro1*/
                                                           ipAddr2,
                                                           port1,
                                                           port2,
                                                           hdr.ipv4.protocol},
                                                           (bit<32>)BLOOM_FILTER_ENTRIES);

       hash(reg_pos_two, HashAlgorithm.crc32, (bit<32>)0, {ipAddr1,								/*calcular a posicao usada pela conexao do pacote atual, para o filtro2*/
                                                           ipAddr2,
                                                           port1,
                                                           port2,
                                                           hdr.ipv4.protocol},
                                                           (bit<32>)BLOOM_FILTER_ENTRIES);
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
        default_action = drop();
    }

    action set_direction(bit<1> dir) {				//para sabermos o snetido do trafego, 0 para sair
        direction = dir;
    }

    table check_ports {
        key = {
            standard_metadata.ingress_port: exact;
            standard_metadata.egress_spec: exact;
        }
        actions = {
            set_direction;
            NoAction;
        }
        size = 1024;
        default_action = NoAction();
    }

    apply {
        if (hdr.ipv4.isValid()){
            ipv4_lpm.apply();
            if (hdr.tcp.isValid()){
                direction = 0; // default
                if (check_ports.apply().hit) {					//so seguimos se tiver havido uma entrada na tabela apos a aplicaçao
                    // test and set the bloom filter
                    if (direction == 0) {						//aplicamos 2 hashs so 2 call pois sso é importante para garantir que os valores calculados na função hash sejam os mesmos, independentemente de o pacote estar indo para dentro ou para fora da rede
                        compute_hashes(hdr.ipv4.srcAddr, hdr.ipv4.dstAddr, hdr.tcp.srcPort, hdr.tcp.dstPort);
                    }
                    else {
                        compute_hashes(hdr.ipv4.dstAddr, hdr.ipv4.srcAddr, hdr.tcp.dstPort, hdr.tcp.srcPort);
                    }
                    // Packet comes from internal network
                    if (direction == 0){
                        // TODO: this packet is part of an outgoing TCP connection.
                        //   We need to set the bloom filter if this is a SYN packet
                        //   E.g. bloom_filter_1.write(<index>, <value>);
						if (hdr.tcp.syn == 1){
                            bloom_filter_1.write(reg_pos_one, 1);			//se for para cria uma conecao, def o novo filtro, para que a resposta conseguirá passar por este
                            bloom_filter_2.write(reg_pos_two, 1);			//na posicao reg_pos_two do filtro, escrevemos 1, usando 2 filtros a chance de falhar é quase nula
                        }
                    }
                    // Packet comes from outside
                    else if (direction == 1){
                        // TODO: this packet is part of an incomming TCP connection.
                        //   We need to check if this packet is allowed to pass by reading the bloom filter
                        //   E.g. bloom_filter_1.read(<value>, <index>);
						
						// Read bloom filter cells to check if there are 1's
                        bloom_filter_1.read(reg_val_one, reg_pos_one);					
                        bloom_filter_2.read(reg_val_two, reg_pos_two);		//copia para reg_val_two o valor do campo reg_pos_two do filtro, 
                        // only allow flow to pass if both entries are set
                        if (reg_val_one != 1 || reg_val_two != 1){			//conecoes aprovadas tem valor 1 colocado durante o SYN, logo é conecao aprovada
                            drop();	
                        }
                    }
                }
            }
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
        packet.emit(hdr.ethernet);
        packet.emit(hdr.ipv4);
        packet.emit(hdr.tcp);
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