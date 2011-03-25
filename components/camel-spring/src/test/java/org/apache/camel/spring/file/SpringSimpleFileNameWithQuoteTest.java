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
package org.apache.camel.spring.file;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.TestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

/**
 * @version 
 */
@ContextConfiguration
public class SpringSimpleFileNameWithQuoteTest extends AbstractJUnit38SpringContextTests {
    protected String expectedBody = "Hello World!";
    @Autowired
    protected ProducerTemplate template;
    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    public void testMocksAreValid() throws Exception {
        result.expectedBodiesReceived(expectedBody);
        result.expectedHeaderReceived("foo", "\"hello.txt\" abc");

        template.sendBodyAndHeader("file:target/foo", expectedBody, Exchange.FILE_NAME, "hello.txt");

        result.assertIsSatisfied();
    }

    @Override
    protected void setUp() throws Exception {
        TestSupport.deleteDirectory("target/foo");
        super.setUp();
    }
}