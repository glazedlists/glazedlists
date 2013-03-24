package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link EventListJXTableSorting} in Swing EDT.
 *
 * @author Holger Brands
 */
public class EventListJXTableSortingTest extends SwingTestCase {

    private EventList<Color> colors;
    private SortedList<Color> sortedColors;
    private TableFormat<Color> colorTableFormat = GlazedLists.tableFormat(
            new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
    private TableModel tableModel;
    private JXTable table;
    private Comparator<Color> redComparator = GlazedLists.beanPropertyComparator(Color.class, "red");
    private Comparator<Color> alphaComparator = GlazedLists.beanPropertyComparator(Color.class, "alpha");

    @Before
    public void setUp() {
        colors = new BasicEventList<Color>();
        colors.add(Color.GRAY);
        colors.add(Color.WHITE);
        colors.add(Color.BLACK);
        sortedColors = new SortedList<Color>(colors, redComparator);
        tableModel = GlazedListsSwing.eventTableModel(GlazedListsSwing.swingThreadProxyList(sortedColors), colorTableFormat);
        table = new JXTable(tableModel);
    }

    /**
     * Tests installing without default comparator and without initial sort order.
     */
    @Test
    public void testInstallWithoutDefaultComparator() {
        assertEquals(redComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE), sortedColors);
        EventListJXTableSorting.install(table, sortedColors);
        assertSame(null, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.GRAY, Color.WHITE, Color.BLACK), sortedColors);
    }

    /**
     * Tests installing with default comparator from SortedList and without initial sort order.
     */
    @Test
    public void testInstallWithDefaultComparator() {
        assertEquals(redComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE), sortedColors);
        EventListJXTableSorting.install(table, sortedColors, sortedColors.getComparator());
        assertSame(redComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE), sortedColors);
    }

    /**
     * Tests installing with default comparator different from SortedLists and without initial sort order.
     */
    @Test
    public void testInstallWithDefaultComparator2() {
        assertEquals(redComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE), sortedColors);
        EventListJXTableSorting.install(table, sortedColors, alphaComparator);
        assertSame(alphaComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.GRAY, Color.WHITE, Color.BLACK), sortedColors);
    }

    /**
     * Tests installing without default comparator and with initial sort order.
     */
    @Test
    public void testInstallWithSortOrderWithoutDefaultComparator() {
        assertEquals(redComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE), sortedColors);
        table.setSortOrder(1, org.jdesktop.swingx.decorator.SortOrder.DESCENDING);
        EventListJXTableSorting.install(table, sortedColors);
        assertNotSame(null, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.WHITE, Color.GRAY, Color.BLACK), sortedColors);
    }

    /**
     * Tests installing with default comparator and with initial sort order.
     */
    @Test
    public void testInstallWithSortOrderWithDefaultComparator() {
        assertEquals(redComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE), sortedColors);
        table.setSortOrder(1, org.jdesktop.swingx.decorator.SortOrder.DESCENDING);
        EventListJXTableSorting.install(table, sortedColors, alphaComparator);
        assertNotSame(alphaComparator, sortedColors.getComparator());
        assertEquals(Arrays.asList(Color.WHITE, Color.GRAY, Color.BLACK), sortedColors);
    }

    /**
     * Tests uninstalling.
     */
    @Test
    public void testUninstall() {
        final FilterPipeline oldPipeline = table.getFilters();
        final EventListJXTableSorting s = EventListJXTableSorting.install(table, sortedColors, sortedColors.getComparator());
        assertNotSame(oldPipeline, table.getFilters());
        s.uninstall();
        assertSame(oldPipeline, table.getFilters());
    }
}
