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

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Flow is a collection of Flow Data Record(s).
 * each containing a set of field values.  The Type and
 * Length of the fields have been previously defined in the
 * Template Record referenced by the FlowSet ID or Template ID.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public final class Flow {

    /*
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |   Record 1 - Field Value 1    |   Record 1 - Field Value 2    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private FlowField field;

    private Object value;

    private Flow(Builder builder) {
        this.field = builder.field;
        this.value = builder.value;
    }

    /**
     * The getter for flow fields.
     *
     * @return flow field
     */
    public FlowField getField() {
        return field;
    }

    /**
     * Returns flow value.
     *
     * @return flow value
     */
    public Object getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.field);
        hash = 23 * hash + Objects.hashCode(this.value);
        return hash;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        final Flow other = (Flow) obj;
        if (this.field != other.field) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("field", field)
                .add("value", value)
                .toString();
    }

    /**
     * Flow data value builder.
     */
    public static class Builder {

        private FlowField field;

        private Object value;

        /**
         * Setter for flow fields.
         *
         * @param field flow field.
         * @return this class builder.
         */
        public Builder field(FlowField field) {
            this.field = field;
            return this;
        }

        /**
         * Setter for flow data value.
         *
         * @param value flow data value.
         * @return this class builder.
         */
        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        /**
         * Checks arguments for flow data value.
         */
        private void checkArguments() {
            checkNotNull(field, "flow field cannot be null");
            checkNotNull(value, "value cannot be null");
        }

        /**
         * Builds data flow.
         *
         * @return data flow object.
         */
        public Flow build() {
            checkArguments();
            return new Flow(this);
        }

    }

}
