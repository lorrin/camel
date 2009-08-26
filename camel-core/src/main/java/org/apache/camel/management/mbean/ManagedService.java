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
package org.apache.camel.management.mbean;

import org.apache.camel.CamelContext;
import org.apache.camel.Service;
import org.apache.camel.impl.ServiceSupport;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description = "Managed Service")
public class ManagedService {

    private CamelContext context;
    private Service service;

    public ManagedService(CamelContext context, Service service) {
        this.context = context;
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public CamelContext getContext() {
        return context;
    }

    @ManagedAttribute(description = "Service running state")
    public boolean isStarted() {
        if (service instanceof ServiceSupport) {
            return ((ServiceSupport) service).isStarted();
        }
        throw new IllegalStateException("The managed service does not support running state, is type: " + service.getClass().getName());
    }

    @ManagedOperation(description = "Start Service")
    public void start() throws Exception {
        service.start();
    }

    @ManagedOperation(description = "Stop Service")
    public void stop() throws Exception {
        service.stop();
    }
}