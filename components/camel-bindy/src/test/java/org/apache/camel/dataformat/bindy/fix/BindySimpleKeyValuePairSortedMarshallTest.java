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
package org.apache.camel.dataformat.bindy.fix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.kvp.BindyKeyValuePairDataFormat;
import org.apache.camel.dataformat.bindy.model.fix.sorted.body.Order;
import org.apache.camel.dataformat.bindy.model.fix.sorted.header.Header;
import org.apache.camel.dataformat.bindy.model.fix.sorted.trailer.Trailer;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.test.JavaConfigContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = "org.apache.camel.dataformat.bindy.fix.BindySimpleKeyValuePairSortedMarshallTest$ContextConfig", loader = JavaConfigContextLoader.class)
public class BindySimpleKeyValuePairSortedMarshallTest extends AbstractJUnit4SpringContextTests {

    private static final transient Log LOG = LogFactory.getLog(BindySimpleKeyValuePairSortedMarshallTest.class);

    private List<Map<String, Object>> models = new ArrayList<Map<String, Object>>();
    private String result = "8=FIX 4.19=2035=034=149=INVMGR56=BRKR1=BE.CHM.00122=411=CHM0001-0148=BE000124567854=158=this is a camel - bindy test10=220\r\n";

    @Produce(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint resultEndpoint;

    @Test
    public void testMarshallMessage() {

        resultEndpoint.expectedBodiesReceived(result);
        template.sendBody(generateModel());

        try {
            resultEndpoint.assertIsSatisfied();
        } catch (InterruptedException e) {
            LOG.error("Unit test error : ", e);
        }
    }

    public List<Map<String, Object>> generateModel() {
        Map<String, Object> modelObjects = new HashMap<String, Object>();

        Header header = new Header();
        header.setBeginString("FIX 4.1");
        header.setBodyLength(20);
        header.setMsgSeqNum(1);
        header.setMsgType("0");
        header.setSendCompId("INVMGR");
        header.setTargetCompId("BRKR");

        Trailer trailer = new Trailer();
        trailer.setCheckSum(220);

        Order order = new Order();
        order.setAccount("BE.CHM.001");
        order.setClOrdId("CHM0001-01");
        order.setIDSource("4");
        order.setSecurityId("BE0001245678");
        order.setSide("1");
        order.setText("this is a camel - bindy test");

        order.setHeader(header);
        order.setTrailer(trailer);

        modelObjects.put(order.getClass().getName(), order);
        modelObjects.put(header.getClass().getName(), header);
        modelObjects.put(trailer.getClass().getName(), trailer);

        models.add(modelObjects);
        return models;
    }

    @Configuration
    public static class ContextConfig extends SingleRouteCamelConfiguration {
        BindyKeyValuePairDataFormat camelDataFormat = new BindyKeyValuePairDataFormat(
            "org.apache.camel.dataformat.bindy.model.fix.sorted");

        @Override
        @Bean
        public RouteBuilder route() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct:start").marshal(camelDataFormat).to("mock:result");
                }
            };
        }
    }
}