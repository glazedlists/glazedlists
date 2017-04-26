/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import ca.odell.glazedlists.EventList;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * This XML Parser greatly simplifies the task of parsing an XML document and
 * inserting the resulting Java Objects into an {@link EventList}. It is
 * architected to be very declarative. The user of the Parser object first
 * configures it with pairs of objects: an {@link XMLTagPath} object which
 * identifies a location within an XML Document, plus {@link PushProcessor} and
 * {@link PopProcessor}s which defines logic capable of processing the data read
 * from the Document at that point.
 *
 * <p>After configuring the Parser with repeated calls to
 * {@link #addProcessor(XMLTagPath, PushProcessor)}, a Document can be parsed using
 * {@link #parse(InputStream, Object)} and the contents of the
 * {@link InputStream} will be inserted into the {@link EventList}.
 *
 * <p>Sample usage on a simple XML Document defining a customer might resemble:
 * <pre>
 *   Parser parser = new Parser();
 *
 *   // create a new Customer object each time the customer start tag is found
 *   XMLTagPath customerTag = new XMLTagPath("customer", XMLTagPath.BODY);
 *   Processor customerStartTag = Processors.createNewObject(Customer.class);
 *   parser.addProcessor(customerTag.start(), customerStartTag);
 *
 *   // add the new Customer object to the target EventList when the end tag is found
 *   Processor customerEndTag = Processors.addObjectToTargetList();
 *   parser.addProcessor(customerTag.end(), customerEndTag);
 *
 *   // populate an EventList of Customers from a stream
 *   EventList<Customer> customers = ...
 *   InputStream source = ...
 *   parser.parse(customers, source);
 * </pre>
 *
 * @author James Lemieux
 */
public class Parser {

    /**
     * A map from each XMLTagPath that represents a location in an XML Document
     * to the Processor that defines the processing logic for that location.
     */
    private final Map<XMLTagPath, PushProcessor> pushProcessors = new HashMap<XMLTagPath, PushProcessor>();
    private final Map<XMLTagPath, PopProcessor> popProcessors = new HashMap<XMLTagPath, PopProcessor>();

    /**
     * Map the logic defined the in the given <code>processor</code> to the
     * location within an XML stream specified by the given <code>path</code>.
     * Each time this parser encounters the <code>path</code> within an XML
     * stream, the <code>processor</code> will be invoked.
     *
     * @param path a unique location within an XML Document specified as a
     *      path of XML tags from the Document root
     * @param processor an object containing the processing logic to execute
     *      when <code>path</code> is encountered within an XML Document
     */
    public void addProcessor(XMLTagPath path, PushProcessor processor) {
        pushProcessors.put(path, processor);
    }
    public void addProcessor(XMLTagPath path, PopProcessor processor) {
        popProcessors.put(path, processor);
    }

    /**
     * Parses the objects in the given <code>source</code> and inserts them
     * into the given <code>target</code> as they are created. In this way, the
     * objects will "stream" into the <code>target</code> EventList.
     *
     * @param source the stream containing XML Documents of data
     * @param handler the ContentHandler that receives the SAX callbacks
     * @throws IOException if an error occurs parsing the <code>source</code>
     */
    private static void parse(InputStream source, ContentHandler handler) throws IOException {
        try {
            // configure a SAX parser
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            SaxParserSidekick.install(xmlReader);

            xmlReader.setContentHandler(handler);

            // parse away
            xmlReader.parse(new InputSource(source));

        } catch (SAXException e) {
            e.printStackTrace();
            throw new IOException("Parsing failed " + e.getMessage());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new IOException("Parsing failed " + e.getMessage());
        }
    }

    /**
     * Parse the objects in the given <code>source</code> and inserts them into
     * the given <code>target</code> as they are created. In this way, the
     * objects will "stream" into the <code>target</code> EventList.
     *
     * @param target the {@link EventList} to populate with data
     * @param source the stream containing XML Documents of data
     * @throws IOException if an error occurs parsing the <code>source</code>
     */
    public <T> void parse(InputStream source, T target) throws IOException {
        parse(source, new Handler<T>(target));
    }

    /**
     * This Handler receives the callbacks from the SAX parser as tags are
     * visited. It performs two duties:
     *
     * <ol>
     *   <li>It continually tracks an {@link XMLTagPath} which uniquely
     *       describes the location within the XML stream. It then executes
     *       any {@link PushProcessor} associated with that {@link XMLTagPath}.
     *
     *   <li>It tracks and stores the String data located between opening
     *       and closing tags and stores it in a Map context for use when
     *       processing.
     * </ol>
     *
     */
    private class Handler<B> extends DefaultHandler {

        /** An Object that describes the current path of open XML tags within the XML stream. */
        private XMLTagPath currentTagPath = XMLTagPath.emptyPath();

        /** A StringBuffer to collect the raw String data between open and close tags within the XML stream. */
        private StringBuffer currentChars = new StringBuffer();

        /** the stack of miscellaneous objects being processed */
        private List<Object> stack = new ArrayList<Object>();

        /**
         * Constructs a SAX ContentHandler which populates the given
         * <code>baseObject</code> with objects as they are discovered.
         */
        public Handler(B baseObject) {
            stack.add(baseObject);
        }

        /** @inheritDoc */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // update the XMLTagPath by pushing on the latest start tag
            currentTagPath = currentTagPath.child(qName).start();

            // process the start tag
            PushProcessor startProcessor = pushProcessors.get(currentTagPath);
            if(startProcessor != null) {
                push(startProcessor);
            }

            // process attributes
            for (int i = 0; i < attributes.getLength(); i++) {
                final XMLTagPath attributeTagPath = currentTagPath.attribute(attributes.getQName(i));
                PopProcessor attributeProcessor = popProcessors.get(attributeTagPath);
                if(attributeProcessor != null) {
                    final String attributeValue = attributes.getValue(i);
                    pop(attributeProcessor, attributeValue);
                }
            }
        }

        /** @inheritDoc */
        @Override
        public void endElement(String uri, String localName, String qName) {
            // update the XMLTagPath by indicating the path is to the *end* tag
            currentTagPath = currentTagPath.end();

            // process the text
            PopProcessor popProcessor = popProcessors.get(currentTagPath.body());
            if(popProcessor != null) {
                String text = currentChars.toString();
                pop(popProcessor, text);
            }

            // process the end tag
            PopProcessor endProcessor = popProcessors.get(currentTagPath.end());
            if(endProcessor != null) {
                Object element = stack.remove(stack.size() - 1);
                pop(endProcessor, element);
            }

            // remove the last tag in the XMLTagPath to obtain the start of the next XMLTagPath
            currentTagPath = currentTagPath.parent();

            // reset the raw String data capture buffer
            currentChars.setLength(0);
        }

        private void push(PushProcessor pushProcessor) {
            stack.add(pushProcessor.evaluate());
        }

        private <T> void pop(PopProcessor<Object,T> popProcessor, T element) {
            try {
                popProcessor.process(stack.get(stack.size() - 1), element);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        /** @inheritDoc */
        @Override
        public void characters(char ch[], int start, int length) {
            currentChars.append(ch, start, length);
        }
    }
}