/*
 * Copyright 2023-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.netflow;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static org.onosproject.netflow.NetflowUtils.VAR_INT_LONG;
import static org.onosproject.netflow.NetflowUtils.VAR_BYTE;
import static org.onosproject.netflow.NetflowUtils.NULL;
import static org.onosproject.netflow.NetflowUtils.VAR_SHORT;
import static org.onosproject.netflow.NetflowUtils.VAR_IP_ADDRESS;
import static org.onosproject.netflow.NetflowUtils.VAR_SHORT_INT;
import static org.onosproject.netflow.NetflowUtils.VAR_INT;
import static org.onosproject.netflow.NetflowUtils.VAR_MAC;

/**
 * The flow fields are a selection of Packet Header
 * fields, lookup results (for example, the autonomous system numbers or
 * the subnet masks), and properties of the packet such as length.
 */
public enum FlowField {

    /**
     * Flow field type definition reference.
     * RFC reference :- rfc3954
     * https://www.ietf.org/rfc/rfc3954.txt
     * Section :- Field Type Definitions.
     */
    IN_BYTES(1, VAR_INT_LONG),
    IN_PKTS(2, VAR_INT_LONG),
    FLOWS(3, VAR_INT_LONG),
    PROTOCOL(4, VAR_BYTE),
    SRC_TOS(5, VAR_BYTE),
    TCP_FLAGS(6, VAR_BYTE),
    L4_SRC_PORT(7, VAR_SHORT),
    IPV4_SRC_ADDR(8, VAR_IP_ADDRESS),
    SRC_MASK(9, VAR_BYTE),
    INPUT_SNMP(10, VAR_SHORT_INT),
    L4_DST_PORT(11, VAR_SHORT),
    IPV4_DST_ADDR(12, VAR_IP_ADDRESS),
    DST_MASK(13, VAR_BYTE),
    OUTPUT_SNMP(14, VAR_SHORT_INT),
    IPV4_NEXT_HOP(15, VAR_IP_ADDRESS),
    SRC_AS(16, VAR_SHORT_INT),
    DST_AST(17, VAR_SHORT_INT),
    BGP_IPV4_NEXT_HOP(18, VAR_IP_ADDRESS),
    MUL_DST_PKTS(19, VAR_INT_LONG),
    MUL_DST_BYTES(20, VAR_INT_LONG),
    LAST_SWITCHED(21, VAR_INT),
    FIRST_SWITCHED(22, VAR_INT),
    OUT_BYTES(23, VAR_INT_LONG),
    OUT_PKTS(24, VAR_INT_LONG),
    MIN_PKT_LNGTH(25, NULL),
    MAX_PKT_LNGTH(26, NULL),
    IPV6_SRC_ADDR(27, VAR_IP_ADDRESS),
    IPV6_DST_ADDR(28, VAR_IP_ADDRESS),
    IPV6_SRC_MASK(29, VAR_BYTE),
    IPV6_DST_MASK(30, VAR_BYTE),
    IPV6_FLOW_LABEL(31, NULL),
    ICMP_TYPE(32, VAR_SHORT),
    MUL_IGMP_TYPE(33, VAR_BYTE),
    SAMPLING_INTERVAL(34, VAR_INT),
    SAMPLING_ALGORITHM(35, VAR_BYTE),
    FLOW_ACTIVE_TIMEOUT(36, VAR_SHORT),
    FLOW_INACTIVE_TIMEOUT(37, VAR_SHORT),
    ENGINE_TYPE(38, VAR_BYTE),
    ENGINE_ID(39, VAR_BYTE),
    TOTAL_BYTES_EXP(40, VAR_INT_LONG),
    TOTAL_PKTS_EXP(41, VAR_INT_LONG),
    TOTAL_FLOWS_EXP(42, VAR_INT_LONG),
    IPV4_SRC_PREFIX(44, NULL),
    IPV4_DST_PREFIX(45, NULL),
    MPLS_TOP_LABEL_TYPE(46, VAR_BYTE),
    MPLS_TOP_LABEL_IP_ADDR(47, VAR_IP_ADDRESS),
    FLOW_SAMPLER_ID(48, VAR_BYTE),
    FLOW_SAMPLER_MODE(49, VAR_BYTE),
    FLOW_SAMPLER_RANDOM_INTERVAL(50, NULL),
    MIN_TTL(52, NULL),
    MAX_TTL(53, NULL),
    IPV4_IDENT(54, NULL),
    DST_TOS(55, VAR_BYTE),
    IN_SRC_MAC(56, VAR_MAC),
    OUT_DST_MAC(57, VAR_MAC),
    SRC_VLAN(58, VAR_SHORT),
    DST_VLAN(59, VAR_SHORT),
    IP_PROTOCOL_VERSION(60, VAR_BYTE),
    DIRECTION(61, VAR_BYTE),
    IPV6_NEXT_HOP(62, VAR_IP_ADDRESS),
    BPG_IPV6_NEXT_HOP(63, VAR_IP_ADDRESS),
    IPV6_OPTION_HEADERS(64, VAR_INT),
    MPLS_LABEL_1(70, NULL),
    MPLS_LABEL_2(71, NULL),
    MPLS_LABEL_3(72, NULL),
    MPLS_LABEL_4(73, NULL),
    MPLS_LABEL_5(74, NULL),
    MPLS_LABEL_6(75, NULL),
    MPLS_LABEL_7(76, NULL),
    MPLS_LABEL_8(77, NULL),
    MPLS_LABEL_9(78, NULL),
    MPLS_LABEL_10(79, NULL),
    IN_DST_MAC(80, VAR_MAC),
    OUT_SRC_MAC(81, VAR_MAC),
    IF_NAME(82, NULL),
    IF_DESC(83, NULL),
    SAMPLER_NAME(84, NULL),
    IN_PERMANENT_BYTES(85, NULL),
    IN_PERMANENT_PKTS(86, NULL),
    FRAGMENT_OFFSET(88, NULL),
    FORWARDING_STATUS(89, NULL),
    MPLS_PAL_RD(90, NULL),
    MPLS_PREFIX_LEN(91, NULL),
    SRC_TRAFFIC_INDEX(92, NULL),
    DST_TRAFFIC_INDEX(93, NULL),
    APPLICATION_DESCRIPTION(94, NULL),
    APPLICATION_TAG(95, NULL),
    APPLICATION_NAME(96, NULL),
    POST_IP_DIFF_SERV_CODE_POINT(98, NULL),
    REPLICATION_FACTOR(99, NULL),
    DEPRECATED(100, NULL),
    LAYER2_PACKET_SECTION_OFFSET(102, NULL),
    LAYER2_PACKET_SECTION_SIZE(103, NULL),
    LAYER2_PACKET_SECTION_DATA(104, NULL);

    final int fieldID;
    final BiFunction<ByteBuffer, Integer, Object> parser;
    private static Map<Integer, FlowField> fields = new ConcurrentHashMap<>();

    private FlowField(int fieldID, BiFunction<ByteBuffer, Integer, Object> parser) {
        this.fieldID = fieldID;
        this.parser = parser;
    }

    static {
        Arrays.stream(FlowField.values()).forEach(f -> fields.put(f.fieldID, f));
    }

    public static Optional<FlowField> getField(int fieldId) {
        return Optional.of(fieldId)
                .filter(id -> fields.containsKey(id))
                .map(id -> fields.get(id));
    }

    public BiFunction<ByteBuffer, Integer, Object> getParser() {
        return this.parser;
    }

}
