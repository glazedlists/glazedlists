/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.xmlbrowser;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import com.publicobject.misc.xml.SaxParserSidekick;

/**
 * Convert an XML input stream into an EventList of Elements.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class EventListXmlContentHandler extends DefaultHandler {

    private final EventList<Tag> target;
    private final List<Tag> stack = new ArrayList<>();

    public EventListXmlContentHandler(EventList<Tag> target) {
        this.target = GlazedLists.threadSafeList(target);
    }

    public EventList<Tag> parse(InputStream inputStream) {
        try {
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            SaxParserSidekick.install(xmlReader);

            EventListXmlContentHandler handler = new EventListXmlContentHandler(target);
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(inputStream));

            return handler.target;
        } catch(IOException e) {
            throw new RuntimeException(e);
        } catch(SAXException e) {
            throw new RuntimeException(e);
        } catch(ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        Tag child;
        if(!stack.isEmpty()) {
            Tag last = stack.get(stack.size() - 1);
            child = last.createChild(qName);

        } else {
            child = new Tag(qName);
        }

        target.add(child);
        stack.add(child);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        stack.remove(stack.size() - 1);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        Tag last = stack.get(stack.size() - 1);
        last.append(new String(ch, start, length));
    }
}
