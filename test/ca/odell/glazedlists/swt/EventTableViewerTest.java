package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.io.IntegerTableFormat;
import junit.framework.TestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class EventTableViewerTest extends TestCase {

    public void testConstructorWithCheckableTable() {
        Display display = new Display();
        Shell parent = new Shell(display);

        EventTableViewer<Integer> viewer = new EventTableViewer<Integer>(new BasicEventList<Integer>(), new Table(parent, SWT.CHECK), new IntegerTableFormat());
        viewer.dispose();
    }
}