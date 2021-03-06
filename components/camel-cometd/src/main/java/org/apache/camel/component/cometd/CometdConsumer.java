/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.cometd;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultMessage;
import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.server.AbstractBayeux;
import org.cometd.server.BayeuxService;

/**
 * A Consumer for receiving messages using Cometd and Bayeux protocol.
 * 
 * @version 
 */
public class CometdConsumer extends DefaultConsumer implements CometdProducerConsumer {

    private AbstractBayeux bayeux;
    private final CometdEndpoint endpoint;
    private ConsumerService service;

    public CometdConsumer(CometdEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    public void start() throws Exception {
        super.start();
        endpoint.connect(this);
        service = new ConsumerService(endpoint.getPath(), bayeux, this);
    }

    @Override
    public void stop() throws Exception {
        endpoint.disconnect(this);
        super.stop();
    }

    public void setBayeux(AbstractBayeux bayeux) {
        this.bayeux = bayeux;
    }

    public CometdEndpoint getEndpoint() {
        return endpoint;
    }

    public static class ConsumerService extends BayeuxService {

        private final CometdEndpoint endpoint;
        private final CometdConsumer consumer;

        public ConsumerService(String channel, Bayeux bayeux, CometdConsumer consumer) {
            super(bayeux, channel);
            this.consumer = consumer;
            this.endpoint = consumer.getEndpoint();
            subscribe(channel, "push");
        }

        public void push(Client client, Object data) throws Exception {
            Message message = new DefaultMessage();
            message.setBody(data);

            Exchange exchange = endpoint.createExchange();
            exchange.setIn(message);

            consumer.getProcessor().process(exchange);
        }
    }
}