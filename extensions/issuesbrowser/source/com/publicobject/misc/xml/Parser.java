/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This XML Parser greatly simplifies the task of parsing an XML document and
 * inserting the resulting Java Objects into an {@link EventList}. It is
 * architected to be very declarative. The user of the Parser object first
 * configures it with pairs of objects: an {@link XMLTagPath} object which
 * identifies a location within an XML Document, and a {@link Processor} object
 * which defines logic capable of processing the data read from the Document at
 * that point.
 *
 * <p>After configuring the Parser with repeated calls to
 * {@link #addProcessor(XMLTagPath, Processor)}, a Document can be parsed using
 * {@link #parse(EventList, InputStream)} and the contents of the
 * {@link InputStream} will be inserted into the {@link EventList}.
 *
 * <p>Sample usage on a simple XML Document defining a customer might resemble:
 * <pre>
 *   Parser parser = new Parser();
 *
 *   // create a new Customer object each time the customer start tag is found
 *   XMLTagPath customerStartPath = XMLTagPath.startTagPath("customer");
 *   Processor customerStartTag = Processors.createNewObject(Customer.class);
 *   parser.addProcessor(customerStartPath, customerStartTag);
 *
 *   // add the new Customer object to the target EventList when the end tag is found
 *   XMLTagPath customerEndPath = XMLTagPath.endTagPath("customer");
 *   Processor customerEndTag = Processors.addObjectToTargetList();
 *   parser.addProcessor(customerEndPath, customerEndTag);
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
    private final Map<XMLTagPath, Processor> processors = new HashMap<XMLTagPath, Processor>();

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
    public void addProcessor(XMLTagPath path, Processor processor) {
        processors.put(path, processor);
    }

    /**
     * Execute the {@link Processor} associated with the given <code>path</code>,
     * if one exists.
     *
     * @param path a unique location within an XML Document specified as a
     *      path of XML tags from the Document root
     * @param context a {@link Map} of all Objects currently being constructed
     *      by the parser. Each key is the {@link XMLTagPath} to the opening XML
     *      tag
     */
    private void executeProcessor(XMLTagPath path, Map<XMLTagPath, Object> context) {
        final Processor p = processors.get(path);
        if (p != null)
            p.process(path, context);
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
    public void parse(EventList target, InputStream source) throws IOException {
        parse(source, new Handler(target));
    }

    /**
     * Parse the given <code>source</code> and return the resulting parse
     * context Map.
     *
     * @param source the {@link InputStream} to be parsed
     * @return the resulting parse context Map created from the {@link InputStream}
     * @throws IOException if an error occurs parsing the <code>source</code>
     */
    public Map<XMLTagPath, Object> parse(InputStream source) throws IOException {
        final Handler handler = new Handler();
        parse(source, handler);

        return handler.context;
    }

    /**
     * This Handler receives the callbacks from the SAX parser as tags are
     * visited. It performs two duties:
     *
     * <ol>
     *   <li>It continually tracks an {@link XMLTagPath} which uniquely
     *       describes the location within the XML stream. It then executes
     *       any {@link Processor} associated with that {@link XMLTagPath}.
     *
     *   <li>It tracks and stores the String data located between opening
     *       and closing tags and stores it in a Map context for use when
     *       processing.
     * </ol>
     */
    private class Handler extends DefaultHandler {

        /** A map from each opening XMLTagPath to the Object or String data associated with that location. */
        private final Map<XMLTagPath, Object> context = new HashMap<XMLTagPath, Object>();

        /** An Object that describes the current path of open XML tags within the XML stream. */
        private XMLTagPath currentTagPath = XMLTagPath.newPath();

        /** A StringBuffer to collect the raw String data between open and close tags within the XML stream. */
        private StringBuffer currentChars = new StringBuffer();

        /**
         * Constructs a SAX ContentHandler with a {@link BasicEventList}.
         * This constructor assumes popuplating an {@link EventList} with
         * results from a given {@link InputStream} is not the main goal
         * of the Parser.
         */
        public Handler() {
            this(new BasicEventList());
        }

        /**
         * Constructs a SAX ContentHandler which populates the given
         * <code>target</code> with objects as they are discovered.
         */
        public Handler(EventList target) {
            // place the target EventList into the context with a "special key",
            // namely an empty XMLTagPath (which is otherwise an invalid path)
            context.put(currentTagPath, target);
        }

        /** @inheritDoc */
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // update the XMLTagPath by pushing on the latest start tag
            currentTagPath = currentTagPath.child(qName).start();

            // add entries in the parser context Map for each of the attributes
            for (int i = 0; i < attributes.getLength(); i++) {
                final XMLTagPath attributeTagPath = currentTagPath.attribute(attributes.getQName(i));
                final String attributeValue = attributes.getValue(i);
                context.put(attributeTagPath, attributeValue);
            }

            // null out any value at the position within the parse context Map
            context.put(currentTagPath.end(), null);

            // execute any Processor associated with the current start XMLTagPath
            executeProcessor(currentTagPath, context);
        }

        /** @inheritDoc */
        public void endElement(String uri, String localName, String qName) {
            // update the XMLTagPath by indicating the path is to the *end* tag
            currentTagPath = currentTagPath.end();

            // store the raw String data captured between the start and end tags
            // if the context does not include a value already
            if (context.get(currentTagPath) == null)
                context.put(currentTagPath, currentChars.toString());

            // execute any Processor associated with the current end XMLTagPath
            executeProcessor(currentTagPath, context);

            // remove the last tag in the XMLTagPath to obtain the start of the next XMLTagPath
            currentTagPath = currentTagPath.parent();

            // reset the raw String data capture buffer
            currentChars.setLength(0);
        }

        /** @inheritDoc */
        public void characters(char ch[], int start, int length) {
            currentChars.append(ch, start, length);
        }
    }
}