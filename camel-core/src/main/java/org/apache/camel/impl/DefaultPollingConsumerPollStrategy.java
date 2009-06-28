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

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.PollingConsumerPollStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A default implementation that just logs a <tt>WARN</tt> level log in case of rollback.
 *
 * @version $Revision$
 */
public class DefaultPollingConsumerPollStrategy implements PollingConsumerPollStrategy {

    private static final transient Log LOG = LogFactory.getLog(DefaultPollingConsumerPollStrategy.class);

    public void begin(Consumer consumer, Endpoint endpoint) {
        // noop
    }

    public void commit(Consumer consumer, Endpoint endpoint) {
        // noop
    }

    public void rollback(Consumer consumer, Endpoint endpoint, Exception e) throws Exception {
        LOG.warn("Consumer " + consumer +  " could not poll endpoint: " + endpoint.getEndpointUri() + " caused by: " + e.getMessage(), e);
    }

}