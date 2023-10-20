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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Flow template fields.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public final class FlowTemplateField {

    /*
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 1           |         Field Length 1        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Field Type 2           |         Field Length 2        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private FlowField flowField;

    private int length;

    private FlowTemplateField(Builder builder) {
        this.flowField = builder.flowField;
        this.length = builder.length;
    }

    /**
     * Returns a numeric value that represents the type of the field.
     *
     * @return flow field
     */
    public FlowField getFlowField() {
        return flowField;
    }

    /**
     * Returns length of the corresponding Field Type, in bytes.
     *
     * @return flow value length
     */
    public int getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.flowField);
        hash = 97 * hash + this.length;
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
        final FlowTemplateField other = (FlowTemplateField) obj;
        if (this.length != other.length) {
            return false;
        }
        return this.flowField == other.flowField;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("flowField", flowField)
                .add("length", length)
                .toString();
    }


    public static class Builder {
        private FlowField flowField;

        private int length;

        /**
         * Setter for flowfield.
         *
         * @param fieldId flow field.
         * @return this class builder.
         */
        public Builder flowField(int fieldId) {
            this.flowField = FlowField.getField(fieldId)
                    .orElseThrow(() -> new RuntimeException("Unsupported flow field"));
            return this;
        }

        /**
         * Setter for flow template length.
         *
         * @param length flow template length.
         * @return this class builder.
         */
        public Builder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * Checks arguments for flow template field.
         */
        private void checkArguments() {
            checkState(length != 0, "Flow length can be zero.");
            checkNotNull(flowField, "Flow field can not be null.");
        }

        /**
         * Builds flow template field.
         *
         * @return flow template field.
         */
        public FlowTemplateField build() {
            checkArguments();
            return new FlowTemplateField(this);
        }
    }
}
