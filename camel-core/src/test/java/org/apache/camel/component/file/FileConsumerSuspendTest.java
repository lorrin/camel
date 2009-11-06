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
package org.apache.camel.component.file;

import java.io.File;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.RoutePolicySupport;

/**
 * @version $Revision$
 */
public class FileConsumerSuspendTest extends ContextTestSupport {

    public void testConsumeSuspendFile() throws Exception {
        deleteDirectory("target/suspended");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Bye World");

        template.sendBodyAndHeader("file://target/suspended", "Bye World", Exchange.FILE_NAME, "bye.txt");
        template.sendBodyAndHeader("file://target/suspended", "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied();

        Thread.sleep(1000);

        // the route is suspended by the policy so we should only receive one
        File file = new File("target/suspended/hello.txt").getAbsoluteFile();
        assertEquals("The file should exists", true, file.exists());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                MyPolicy myPolicy = new MyPolicy();
                from("file://target/suspended?maxMessagesPerPoll=1&sortBy=file:name")
                    .routePolicy(myPolicy).id("myRoute")
                    .to("mock:result");
            }
        };
    }

    private class MyPolicy extends RoutePolicySupport {

        private int counter;

        public void onExchangeDone(Route route, Exchange exchange) {
            // only stop it at first run
            if (counter++ == 0) {
                try {
                    super.stopConsumer(route.getConsumer());
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }
    }

}