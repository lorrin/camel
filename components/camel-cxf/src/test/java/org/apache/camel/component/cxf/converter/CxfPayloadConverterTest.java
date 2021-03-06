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
package org.apache.camel.component.cxf.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.camel.component.cxf.CxfPayload;
import org.apache.camel.test.junit4.ExchangeTestSupport;
import org.junit.Before;
import org.junit.Test;

public class CxfPayloadConverterTest extends ExchangeTestSupport {
    private Document document;
    private CxfPayload<String[]> payload;
    private FileInputStream inputStream;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        File file = new File("src/test/resources/org/apache/camel/component/cxf/converter/test.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();
        List<Element> body = new ArrayList<Element>();
        body.add(document.getDocumentElement());
        payload = new CxfPayload<String[]>(new ArrayList<String[]>(), body);
        inputStream = new FileInputStream(file);
    }

    @Test
    public void testDocumentToCxfPayload() {
        CxfPayload<String[]> payload = CxfPayloadConverter.documentToCxfPayload(document, exchange);
        assertNotNull(payload);
        assertEquals("Get a wrong size of body", 1, payload.getBody().size());
    }

    @Test
    public void testNodeListToCxfPayload() {
        NodeList nodeList = document.getChildNodes();
        CxfPayload<String[]> payload = CxfPayloadConverter.nodeListToCxfPayload(nodeList, exchange);
        assertNotNull(payload);
        assertEquals("Get a wrong size of body", 1, payload.getBody().size());
    }
    
    @Test
    public void testCxfPayloadToNodeList() {
        NodeList nodeList = CxfPayloadConverter.cxfPayloadToNodeList(payload, exchange);
        assertNotNull(nodeList);
        assertEquals("Get a worng size of nodeList", 1,  nodeList.getLength());
    }
   
    @Test
    public void testToCxfPayload() {
        // use default type converter
        exchange.getIn().setBody(inputStream);
        CxfPayload payload = exchange.getIn().getBody(CxfPayload.class);
        assertTrue(payload instanceof CxfPayload);
        assertEquals("Get a wrong size of body", 1, payload.getBody().size());
    }
    
    @Test
    public void testFromCxfPayload() {
        exchange.getIn().setBody(payload);
        InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        assertTrue(inputStream instanceof InputStream);       
    }

}
