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
import java.util.function.Predicate;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import org.onlab.packet.DeserializationException;
import org.onlab.packet.Deserializer;

import static com.google.common.base.Preconditions.checkState;

/**
 * One of the essential elements in the NetFlow format is the Template
 * FlowSet.  Templates greatly enhance the flexibility of the Flow
 * Record format because they allow the NetFlow Collector to process
 * Flow Records without necessarily knowing the interpretation of all
 * the data in the Flow Record.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public final class TemplateFlowSet extends FlowSet {

    /*
        0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |       FlowSet ID = 0          |          Length               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |      Template ID 256          |         Field Count           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 1           |         Field Length 1        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 2           |         Field Length 2        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |             ...               |              ...              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type N           |         Field Length N        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |      Template ID 257          |         Field Count           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 1           |         Field Length 1        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 2           |         Field Length 2        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |             ...               |              ...              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type M           |         Field Length M        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |             ...               |              ...              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Template ID K          |         Field Count           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |             ...               |              ...              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private int flowSetId;

    private int length;

    private List<DataTemplateRecord> records;

    private TemplateFlowSet(Builder builder) {
        this.records = builder.records;
        this.length = builder.length;
        this.flowSetId = builder.flowSetId;
    }

    /**
     * Return template flow set id.
     * FlowSet ID value of 0 is reserved for the Template FlowSet.
     *
     * @return flow set ID
     */
    @Override
    public int getFlowSetId() {
        return this.flowSetId;
    }

    /**
     * Returns total length of this flowSet.
     *
     * @return length of flowset
     */
    @Override
    public int getLength() {
        return this.length;
    }

    /**
     * Returns list of flow records.
     *
     * @return list of flow records
     */
    public List<DataTemplateRecord> getRecords() {
        return records;
    }

    /**
     * Returns type of the flowset.
     *
     * @return type of the flowset
     */
    @Override
    public Type getType() {
        return Type.TEMPLATE_FLOWSET;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.flowSetId;
        hash = 53 * hash + this.length;
        hash = 53 * hash + Objects.hashCode(this.records);
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
        final TemplateFlowSet other = (TemplateFlowSet) obj;
        if (this.flowSetId != other.flowSetId) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return Objects.equals(this.records, other.records);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("flowSetId", flowSetId)
                .add("length", length)
                .add("records", records)
                .toString();
    }

    /**
     * Data deserializer function for template flow set.
     *
     * @return data deserializer function
     */
    public static Deserializer<TemplateFlowSet> deserializer() {
        return (data, offset, length) -> {

            Predicate<ByteBuffer> isValidBuffer = b -> b.remaining() < FlowSet.FIELD_LENTH;

            ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            if (isValidBuffer.test(bb)) {
                throw new DeserializationException("Invalid buffer size");
            }
            Builder builder = new Builder()
                    .flowSetId(bb.getShort())
                    .length(bb.getShort());
            while (bb.hasRemaining()) {
                if (isValidBuffer.test(bb)) {
                    break;
                }
                int templateId = bb.getShort();
                int fieldCount = bb.getShort();
                int bufferLength = (fieldCount * FlowSet.FIELD_LENTH) + FlowSet.FIELD_LENTH;
                byte[] record = new byte[bufferLength];
                bb.position(bb.position() - FlowSet.FIELD_LENTH);
                if (bb.remaining() < bufferLength) {
                    break;
                }
                bb.get(record);
                builder.templateRecord(DataTemplateRecord.deserializer().deserialize(record, 0, bufferLength));

            }
            return builder.build();
        };
    }

    /**
     * Builder for template flow set.
     */
    private static class Builder {

        private int flowSetId;

        private int length;

        private List<DataTemplateRecord> records = new LinkedList<>();

        /**
         * Setter for template flow set id.
         *
         * @param flowSetId template flow set id.
         * @return this class builder.
         */
        public Builder flowSetId(int flowSetId) {
            this.flowSetId = flowSetId;
            return this;
        }

        /**
         * Setter for total length of this flowSet.
         *
         * @param length total length of this flowSet.
         * @return this class builder.
         */
        public Builder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * Setter for list of flow records.
         *
         * @param records list of flow records.
         * @return this class builder.
         */
        public Builder templateRecords(List<DataTemplateRecord> records) {
            this.records = records;
            return this;
        }

        /**
         * Setter for flow records.
         *
         * @param record flow records.
         * @return this class builder.
         */
        public Builder templateRecord(DataTemplateRecord record) {
            this.records.add(record);
            return this;
        }

        /**
         * Checks arguments for template flow set.
         */
        private void checkArguments() {
            checkState(flowSetId == 0, "Invalid flow set id.");
            checkState(length == 0, "Invalid flow set length.");

        }

        /**
         * Builds template flow set.
         *
         * @return template flow set.
         */
        public TemplateFlowSet build() {
            checkArguments();
            return new TemplateFlowSet(this);
        }

    }

}
