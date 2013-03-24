/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.ExecuteOnMainThread;

import java.awt.EventQueue;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link SwingTestRule}.
 *
 * @author Holger Brands
 */
public class SwingTestRuleTest extends SwingTestCase {

	@Test
	public void executeOnSwingThread1() {
		System.out.println(Thread.currentThread() + " SwingTestRuleTest.executeOnSwingThread1()");
		assertTrue("Expected Swing EDT but was " + Thread.currentThread(), EventQueue.isDispatchThread());
	}

	@Test
	@ExecuteOnMainThread
	public void executeOnMainThread() {
		System.out.println(Thread.currentThread() + "SwingTestRuleTest.executeOnMainThread()");
		assertFalse("Expected Main thread but was " + Thread.currentThread(), EventQueue.isDispatchThread());
	}

	@Test
	public void executeOnSwingThread2() {
		System.out.println(Thread.currentThread() + " SwingTestRuleTest.executeOnSwingThread2()");
		assertTrue("Expected Swing EDT but was " + Thread.currentThread(), EventQueue.isDispatchThread());
	}
}
