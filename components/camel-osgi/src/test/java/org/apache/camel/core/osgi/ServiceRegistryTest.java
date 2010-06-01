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
package org.apache.camel.core.osgi;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.osgi.CamelContextFactory;
import org.apache.camel.osgi.CamelContextFactoryBean;
import org.apache.camel.osgi.test.MyService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceRegistryTest extends CamelOsgiTestSupport {

    @Test
    public void camelContextFactoryServiceRegistryTest() throws Exception {
        CamelContextFactory factory = new CamelContextFactory();
        factory.setBundleContext(getBundleContext());
        DefaultCamelContext context = factory.createContext();
        context.start();
        MyService myService = context.getRegistry().lookup(MyService.class.getName(), MyService.class);
        assertNotNull("MyService should not be null", myService);
        
        Object service = context.getRegistry().lookup(MyService.class.getName());
        assertNotNull("MyService should not be null", service);
        
        service = context.getRegistry().lookupByType(MyService.class);
        assertNotNull("MyService should not be null", service);
        context.stop();
    }
    
    @Test
    public void camelContextFactoryBeanServiceRegistryTest() throws Exception {
        CamelContextFactoryBean factoryBean = new CamelContextFactoryBean();
        factoryBean.setBundleContext(getBundleContext());
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/apache/camel/osgi/camelContext.xml");
        factoryBean.setApplicationContext(applicationContext);
        DefaultCamelContext context = factoryBean.getContext();
        context.start();
        MyService myService = context.getRegistry().lookup(MyService.class.getName(), MyService.class);
        assertNotNull("MyService should not be null", myService);
        
        Object service = context.getRegistry().lookup(MyService.class.getName());
        assertNotNull("MyService should not be null", service);
        
        service = context.getRegistry().lookupByType(MyService.class);
        assertNotNull("MyService should not be null", service);
        
        context.stop();
    }

}