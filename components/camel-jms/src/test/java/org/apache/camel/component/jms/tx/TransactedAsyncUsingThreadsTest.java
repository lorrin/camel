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
package org.apache.camel.component.jms.tx;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.WaitForTaskToComplete;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class TransactedAsyncUsingThreadsTest extends CamelSpringTestSupport {

    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(
                "/org/apache/camel/component/jms/tx/TransactedAsyncUsingThreadsTest.xml");
    }

    protected int getExpectedRouteCount() {
        return 0;
    }

    private static int counter;
    private static String thread1;
    private static String thread2;

    @Before
    public void init() {
        counter = 0;
        thread1 = "";
        thread2 = "";
    }

    @Test
    public void testConsumeAsyncOK() throws Exception {
        counter = 1;

        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:async").expectedMessageCount(1);

        template.sendBody("activemq:queue:foo", "Hello World");

        assertMockEndpointsSatisfied();

        assertNotSame("Should use a different thread when doing async routing", thread1, thread2);
    }

    @Test
    public void testConsumeAsyncFail() throws Exception {
        counter = 0;

        getMockEndpoint("mock:result").expectedMessageCount(1);
        // we need a retry attempt so we get 2 messages
        getMockEndpoint("mock:async").expectedMessageCount(2);

        // the 1st message is the original message
        getMockEndpoint("mock:async").message(0).header("JMSRedelivered").isEqualTo(false);

        // the 2nd message is the redelivered by the JMS broker
        getMockEndpoint("mock:async").message(1).header("JMSRedelivered").isEqualTo(true);

        template.sendBody("activemq:queue:foo", "Bye World");

        assertMockEndpointsSatisfied();

        assertNotSame("Should use a different thread when doing async routing", thread1, thread2);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:foo")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            thread1 = Thread.currentThread().getName();
                        }
                    })
                    // use transacted routing
                    .transacted()
                    // use async threads to process the exchange from this point forward
                    // but let the consumer wait until the async routing is complete
                    // so we can let the transaction commit or rollback depending how it went
                    .threads(5).waitForTaskToComplete(WaitForTaskToComplete.Always)
                    // send to mock for verification
                    .to("mock:async")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            thread2 = Thread.currentThread().getName();

                            if (counter++ == 0) {
                                // simulate error so we can test rollback and have the JMS broker
                                // do redelivery
                                throw new IllegalAccessException("Damn");
                            }
                        }
                    }).to("mock:result");

            }
        };
    }


}