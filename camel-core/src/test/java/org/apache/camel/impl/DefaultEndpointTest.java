package org.apache.camel.impl;

import org.apache.camel.ContextTestSupport;

/**
 * @version $Revision$
 */
public class DefaultEndpointTest extends ContextTestSupport {
    public void testSanitizeUri() {
        assertNull(DefaultEndpoint.sanitizeUri(null));
        assertEquals("", DefaultEndpoint.sanitizeUri(""));
        assertSanitizedUriUnchanged("http://camel.apache.org");
        assertSanitizedUriUnchanged("irc://irc.codehaus.org/camel");
        assertSanitizedUriUnchanged("https://issues.apache.org/activemq/secure/AddComment!default.jspa?id=33239");
        assertEquals("ftp://host.mysite.com/records?passiveMode=true&user=someuser&password=******",
                DefaultEndpoint.sanitizeUri("ftp://host.mysite.com/records?passiveMode=true&user=someuser&password=superSecret"));
        assertEquals("sftp://host.mysite.com/records?user=someuser&privateKeyFile=key.file&privateKeyFilePassphrase=******&knownHostsFile=hosts.list",
                DefaultEndpoint.sanitizeUri("sftp://host.mysite.com/records?user=someuser&privateKeyFile=key.file&privateKeyFilePassphrase=superSecret&knownHostsFile=hosts.list"));
    }

    public void assertSanitizedUriUnchanged(String uri) {
        assertEquals(uri, DefaultEndpoint.sanitizeUri(uri));
    }
}
