/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.nachocalendar;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.matchers.RangeMatcherEditor;
import net.sf.nachocalendar.CalendarFactory;
import net.sf.nachocalendar.components.DateField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Date;

/**
 * This {@link NachoDateRangeMatcherEditor} uses
 * <a href="http://nachocalendar.sourceforge.net/">NachoCalendar</a> DateField
 * objects to edit the endpoints of the date range.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: NachoCalendar</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>NachoCalendar</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>0.23</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="http://nachocalendar.sourceforge.net/">http://nachocalendar.sourceforge.net/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.gnu.org/copyleft/lesser.html">LGPL</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>It provides a single hook for customizing the layout and/or look of the
 * DateFields in the form of a local factory method which produces the entire
 * component, {@link #createComponent}. Subclasses may override that method to
 * decorate and customize the look of this {@link NachoDateRangeMatcherEditor} as
 * needed.
 *
 * @author James Lemieux
 */
public class NachoDateRangeMatcherEditor<E> extends RangeMatcherEditor<Date,E> {

    /** The UI components which capture the start and end of the date range */
    private final DateField fromDateField = CalendarFactory.createDateField();
    private final DateField toDateField = CalendarFactory.createDateField();

    /** The panel containing all UI components which edit the date range */
    private final JComponent component;

    public NachoDateRangeMatcherEditor(Filterator<Date,E> filterator) {
        super(filterator);

        fromDateField.setAntiAliased(true);
        toDateField.setAntiAliased(true);

        // initialize the pickers to have no values
        fromDateField.setValue(null);
        toDateField.setValue(null);

        // react to changes in the date pickers by broadcasting new Matchers
        final ChangeListener changeListener = new DateRangeChangeListener();
        fromDateField.addChangeListener(changeListener);
        toDateField.addChangeListener(changeListener);

        this.component = createComponent(fromDateField, toDateField);
    }

    /**
     * A local factory method responsible for customizing the given DateField
     * objects and laying them out in some swing container in a manner that is
     * appropriate for the application that uses it.
     *
     * @param fromDateField the DateField which edits the start of this date range
     * @param toDateField the DateField which edits the end of this date range
     * @return a swing component which edits the date range of this MatcherEditor
     */
    protected JComponent createComponent(DateField fromDateField, DateField toDateField) {
        final JPanel panel = new JPanel(new GridBagLayout());

        // assemble the panel we will return as the MatcherEditor's UI
        panel.setOpaque(false);
        panel.add(fromDateField, new GridBagConstraints(0, 0, 1, 1, 1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel("to"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
        panel.add(toDateField, new GridBagConstraints(2, 0, 1, 1, 1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        return panel;
    }

    /**
     * This method returns the component created by {@link #createComponent}.
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * As either end of the date range changes, broadcast create new Matchers
     * describing the change that occurred to the date range.
     */
    private class DateRangeChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            final Date fromDate = (Date) fromDateField.getValue();
            final Date toDate = (Date) toDateField.getValue();
            setRange(fromDate, toDate);
        }
    }
}