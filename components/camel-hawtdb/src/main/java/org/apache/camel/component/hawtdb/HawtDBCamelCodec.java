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
package org.apache.camel.component.hawtdb;

import java.io.IOException;
import java.io.Serializable;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultExchangeHolder;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.DataByteArrayInputStream;
import org.fusesource.hawtbuf.DataByteArrayOutputStream;
import org.fusesource.hawtbuf.codec.Codec;
import org.fusesource.hawtbuf.codec.ObjectCodec;
import org.fusesource.hawtbuf.codec.StringCodec;

/**
 * @version 
 */
public final class HawtDBCamelCodec {

    private Codec<String> keyCodec = new StringCodec();
    private Codec<DefaultExchangeHolder> exchangeCodec = new ObjectCodec<DefaultExchangeHolder>();

    public Buffer marshallKey(String key) throws IOException {
        DataByteArrayOutputStream baos = new DataByteArrayOutputStream();
        keyCodec.encode(key, baos);
        return baos.toBuffer();
    }

    public String unmarshallKey(Buffer buffer) throws IOException {
        DataByteArrayInputStream bais = new DataByteArrayInputStream(buffer);
        String key = keyCodec.decode(bais);
        return key;
    }

    public Buffer marshallExchange(CamelContext camelContext, Exchange exchange) throws IOException {
        DataByteArrayOutputStream baos = new DataByteArrayOutputStream();
        // use DefaultExchangeHolder to marshal to a serialized object
        DefaultExchangeHolder pe = DefaultExchangeHolder.marshal(exchange, false);
        // add the aggregated size property as the only property we want to retain
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_SIZE, exchange.getProperty(Exchange.AGGREGATED_SIZE, Integer.class));
        // add the aggregated completed by property to retain
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_COMPLETED_BY, exchange.getProperty(Exchange.AGGREGATED_COMPLETED_BY, String.class));
        // add the aggregated correlation key property to retain
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_CORRELATION_KEY, exchange.getProperty(Exchange.AGGREGATED_CORRELATION_KEY, String.class));
        // persist the from endpoint as well
        if (exchange.getFromEndpoint() != null) {
            DefaultExchangeHolder.addProperty(pe, "CamelAggregatedFromEndpoint", exchange.getFromEndpoint().getEndpointUri());
        }
        exchangeCodec.encode(pe, baos);
        return baos.toBuffer();
    }

    public Exchange unmarshallExchange(CamelContext camelContext, Buffer buffer) throws IOException {
        DataByteArrayInputStream bais = new DataByteArrayInputStream(buffer);
        DefaultExchangeHolder pe = exchangeCodec.decode(bais);
        Exchange answer = new DefaultExchange(camelContext);
        DefaultExchangeHolder.unmarshal(answer, pe);
        // restore the from endpoint
        String fromEndpointUri = (String) answer.removeProperty("CamelAggregatedFromEndpoint");
        if (fromEndpointUri != null) {
            Endpoint fromEndpoint = camelContext.hasEndpoint(fromEndpointUri);
            if (fromEndpoint != null) {
                answer.setFromEndpoint(fromEndpoint);
            }
        }
        return answer;
    }

}
