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

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.util.ObjectHelper;

/**
 * A {@link Registry} which delegates to the real registry.
 * <p/>
 * This is used to ensure that Camel performs property placeholder resolution on every lookup.
 *
 * @version 
 */
public class PropertyPlaceholderDelegateRegistry implements Registry {

    private final CamelContext context;
    private final Registry delegate;

    public PropertyPlaceholderDelegateRegistry(CamelContext context, Registry delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    public Object lookup(String name) {
        try {
            name = context.resolvePropertyPlaceholders(name);
            return delegate.lookup(name);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }

    public <T> T lookup(String name, Class<T> type) {
        try {
            name = context.resolvePropertyPlaceholders(name);
            return delegate.lookup(name, type);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }

    public <T> Map<String, T> lookupByType(Class<T> type) {
        return delegate.lookupByType(type);
    }

    public Registry getRegistry() {
        return delegate;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
