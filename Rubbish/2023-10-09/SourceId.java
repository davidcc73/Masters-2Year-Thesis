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

import java.util.Objects;
import com.google.common.base.MoreObjects;

/**
 * SourceId is that identifies the Exporter Observation Domain.
 * NetFlow Collectors SHOULD use the combination of the source IP
 * address and the Source ID field to separate different export
 * streams originating from the same Exporter.
 * Ref: https://www.ietf.org/rfc/rfc3954.txt
 */
public class SourceId {

    private String sourceIp;

    private int id;

    public SourceId(int id, String sourceIp) {
        this.sourceIp = sourceIp;
        this.id = id;
    }

    public SourceId(int id) {
        this.id = id;
    }

    /**
     * Returns exporter ip address.
     *
     * @return exporter ip address.
     */
    public String getSourceIp() {
        return sourceIp;
    }

    /**
     * Returns exporter unique id.
     *
     * @return exporter unique id.
     */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.sourceIp);
        hash = 23 * hash + this.id;
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
        final SourceId other = (SourceId) obj;
        if (this.id != other.id) {
            return false;
        }
        return Objects.equals(this.sourceIp, other.sourceIp);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("sourceIp", sourceIp)
                .add("id", id)
                .toString();
    }

}
