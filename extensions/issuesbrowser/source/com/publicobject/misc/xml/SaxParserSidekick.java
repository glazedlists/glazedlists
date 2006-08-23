/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import org.xml.sax.*;

import java.io.ByteArrayInputStream;

/**
 * ParserSidekick performs various services for the SaxParser.
 * It skips DTD validation for a significant performance boost.
 *
 * @see <a href="http://forum.java.sun.com/thread.jspa?forumID=34&threadID=284209">Java Forums</a>
 */
public final class SaxParserSidekick implements EntityResolver, ErrorHandler {

    private SaxParserSidekick() {
        // prevent instantiation
    }

    public static void install(XMLReader xmlReader) {
        SaxParserSidekick parserSidekick = new SaxParserSidekick();
        xmlReader.setEntityResolver(parserSidekick);
        xmlReader.setErrorHandler(parserSidekick);
    }

    /**
     * Don't fetch a DTD from a remote webserver.
     */
    public InputSource resolveEntity(String publicId, String systemId) {
        // skip the DTD
        if(systemId.endsWith("issuezilla.dtd")) {
            byte[] emptyDTDBytes = "<?xml version='1.0' encoding='UTF-8'?>".getBytes();
            return new InputSource(new ByteArrayInputStream(emptyDTDBytes));
        } else {
            return null;
        }
    }
    public void error(SAXParseException exception) {
        System.out.println("Sax error, \"" + exception.getMessage() + "\"");
    }
    public void fatalError(SAXParseException exception) {
        System.out.println("Sax fatal error, \"" + exception.getMessage() + "\"");
    }
    public void warning(SAXParseException exception) {
        System.out.println("Sax warning, \"" + exception.getMessage() + "\"");
    }
}