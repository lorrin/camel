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
package org.apache.camel.component.file.remote;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.junit.Before;
import org.junit.Test;

/**
 * @version 
 */
public class FtpProducerRootFileExistFailTest extends FtpServerTestSupport {

    private String getFtpUrl() {
        return "ftp://admin@localhost:" + getPort() + "?password=admin&fileExist=Fail";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // create existing file on ftp server
        template.sendBodyAndHeader(getFtpUrl(), "Hello World", Exchange.FILE_NAME, "hello.txt");
    }

    @Test
    public void testFail() throws Exception {
        try {
            template.sendBodyAndHeader(getFtpUrl(), "Bye World", Exchange.FILE_NAME, "hello.txt");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            GenericFileOperationFailedException cause = assertIsInstanceOf(GenericFileOperationFailedException.class, e.getCause());
            assertEquals("File already exist: hello.txt. Cannot write new file.", cause.getMessage());
        }

        // root file should still exist
        assertFileExists(FTP_ROOT_DIR + "hello.txt");
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }
}