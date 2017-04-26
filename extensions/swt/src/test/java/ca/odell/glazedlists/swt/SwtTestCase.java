/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * Utility class for running JUnit tests with SWT code.
 *
 * <p>
 * This class has the following behaviour:
 *
 * <ul>
 * <li>it uses {@link SwtClassRule} to initialize a SWT display and shell in a dedicated thread</li>
 *
 * <li>it uses {@link SwtTestRule} to perfom each test method on the SWT display thread</li>
 * </ul>
 *
 * This class provides both the SWT {@link Display} and {@link Shell} for the
 * test methods available via {@link #getDisplay()} and {@link #getShell()}.
 *
 * @author Holger Brands
 */
public abstract class SwtTestCase {
    @ClassRule
    public static SwtClassRule swtClassRule = new SwtClassRule();

    @Rule
    public SwtTestRule swtTestRule = new SwtTestRule(swtClassRule);

    protected Display getDisplay() {
        return swtClassRule.getDisplay();
    }

    protected Shell getShell() {
        return swtClassRule.getShell();
    }
}