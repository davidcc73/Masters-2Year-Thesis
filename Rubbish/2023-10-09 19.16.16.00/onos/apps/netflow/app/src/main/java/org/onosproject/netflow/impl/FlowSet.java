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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.onlab.packet.BasePacket;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Deserializer;

/**
 * FlowSet is a generic term for a collection of Flow Records that have.
 * a similar structure.  In an Export Packet, one or more FlowSets
 * follow the Packet Header.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public abstract class FlowSet extends BasePacket {

    public static final int FLOW_SET_HEADER_LENTH = 4;

    public static final int FIELD_LENTH = 4;

    public static final int RECORD_HEADER_LENGTH = 4;

    /**
     * FlowSets type
     * FlowSets: Template FlowSet, Options Template FlowSet, and Data FlowSet.
     */
    public enum Type {

        TEMPLATE_FLOWSET(0, TemplateFlowSet.deserializer()),
        OPTIONAL_TEMPLATE_FLOWSET(1, OptionalTemplateFlowSet.deserializer()),
        DATA_FLOWSET(Integer.MAX_VALUE, DataFlowSet.deserializer());

        private final int flowSetId;
        private final Deserializer deserializer;

        Type(int flowSetId, Deserializer deserializer) {
            this.flowSetId = flowSetId;
            this.deserializer = deserializer;
        }

        private static Map<Integer, Type> parser = new ConcurrentHashMap<>();

        static {
            Arrays.stream(Type.values()).forEach(type -> parser.put(type.flowSetId, type));
        }

        public static Type getType(int flowSetId) throws DeserializationException {
            if (flowSetId < 0) {
                throw new DeserializationException("Invalid trap type");
            }
            return Optional.of(flowSetId)
                    .filter(id -> parser.containsKey(id))
                    .map(id -> parser.get(id))
                    .orElse(DATA_FLOWSET);
        }

        public Deserializer getDecoder() {
            return this.deserializer;
        }

    }

    public abstract Type getType();

    public abstract int getFlowSetId();

    public abstract int getLength();

    @Override
    public byte[] serialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
