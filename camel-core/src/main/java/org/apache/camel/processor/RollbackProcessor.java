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
package org.apache.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RollbackExchangeException;

/**
 * Processor for marking an {@link org.apache.camel.Exchange} to rollback.
 *
 * @version $Revision$
 */
public class RollbackProcessor implements Processor, Traceable {

    private String message;

    public RollbackProcessor() {
    }

    public RollbackProcessor(String message) {
        this.message = message;
    }

    public void process(Exchange exchange) throws Exception {
        // mark the exchange for rollback
        exchange.setProperty(Exchange.ROLLBACK_ONLY, Boolean.TRUE);
        if (message != null) {
            exchange.setException(new RollbackExchangeException(message, exchange));
        } else {
            exchange.setException(new RollbackExchangeException(exchange));
        }
    }

    @Override
    public String toString() {
        if (message != null) {
            return "Rollback[" + message + "]";
        } else {
            return "Rollback";
        }
    }

    public String getTraceLabel() {
        return "Rollback";
    }
}