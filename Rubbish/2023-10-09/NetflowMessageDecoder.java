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


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decode an netflow message from a Channel, for use in a netty pipeline.
 */
public class NetflowMessageDecoder extends FrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(NetflowMessageDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

        try {
            if (buffer.readableBytes() > 0) {
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                ctx.setAttachment(null);
                return NetFlowPacket.deserializer().deserialize(bytes, 0, bytes.length);
            }
        } catch (Exception e) {
            log.error("Netflow message decode error");
            buffer.resetReaderIndex();
            buffer.discardReadBytes();

        }
        return null;
    }

}
