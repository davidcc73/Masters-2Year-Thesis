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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.MoreObjects;

import org.onlab.packet.Deserializer;
import org.onosproject.netflow.TemplateId;
import org.onosproject.netflow.FlowTemplateField;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Template Record defines the structure and interpretation.
 * of fields in an Options Data Record, including defining the scope
 * within which the Options Data Record is relevant.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public final class DataTemplateRecord extends TemplateRecord {

    /*
        0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |      Template ID 256          |         Field Count           |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 1           |         Field Length 1        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 2           |         Field Length 2        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private TemplateId templateId;

    private int filedCount;

    private List<FlowTemplateField> fields;

    private DataTemplateRecord(Builder builder) {
        this.templateId = builder.templateId;
        this.filedCount = builder.filedCount;
        this.fields = builder.fields;
    }

    /**
     * Returns template record's template id.
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
        return templateId;
    }

    /**
     * Returns number of fields in this Template Record.
     *
     * @return field count
     */
    public int getFiledCount() {
        return filedCount;
    }

    /**
     * Returns list of flow template fields.
     *
     * @return list of flow template fields
     */
    public List<FlowTemplateField> getFields() {
        return fields;
    }

    public int getValueLength() {
        Optional.ofNullable(fields)
                .orElseThrow(() -> new IllegalStateException("Invalid fields"));
        return fields.stream()
                .filter(Objects::nonNull)
                .map(FlowTemplateField::getLength)
                .collect(Collectors.summingInt(Integer::intValue));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("templateId", templateId)
                .add("filedCount", filedCount)
                .add("fields", fields)
                .toString();
    }

    /**
     * Data deserializer function for data template record.
     *
     * @return data deserializer function
     */
    public static Deserializer<DataTemplateRecord> deserializer() {
        return (data, offset, length) -> {
            Predicate<ByteBuffer> isValidBuffer = b -> b.remaining() < FlowSet.FIELD_LENTH;
            Function<ByteBuffer, FlowTemplateField> parse = (b)
                    -> {
                if (isValidBuffer.test(b)) {
                    throw new IllegalStateException("Invalid buffersize");
                }
                return new FlowTemplateField.Builder()
                        .flowField(b.getShort())
                        .length(b.getShort())
                        .build();
            };

            Builder builder = new Builder();
            ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            if (isValidBuffer.test(bb)) {
                throw new IllegalStateException("Invalid buffersize");
            }
            builder.templateId(bb.getShort())
                    .filedCount(bb.getShort());
            IntStream.rangeClosed(1, builder.filedCount).forEach(i -> builder.templateField(parse.apply(bb)));
            return builder.build();
        };
    }

    /**
     * Builder for data template record.
     */
    private static class Builder {

        private TemplateId templateId;

        private int filedCount;

        private List<FlowTemplateField> fields = new LinkedList<>();

        /**
         * Setter for template record's template id.
         *
         * @param templateId template record's template id.
         * @return this class builder.
         */
        public Builder templateId(int templateId) {
            this.templateId = new TemplateId((templateId));
            return this;
        }

        /**
         * Setter for number of fields in this Template Record.
         *
         * @param filedCount number of fields in this Template Record.
         * @return this class builder.
         */
        public Builder filedCount(int filedCount) {
            this.filedCount = filedCount;
            return this;
        }

        /**
         * Setter for list of flow template fields.
         *
         * @param fields list of flow template fields.
         * @return this class builder.
         */
        public Builder templateFields(List<FlowTemplateField> fields) {
            this.fields = fields;
            return this;
        }

        /**
         * Setter for  flow template fields.
         *
         * @param field flow template fields.
         * @return this class builder.
         */
        public Builder templateField(FlowTemplateField field) {
            this.fields.add(field);
            return this;
        }

        /**
         * Checks arguments for data template record.
         */
        private void checkArguments() {
            checkState(filedCount != 0, "Invalid template filed count.");
            checkNotNull(templateId, "Template Id cannot be null.");

        }

        /**
         * Builds data template record.
         *
         * @return data template record.
         */
        public DataTemplateRecord build() {
            checkArguments();
            return new DataTemplateRecord(this);
        }

    }

}
