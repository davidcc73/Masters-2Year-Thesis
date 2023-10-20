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

import java.util.List;

import org.onlab.packet.Deserializer;


/**
 * The Options Template Record (and its corresponding Options Data
 * Record) is used to supply information about the NetFlow process
 * configuration or NetFlow process specific data, rather than supplying
 * information about IP Flows.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public class OptionalTemplateFlowSet extends FlowSet {

    /*
        0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |       FlowSet ID = 1          |          Length               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |         Template ID           |      Option Scope Length      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |        Option Length          |       Scope 1 Field Type      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |     Scope 1 Field Length      |               ...             |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |     Scope N Field Length      |      Option 1 Field Type      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |     Option 1 Field Length     |             ...               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |     Option M Field Length     |           Padding             |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    private int flowSetId;

    private int length;

    private List<DataTemplateRecord> records;

    /**
     * Returns flowset type.
     *
     * @return flowset type
     */
    @Override
    public Type getType() {
        return Type.OPTIONAL_TEMPLATE_FLOWSET;
    }

    /**
     * Returns flowset id.
     * FlowSet ID value of 1 is reserved for the Options Template.
     *
     * @return flow set ID
     */
    public int getFlowSetId() {
        return flowSetId;
    }

    /**
     * Returns total length of this FlowSet.
     * Each Options Template FlowSet
     * MAY contain multiple Options Template Records.  Thus, the
     * Length value MUST be used to determine the position of the next
     * FlowSet record, which could be either a Template FlowSet or
     * Data FlowSet.
     *
     * @return flow set ID
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns list of optional data template records.
     *
     * @return list of optional data template records
     */
    public List<DataTemplateRecord> getRecords() {
        return records;
    }

    /**
     * Deserializer function for data option template flowset.
     *
     * @return data deserializer function
     */
    public static Deserializer<OptionalTemplateFlowSet> deserializer() {
        return (data, offset, length) -> {
            //TODO parse optional template
            return new OptionalTemplateFlowSet();
        };
    }

}
