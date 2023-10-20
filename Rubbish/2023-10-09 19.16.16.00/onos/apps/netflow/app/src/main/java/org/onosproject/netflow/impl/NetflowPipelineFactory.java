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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;


/**
 * Creates a ChannelPipeline for a server-side netflow message channel.
 */
public class NetflowPipelineFactory implements ChannelPipelineFactory {

    /**
     * Constructor to initialize the values.
     */
    public NetflowPipelineFactory() {
        super();
    }

    /**
     * Get server-side pipe line channel.
     *
     * @return ChannelPipeline server-side pipe line channel
     * @throws Exception on while getting pipe line
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        NeflowChannelHandler handler = new NeflowChannelHandler();

        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("netflowmessagedecoder", new NetflowMessageDecoder());
        pipeline.addLast("ActiveHandler", handler);
        return pipeline;
    }

}

