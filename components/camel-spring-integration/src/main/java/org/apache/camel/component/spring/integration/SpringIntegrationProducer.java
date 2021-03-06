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
package org.apache.camel.component.spring.integration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.util.ObjectHelper;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;

/**
 * A producer of exchanges for the Spring Integration
 * Please specify the outputChannel in the endpoint url for this producer.
 * If the message pattern is inOut, the inputChannel property
 * should be set for receiving the response message.
 * @version 
 */
public class SpringIntegrationProducer extends DefaultProducer implements Processor {    
    private final ChannelResolver channelResolver;
    private DirectChannel inputChannel;
    private MessageChannel outputChannel;

    public SpringIntegrationProducer(SpringCamelContext context, SpringIntegrationEndpoint endpoint) {
        super(endpoint);
        this.channelResolver = new BeanFactoryChannelResolver(context.getApplicationContext());
    }

    @Override
    public SpringIntegrationEndpoint getEndpoint() {
        return (SpringIntegrationEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (getEndpoint().getMessageChannel() == null) {
            String outputChannelName = getEndpoint().getDefaultChannel();
            if (ObjectHelper.isEmpty(outputChannelName)) {
                outputChannelName = getEndpoint().getInputChannel();
            }

            ObjectHelper.notEmpty(outputChannelName, "OutputChannelName", getEndpoint());
            outputChannel = channelResolver.resolveChannelName(outputChannelName);
        } else {
            outputChannel = getEndpoint().getMessageChannel();
        }

        if (outputChannel == null) {
            throw new IllegalArgumentException("Cannot resolve OutputChannel on " + getEndpoint());
        }

        // if we do in-out we need to setup the input channel as well
        if (getEndpoint().isInOut()) {
            // we need to setup right inputChannel for further processing
            ObjectHelper.notEmpty(getEndpoint().getInputChannel(), "InputChannel", getEndpoint());
            inputChannel = (DirectChannel)channelResolver.resolveChannelName(getEndpoint().getInputChannel());

            if (inputChannel == null) {
                throw new IllegalArgumentException("Cannot resolve InputChannel on " + getEndpoint());
            }
        }
    }

    public void process(final Exchange exchange) throws Exception {
        if (exchange.getPattern().isOutCapable()) {

            // we want to do in-out so the inputChannel is mandatory (used to receive reply from spring integration)
            if (inputChannel == null) {
                throw new IllegalArgumentException("InputChannel has not been configured on " + getEndpoint());
            }
            exchange.getIn().getHeaders().put(MessageHeaders.REPLY_CHANNEL , inputChannel);

            // subscribe so we can receive the reply from spring integration
            inputChannel.subscribe(new MessageHandler() {
                public void handleMessage(Message<?> message) {
                    if (log.isDebugEnabled()) {
                        log.debug("Received " + message + " from InputChannel: " + inputChannel);
                    }
                    SpringIntegrationBinding.storeToCamelMessage(message, exchange.getOut());
                }
            });
        }
        org.springframework.integration.Message siOutmessage = SpringIntegrationBinding.createSpringIntegrationMessage(exchange);

        // send the message to spring integration
        if (log.isDebugEnabled()) {
            log.debug("Sending " + siOutmessage + " to OutputChannel: " + outputChannel);
        }
        outputChannel.send(siOutmessage);
    }

}
