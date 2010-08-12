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
package org.apache.camel.impl;

import junit.framework.TestCase;

public class ActiveMQUuidGeneratorTest extends TestCase {
    
    private static final String PATTERN = "^ID-.*/\\d{4,5}-\\d{13}/\\d{1}-\\d{1}$";
    private ActiveMQUuidGenerator uuidGenerator;

    public void setUp() throws Exception {
        uuidGenerator = new ActiveMQUuidGenerator();
    }

    public void testGenerateUUID() {
        String firstUUID = uuidGenerator.generateUuid();
        String secondUUID = uuidGenerator.generateUuid();

        assertTrue(firstUUID.matches(PATTERN));
        assertTrue(secondUUID.matches(PATTERN));
        assertFalse(firstUUID.equals(secondUUID));
    }
}