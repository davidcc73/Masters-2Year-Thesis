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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

import org.onosproject.netflow.DataRecord;
import org.onosproject.netflow.TemplateId;
import org.onosproject.netflow.Flow;
import org.onosproject.netflow.FlowTemplateField;
import org.onosproject.netflow.DataDeserializer;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * A Flow Data Record is a data record that contains values of the Flow.
 * parameters corresponding to a Template Record.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public class DataFlowRecord extends DataRecord {

    /*
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 1 - Field Value 1    |   Record 1 - Field Value 2    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 1 - Field Value 3    |             ...               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 2 - Field Value 1    |   Record 2 - Field Value 2    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 2 - Field Value 3    |             ...               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private TemplateId templateId;

    private List<Flow> flows;

    public DataFlowRecord(Builder builder) {
        this.templateId = builder.templateId;
        this.flows = builder.flows;
    }

    /**
     * Returns unique template ID.
     * Template Records is given a unique Template ID.
     * This uniqueness is local to the Observation
     * Domain that generated the Template ID.  Template IDs 0-255 are
     * reserved for Template FlowSets, Options FlowSets, and other
     * reserved FlowSets yet to be created.
     *
     * @return list of flowsets
     */
    @Override
    public TemplateId getTemplateId() {
        return this.templateId;
    }

    /**
     * Returns type of data flow.
     *
     * @return type of data flow
     */
    public List<Flow> getFlows() {
        return flows;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.templateId);
        hash = 29 * hash + Objects.hashCode(this.flows);
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
        final DataFlowRecord other = (DataFlowRecord) obj;
        if (!Objects.equals(this.templateId, other.templateId)) {
            return false;
        }
        return Objects.equals(this.flows, other.flows);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("templateId", templateId)
                .add("flows", flows)
                .toString();
    }

    /**
     * Deserializer function for data flow record.
     *
     * @return deserializer function
     */
    public static DataDeserializer<DataFlowRecord> deserializer() {
        return (data, offset, length, template) -> {
            ByteBuffer bb = ByteBuffer.wrap(data, offset, length);

            Predicate<FlowTemplateField> isValidTemplate = t
                    -> Objects.nonNull(t) && Objects.nonNull(t.getFlowField()) && t.getLength() > 0;

            BiPredicate<ByteBuffer, Integer> isValidBuffer = (b, l)
                    -> b.hasRemaining() && b.remaining() >= l;

            Function<FlowTemplateField, Flow> parser = (f) -> {

                if (!isValidTemplate.test(f) && isValidBuffer.test(bb, f.getLength())) {
                    throw new IllegalStateException("Invalid data set");
                }
                return new Flow.Builder()
                        .field(f.getFlowField())
                        .value(f.getFlowField().getParser().apply(bb, f.getLength()))
                        .build();

            };
            DataTemplateRecord templateRecord = (DataTemplateRecord) template;
            Builder builder = new Builder()
                    .templateId(templateRecord.getTemplateId());
            long count = templateRecord.getFields().stream()
                    .filter(Objects::nonNull)
                    .map(t -> builder.flow(parser.apply(t)))
                    .count();

            if (count != templateRecord.getFiledCount()) {
                throw new IllegalStateException("Invalid parsing fields");
            }
            return builder.build();
        };
    }

    /**
     * Builder for data flow record.
     */
    private static class Builder {

        private TemplateId templateId;

        private List<Flow> flows = new LinkedList<>();

        /**
         * Setter for unique template ID.
         *
         * @param templateId template id.
         * @return this class builder.
         */
        public Builder templateId(TemplateId templateId) {
            this.templateId = templateId;
            return this;
        }

        /**
         * Setter for data flow.
         *
         * @param flow data flow.
         * @return this class builder.
         */
        public Builder flow(Flow flow) {
            this.flows.add(flow);
            return this;
        }

        /**
         * Setter for list of data flow.
         *
         * @param flows list of data flow.
         * @return this class builder.
         */
        public Builder flows(List<Flow> flows) {
            this.flows = flows;
            return this;
        }

        /**
         * Checks arguments for data flow record.
         */
        private void checkArguments() {
            checkNotNull(templateId, "TemplateId cannot be null");
        }

        /**
         * Builds data flow record.
         *
         * @return data flow record.
         */
        public DataFlowRecord build() {
            checkArguments();
            return new DataFlowRecord(this);
        }

    }

}
