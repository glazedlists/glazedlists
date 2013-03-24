/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.ExecuteOnMainThread;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link SwtTestRule}.
 *
 * @author Holger Brands
 */
public class SwtTestRuleTest extends SwtTestCase {

	@Test
	public void executeOnSwtThread1() {
		System.out.println(Thread.currentThread() + " SwtTestRuleTest.executeOnSwtThread1()");
		assertSame("Expected SWT thread but was " + Thread.currentThread(), swtClassRule.getDisplay().getThread(), Thread.currentThread());
	}

	@Test
	@ExecuteOnMainThread
	public void executeOnMainThread() {
		System.out.println(Thread.currentThread() + "SwtTestRuleTest.executeOnMainThread()");
		assertNotSame("Expected Main thread but was " + Thread.currentThread(), swtClassRule.getDisplay().getThread(), Thread.currentThread());
	}

	@Test
	public void executeOnSwtThread2() {
		System.out.println(Thread.currentThread() + " SwtTestRuleTest.executeOnSwtThread2()");
		assertSame("Expected SWT thread but was " + Thread.currentThread(), swtClassRule.getDisplay().getThread(), Thread.currentThread());
	}

}
