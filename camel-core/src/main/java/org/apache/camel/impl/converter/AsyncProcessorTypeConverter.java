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
package org.apache.camel.impl.converter;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.util.ObjectHelper;

/**
 * A simple converter that can convert any {@link Processor} to an {@link AsyncProcessor}.
 * Processing will still occur synchronously but it will provide the required
 * notifications that the caller expects.
 *
 * @version $Revision$
 */
public class AsyncProcessorTypeConverter implements TypeConverter {

    private static final class ProcessorToAsyncProcessorBridge extends DelegateProcessor implements AsyncProcessor {

        private ProcessorToAsyncProcessorBridge(Processor processor) {
            super(processor);
        }

        public boolean process(Exchange exchange, AsyncCallback callback) {
            if (processor == null) {
                // no processor then we are done
                callback.done(true);
                return true;
            }
            try {
                processor.process(exchange);
            } catch (Throwable e) {
                // must catch throwable so we catch all
                exchange.setException(e);
            } finally {
                // we are bridging a sync processor as async so callback with true
                callback.done(true);
            }
            return true;
        }

        @Override
        public String toString() {
            if (processor != null) {
                return processor.toString();
            } else {
                return "Processor is null";
            }
        }
    }

    public <T> T convertTo(Class<T> type, Object value) {
        if (value != null) {
            if (type.equals(AsyncProcessor.class)) {
                if (value instanceof AsyncProcessor) {
                    return type.cast(value);
                } else if (value instanceof Processor) {
                    // Provide an async bridge to the regular processor.
                    final Processor processor = (Processor)value;
                    return type.cast(new ProcessorToAsyncProcessorBridge(processor));
                }
            }
        }
        return null;
    }

    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {
        return convertTo(type, value);
    }

    public <T> T mandatoryConvertTo(Class<T> type, Object value) throws NoTypeConversionAvailableException {
        return convertTo(type, value);
    }

    public <T> T mandatoryConvertTo(Class<T> type, Exchange exchange, Object value) throws NoTypeConversionAvailableException {
        return convertTo(type, exchange, value);
    }

    public static AsyncProcessor convert(Processor value) {
        if (value instanceof AsyncProcessor) {
            return (AsyncProcessor)value;
        }
        return new ProcessorToAsyncProcessorBridge(value);
    }
}