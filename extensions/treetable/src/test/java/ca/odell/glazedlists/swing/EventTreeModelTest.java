package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeListTest;

import org.junit.Test;

public class EventTreeModelTest extends SwingTestCase {

    @Test
    public void testDispose() {
        EventList<String> treeNodeList = new BasicEventList<>();

        TreeList<String> glazedTreeList = new TreeList<>(treeNodeList, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        EventTreeModel<String> eventTreeModel = new EventTreeModel<>(glazedTreeList);
        eventTreeModel.dispose();
        glazedTreeList.dispose();
    }
}