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
package org.onosproject.netflow.impl;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import com.google.common.base.MoreObjects;
import java.util.Objects;

import org.onlab.packet.Deserializer;
import org.onlab.packet.BasePacket;

import static com.google.common.base.Preconditions.checkState;


/**
 * An Netflow Packet consists of a Packet Header followed by one or more
 * FlowSets.  The FlowSets can be any of the possible three types:
 * Template, Data, or Options Template.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public final class NetFlowPacket extends BasePacket {

    /*
     +--------+-------------------------------------------+
     |        | +----------+ +---------+ +----------+     |
     | Packet | | Template | | Data    | | Options  |     |
     | Header | | FlowSet  | | FlowSet | | Template | ... |
     |        | |          | |         | | FlowSet  |     |
     |        | +----------+ +---------+ +----------+     |
     +--------+-------------------------------------------+

   The Packet Header format is specified as:

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |       Version Number          |            Count              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                           sysUpTime                           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                           UNIX Secs                           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                       Sequence Number                         |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                        Source ID                              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

*/


    private int version;
    private int count;
    private long sysUptime;
    private long timestamp;
    private int flowSequence;
    private int sourceId;
    private List<FlowSet> flowSets;

    private NetFlowPacket(Builder builder) {
        this.version = builder.version;
        this.count = builder.count;
        this.sysUptime = builder.sysUptime;
        this.timestamp = builder.timestamp;
        this.flowSequence = builder.flowSequence;
        this.sourceId = builder.sourceId;
        this.flowSets = builder.flowSets;
    }

    /**
     * Returns Version of Flow Record format exported in this packet.
     *
     * @return version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns total number of records in the Export Packet.
     *
     * @return total number of recoreds
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns time in milliseconds since this device was first booted.
     *
     * @return system up time
     */
    public long getSysUptime() {
        return sysUptime;
    }

    /**
     * Returns packet timestamp.
     * Time in seconds since 0000 UTC 1970, at which the Export Packet
     * leaves the Exporter
     *
     * @return packet time stamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns flow packet sequence number.
     * Incremental sequence counter of all Export Packets sent from
     * the current Observation Domain by the Exporter.  This value
     * MUST be cumulative, and SHOULD be used by the Collector to
     * identify whether any Export Packets have been missed.
     *
     * @return packet sequence number
     */
    public int getFlowSequence() {
        return flowSequence;
    }

    /**
     * Returns A 32-bit value that identifies the Exporter Observation Domain.
     *
     * @return exporter source id
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * Returns list of flowsets.
     *
     * @return list of flowsets
     */
    public List<FlowSet> getFlowSets() {
        return flowSets;
    }

    /**
     * Deserializer function for netflow packets.
     *
     * @return deserializer function
     */
    public static Deserializer<NetFlowPacket> deserializer() {
        return (data, offset, length) -> {
            System.out.println(length);
            ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            Builder builder = new Builder();
            builder.version(bb.getShort())
                    .count(bb.getShort())
                    .sysUptime(bb.getInt())
                    .timestamp(bb.getInt())
                    .flowSequence(bb.getInt())
                    .sourceId(bb.getInt());
            while (bb.hasRemaining()) {

                int flowSetId = bb.getShort();
                int flowSetLength = bb.getShort();
                bb.position(bb.position() - FlowSet.FLOW_SET_HEADER_LENTH);
                byte[] flowSet;
                if (bb.remaining() < flowSetLength) {
                    break;
                }

                flowSet = new byte[flowSetLength];
                bb.get(flowSet);

                builder.flowSet((FlowSet) FlowSet.Type.getType(flowSetId).getDecoder()
                        .deserialize(flowSet, 0, flowSetLength));
            }
            return builder.build();
        };
    }

    @Override
    public byte[] serialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.version;
        hash = 59 * hash + this.count;
        hash = 59 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        hash = 59 * hash + this.flowSequence;
        hash = 59 * hash + this.sourceId;
        hash = 59 * hash + Objects.hashCode(this.flowSets);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NetFlowPacket other = (NetFlowPacket) obj;
        if (this.version != other.version) {
            return false;
        }
        if (this.count != other.count) {
            return false;
        }
        if (this.timestamp != other.timestamp) {
            return false;
        }
        if (this.flowSequence != other.flowSequence) {
            return false;
        }
        if (this.sourceId != other.sourceId) {
            return false;
        }
        return Objects.equals(this.flowSets, other.flowSets);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("version", version)
                .add("count", count)
                .add("sysUptime", sysUptime)
                .add("timestamp", timestamp)
                .add("flowSequence", flowSequence)
                .add("sourceId", sourceId)
                .add("flowSets", flowSets)
                .toString();
    }

    /**
     * Builder for netflow packet.
     */
    private static class Builder {

        private int version;
        private int count;
        private long sysUptime;
        private long timestamp;
        private int flowSequence;
        private int sourceId;
        private List<FlowSet> flowSets = new LinkedList<>();

        /**
         * Setter Version of Flow Record format exported in this packet.
         *
         * @param version number.
         * @return this class builder.
         */
        public Builder version(int version) {
            this.version = version;
            return this;
        }

        /**
         * Setter for total number of records in the Export Packet.
         *
         * @param count flow record count.
         * @return this class builder.
         */
        public Builder count(int count) {
            this.count = count;
            return this;
        }

        /**
         * Setter for time in milliseconds since this device was first booted.
         *
         * @param sysUptime system up time.
         * @return this class builder.
         */
        public Builder sysUptime(long sysUptime) {
            this.sysUptime = sysUptime;
            return this;
        }

        /**
         * Setter for packet timestamp.
         *
         * @param timestamp packet timestamp.
         * @return this class builder.
         */
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Setter for flow sequence.
         *
         * @param flowSequence flow sequence.
         * @return this class builder.
         */
        public Builder flowSequence(int flowSequence) {
            this.flowSequence = flowSequence;
            return this;
        }

        /**
         * Setter for sourceId.
         *
         * @param sourceId exporter sourceid.
         * @return this class builder.
         */
        public Builder sourceId(int sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        /**
         * Setter for list of flowsets.
         *
         * @param flowSets list of flowsets.
         * @return this class builder.
         */
        public Builder flowSets(List<FlowSet> flowSets) {
            this.flowSets = flowSets;
            return this;
        }

        /**
         * Setter for flowset.
         *
         * @param flowSet flowset.
         * @return this class builder.
         */
        public Builder flowSet(FlowSet flowSet) {
            this.flowSets.add(flowSet);
            return this;
        }

        /**
         * Checks arguments for netflow packet.
         */
        private void checkArguments() {
            checkState(version != 0, "Invalid Version.");
            checkState(count != 0, "Invalid record count.");
            checkState(sysUptime != 0, "Invalid system up time.");
            checkState(timestamp != 0, "Invalid flow timestamp.");
        }

        /**
         * Builds Netflow packet.
         *
         * @return Netflowpacket.
         */
        public NetFlowPacket build() {
            checkArguments();
            return new NetFlowPacket(this);
        }

    }

}
