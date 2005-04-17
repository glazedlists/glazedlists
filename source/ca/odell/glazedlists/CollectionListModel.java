/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.List;


/**
 * Provides the logic to map a parent record (e.g., a records album) to its children
 * (e.g., the songs on the record). Serves basically the same purpose as
 * {@link javax.swing.tree.TreeModel} does to a JTree in Swing.
 *
 * @see CollectionList
 * @see GlazedLists#listCollectionListModel()
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public interface CollectionListModel {
    
	/**
	 * Return a list of the child nodes for a parent node.
	 *
	 * @param parent The parent node.
	 * @return A List containing the child nodes.
	 */
	public List getChildren(Object parent);
}
