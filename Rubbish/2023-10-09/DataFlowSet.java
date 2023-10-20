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
import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

import org.onlab.packet.DeserializationException;
import org.onlab.packet.Deserializer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A Data FlowSet is one or more records, of the same type, that are
 * grouped together in an Export Packet.  Each record is either a Flow
 * Data Record or an Options Data Record previously defined by a
 * Template Record or an Options Template Record.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public final class DataFlowSet extends FlowSet {

    /*
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   FlowSet ID = Template ID    |          Length               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 1 - Field Value 1    |   Record 1 - Field Value 2    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 1 - Field Value 3    |             ...               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 2 - Field Value 1    |   Record 2 - Field Value 2    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 2 - Field Value 3    |             ...               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 3 - Field Value 1    |             ...               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |              ...              |            Padding            |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private int flowSetId;

    private int length;

    private byte[] data;

    private DataFlowSet(Builder builder) {
        this.flowSetId = builder.flowSetId;
        this.length = builder.length;
        this.data = builder.data;
    }

    private List<DataFlowRecord> dataFlow = new LinkedList<>();

    /**
     * Returns flowset id.
     * Each Data FlowSet is associated with a FlowSet ID.  The FlowSet
     * ID maps to a (previously generated) Template ID
     *
     * @return flowset id
     */
    @Override
    public int getFlowSetId() {
        return this.flowSetId;
    }

    /**
     * Returns length of this FlowSet.
     * Length is the sum of the lengths
     * of the FlowSet ID, Length itself, all Flow Records within this
     * FlowSet, and the padding bytes, if any.
     *
     * @return length of the flowset
     */
    @Override
    public int getLength() {
        return this.length;
    }

    /**
     * Returns list of data flow records.
     *
     * @return list of data flow records
     */
    public List<DataFlowRecord> getDataFlow() {
        return dataFlow;
    }

    /**
     * Set data flow record.
     *
     * @param dataFlow data flow record
     */
    public void setDataFlow(DataFlowRecord dataFlow) {
        this.dataFlow.add(dataFlow);
    }

    /**
     * Returns type of flowset.
     *
     * @return type of flowset
     */
    @Override
    public Type getType() {
        return Type.DATA_FLOWSET;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.flowSetId;
        hash = 79 * hash + this.length;
        hash = 79 * hash + Objects.hashCode(this.dataFlow);
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
        final DataFlowSet other = (DataFlowSet) obj;
        if (this.flowSetId != other.flowSetId) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return Objects.equals(this.dataFlow, other.dataFlow);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("flowSetId", flowSetId)
                .add("length", length)
                .add("data", data)
                .toString();
    }

    /**
     * Deserializer function for data flow set.
     *
     * @return deserializer function
     */
    public static Deserializer<DataFlowSet> deserializer() {
        return (data, offset, length) -> {
            Function<ByteBuffer, byte[]> readBytes = b -> {
                if (b.remaining() == b.limit()) {
                    return null;
                }
                byte[] bytes = new byte[b.remaining()];
                b.get(bytes);
                return bytes;
            };
            ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            return new Builder()
                    .flowSetId(bb.getShort())
                    .length(bb.getShort())
                    .data(readBytes.apply(bb))
                    .build();

        };
    }

    /**
     * Data eserializer function for data flow record.
     *
     * @param template data template record
     * @throws DeserializationException if unable to deserialize data
     */
    public void dataDeserializer(DataTemplateRecord template) throws DeserializationException {
        dataFlow = new LinkedList<>();
        ByteBuffer bb = ByteBuffer.wrap(data, 0, data.length);

        while (bb.hasRemaining()) {
            if (bb.remaining() < template.getValueLength()) {
                break;
            }
            byte[] dataRecord = new byte[template.getValueLength()];
            bb.get(dataRecord);
            this.setDataFlow(DataFlowRecord.deserializer().deserialize(
                    data, 0, template.getValueLength(), template));
        }

    }

    /**
     * Builder for data flow set.
     */
    private static class Builder {

        private int flowSetId;

        private int length;

        private byte[] data;

        /**
         * Setter for flowset id.
         *
         * @param flowSetId flowset id.
         * @return this class builder.
         */
        public Builder flowSetId(int flowSetId) {
            this.flowSetId = flowSetId;
            return this;
        }

        /**
         * Setter for length of this FlowSet.
         *
         * @param length length of this FlowSet.
         * @return this class builder.
         */
        public Builder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * Setter for flow data.
         *
         * @param data flow data.
         * @return this class builder.
         */
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        /**
         * Checks arguments for data flow set.
         */
        private void checkArguments() {
            checkState(flowSetId != 0, "Invalid data flowset id.");
            checkState(length != 0, "Invalid data flowset length.");
            checkNotNull(data, "Data flow set cannot be null");

        }

        /**
         * Builds data flowset.
         *
         * @return data flowset.
         */
        public DataFlowSet build() {
            checkArguments();
            return new DataFlowSet(this);
        }

    }

}
