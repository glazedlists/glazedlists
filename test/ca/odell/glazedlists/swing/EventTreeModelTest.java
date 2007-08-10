package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeListTest;

public class EventTreeModelTest extends SwingTestCase {

    public void guiTestDispose() {
        EventList<String> treeNodeList = new BasicEventList<String>();

        TreeList<String> glazedTreeList = new TreeList<String>(treeNodeList, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.NODES_START_COLLAPSED);
        EventTreeModel<String> eventTreeModel = new EventTreeModel<String>(glazedTreeList);
        eventTreeModel.dispose();
        glazedTreeList.dispose();
    }
}