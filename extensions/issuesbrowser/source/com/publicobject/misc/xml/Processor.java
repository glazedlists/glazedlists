/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import java.util.Map;

/**
 * This interface defines the logic to perform when a given {@link XMLTagPath}
 * is encountered within an XML Document. Generally, the logic will read and
 * write to a given parse context {@link Map} that it is provided with as well
 * as mutate the objects within the map. The Processor's job is really to map
 * the raw String data located by the parser to formatted type-safe data
 * appropriate for use in the Objects being built.
 *
 * @author James Lemieux
 */
public interface Processor {
    /**
     * Called when the given <code>path</code> is encountered within an XML
     * stream. The given <code>context</code> contains parse data keyed to
     * {@link XMLTagPath} objects that indicate where, in the XML Document,
     * the data came from. Generally, custom objects, like say a Customer
     * object, is associated with the XMLTagPath to the starting customer
     * tag, and raw String data is associated with the end tag, like say
     * an end name tag.
     *
     * @param path the {@link XMLTagPath} associated with this Processor
     * @param context the data produced by other Processors and the
     *      {@link Parser}. It represents the sum total of all knowledge
     *      and partially built objects at any point in the parsers life.
     */
    public void process(XMLTagPath path, Map<XMLTagPath, Object> context);
}