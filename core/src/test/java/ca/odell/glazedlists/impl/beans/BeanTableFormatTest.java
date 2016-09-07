/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.awt.Color;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the BeanTableFormat works as expected.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanTableFormatTest {

    /** the formats to test */
    private TableFormat<FootballTeam> footballFormat;
    private TableFormat<FootballTeam> classedFootballFormat;

    /** the objects to test */
    private FootballTeam riders = new FootballTeam("Roughriders", "Saskatchewan", Color.green, Color.white);
    private FootballTeam ticats = new FootballTeam("Tiger-Cats", "Hamilton", Color.yellow, Color.black);

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        String[] propertyNames = new String[] { "name", "home", "primary", "secondary", "yearWinner", "matchCount"};
        String[] columnNames = new String[] { "Name", "Home", "Primary Color", "Secondary Color", "Has won this year", "Match count" };
        boolean[] writable = new boolean[] { false, true, false, false, false, false };
        footballFormat = GlazedLists.tableFormat(propertyNames, columnNames, writable);
        classedFootballFormat = GlazedLists.tableFormat(FootballTeam.class, propertyNames, columnNames, writable);
    }

    /**
     * Clean up after the test.
     */
    @After
    public void tearDown() {
        footballFormat = null;
        classedFootballFormat = null;
    }

    /**
     * Tests that BeanTableFormat works as a TableFormat.
     */
    @Test
    public void testTableFormat() {
        assertEquals(6,                  footballFormat.getColumnCount());
        assertEquals("Name",             footballFormat.getColumnName(0));
        assertEquals("Home",             footballFormat.getColumnName(1));
        assertEquals("Primary Color",    footballFormat.getColumnName(2));
        assertEquals("Secondary Color",  footballFormat.getColumnName(3));

        assertEquals("Roughriders",      footballFormat.getColumnValue(riders, 0));
        assertEquals("Saskatchewan",     footballFormat.getColumnValue(riders, 1));
        assertEquals(Color.green,        footballFormat.getColumnValue(riders, 2));
        assertEquals(Color.white,        footballFormat.getColumnValue(riders, 3));

        assertEquals("Tiger-Cats",       footballFormat.getColumnValue(ticats, 0));
        assertEquals("Hamilton",         footballFormat.getColumnValue(ticats, 1));
        assertEquals(Color.yellow,       footballFormat.getColumnValue(ticats, 2));
        assertEquals(Color.black,        footballFormat.getColumnValue(ticats, 3));
    }

    /**
     * Tests that BeanTableFormat works as a WritableTableFormat.
     */
    @Test
    public void testWritableTableFormat() {
        WritableTableFormat<FootballTeam> writableFootballFormat = (WritableTableFormat<FootballTeam>) footballFormat;
        assertEquals(false,              writableFootballFormat.isEditable(riders, 0));
        assertEquals(true,               writableFootballFormat.isEditable(riders, 1));
        assertEquals(false,              writableFootballFormat.isEditable(riders, 2));
        assertEquals(false,              writableFootballFormat.isEditable(riders, 3));

        writableFootballFormat.setColumnValue(riders, "Regina", 1);
        assertEquals("Regina",           riders.getHome());
        assertEquals("Regina",           footballFormat.getColumnValue(riders, 1));

        writableFootballFormat.setColumnValue(ticats, "Lancaster", 1);
        assertEquals("Lancaster",        ticats.getHome());
        assertEquals("Lancaster",        footballFormat.getColumnValue(ticats, 1));
    }

    /**
     * Tests that BeanTableFormat works as an AdvancedTableFormat.
     */
    @Test
    public void testAdvancedTableFormat() {
        AdvancedTableFormat emptyAdvancedFootballFormat = (AdvancedTableFormat)footballFormat;
        assertEquals(Object.class,       emptyAdvancedFootballFormat.getColumnClass(0));
        assertEquals(Object.class,       emptyAdvancedFootballFormat.getColumnClass(1));
        assertEquals(Object.class,       emptyAdvancedFootballFormat.getColumnClass(2));
        assertEquals(Object.class,       emptyAdvancedFootballFormat.getColumnClass(3));
        assertEquals(Object.class,       emptyAdvancedFootballFormat.getColumnClass(4));
        assertEquals(Object.class,       emptyAdvancedFootballFormat.getColumnClass(5));
        assertEquals(GlazedLists.comparableComparator(), emptyAdvancedFootballFormat.getColumnComparator(0));
        assertEquals(GlazedLists.comparableComparator(), emptyAdvancedFootballFormat.getColumnComparator(1));
        assertEquals(GlazedLists.comparableComparator(), emptyAdvancedFootballFormat.getColumnComparator(2));
        assertEquals(GlazedLists.comparableComparator(), emptyAdvancedFootballFormat.getColumnComparator(3));
        assertEquals(GlazedLists.comparableComparator(), emptyAdvancedFootballFormat.getColumnComparator(4));
        assertEquals(GlazedLists.comparableComparator(), emptyAdvancedFootballFormat.getColumnComparator(5));

        AdvancedTableFormat fullAdvancedFootballFormat = (AdvancedTableFormat)classedFootballFormat;
        assertEquals(String.class,       fullAdvancedFootballFormat.getColumnClass(0));
        assertEquals(String.class,       fullAdvancedFootballFormat.getColumnClass(1));
        assertEquals(Color.class,        fullAdvancedFootballFormat.getColumnClass(2));
        assertEquals(Color.class,        fullAdvancedFootballFormat.getColumnClass(3));
        assertEquals(Boolean.class,      fullAdvancedFootballFormat.getColumnClass(4));
        assertEquals(Integer.class,      fullAdvancedFootballFormat.getColumnClass(5));
        assertEquals(GlazedLists.comparableComparator(), fullAdvancedFootballFormat.getColumnComparator(0));
        assertEquals(GlazedLists.comparableComparator(), fullAdvancedFootballFormat.getColumnComparator(1));
        assertEquals(null,                               fullAdvancedFootballFormat.getColumnComparator(2));
        assertEquals(null,                               fullAdvancedFootballFormat.getColumnComparator(3));
        assertEquals(GlazedLists.comparableComparator(), fullAdvancedFootballFormat.getColumnComparator(4));
        assertEquals(GlazedLists.comparableComparator(), fullAdvancedFootballFormat.getColumnComparator(5));
    }

    @Test
    public void testIdentityColumn() {
        final String[] propertyNames = {"this"};
        final String[] columnLabels = {"Summary"};
        final TableFormat<FootballTeam> footballTeamTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        assertSame(riders, footballTeamTableFormat.getColumnValue(riders, 0));

        final boolean[] writable = {true};
        try {
            GlazedLists.tableFormat(FootballTeam.class, propertyNames, columnLabels, writable);
            fail("failed to throw an exception for a writable column of property 'this'");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }
}

/**
 * A test object.
 */
class FootballTeam {
    private String name;
    private String home;
    private Color primary;
    private Color secondary;
    private boolean yearWinner;
    private int matchCount;
    public FootballTeam(String name, String home, Color primary, Color secondary) {
        this.name = name;
        this.home = home;
        this.primary = primary;
        this.secondary = secondary;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; };

    public String getHome() { return home; }
    public void setHome(String home) { this.home = home; };

    public Color getPrimary() { return primary; }
    public void setPrimary(Color primary) { this.primary = primary; };

    public Color getSecondary() { return secondary; }
    public void setSecondary(Color secondary) { this.secondary = secondary; };

    public int getMatchCount() { return matchCount; }
    public void setMatchCount(int matchCount) { this.matchCount = matchCount; }

    public boolean isYearWinner() { return yearWinner; }
    public void setYearWinner(boolean yearWinner) { this.yearWinner = yearWinner; }
}
