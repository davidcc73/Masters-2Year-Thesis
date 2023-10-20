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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Channel handler deals with the netfow exporter connection and dispatches messages
 * from netfow exporter to the appropriate locations.
 */
public class NeflowChannelHandler extends SimpleChannelHandler {

    private Channel channel;

    /**
     * Create a new netflow channelHandler instance.
     */
    NeflowChannelHandler() {
    }

    /**
     * Netflow channel connect to netflow exporter.
     *
     * @param ctx   channel handler context
     * @param event channel state event
     * @throws Exception on error while connecting channel
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        channel = event.getChannel();
    }

    /**
     * Netflow channel disconnect to netflow exporter.
     *
     * @param ctx   channel handler context
     * @param event channel state event
     * @throws Exception on error while disconnecting channel
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        channel = event.getChannel();
    }

    /**
     * Netflow channel exception to netflow exporter.
     *
     * @param ctx   channel handler context
     * @param event channel exception event
     * @throws Exception on error while parsing exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) throws Exception {
        //TODO exception handler
    }

    /**
     * Netflow message receive from netflow exporter.
     *
     * @param ctx   channel handler context
     * @param event channel message event
     * @throws Exception on error while parsing exception
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        //TODO Netflow message store to netflow distributed store
        NetFlowPacket packet = (NetFlowPacket) event.getMessage();
    }

}
