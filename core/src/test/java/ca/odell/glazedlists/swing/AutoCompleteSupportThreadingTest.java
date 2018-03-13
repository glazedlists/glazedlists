package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.junit.Test;

import static org.junit.Assert.*;

public class AutoCompleteSupportThreadingTest {

    @Test
    public void testNonEDTAccess() throws Exception {
        try {
            AutoCompleteSupport.install(new JComboBox<>(), new BasicEventList<>());
            fail("failed to receive IllegalStateException installing AutoCompleteSupport from non-EDT");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            AutoCompleteSupport.install(new JComboBox<>(), new BasicEventList<>(), GlazedLists.toStringTextFilterator());
            fail("failed to receive IllegalStateException installing AutoCompleteSupport from non-EDT");
        } catch (IllegalStateException e) {
            // expected
        }

        final InstallAutoCompleteSupportRunnable installAutoCompleteSupportRunnable = new InstallAutoCompleteSupportRunnable();
        SwingUtilities.invokeAndWait(installAutoCompleteSupportRunnable);

        final AutoCompleteSupport<Object> support = installAutoCompleteSupportRunnable.getSupport();

        try {
            support.setStrict(true);
            fail("failed to receive IllegalStateException mutating AutoCompleteSupport from non-EDT");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            support.setCorrectsCase(true);
            fail("failed to receive IllegalStateException mutating AutoCompleteSupport from non-EDT");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            support.uninstall();
            fail("failed to receive IllegalStateException uninstalling AutoCompleteSupport from non-EDT");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    private static final class InstallAutoCompleteSupportRunnable implements Runnable {
        private AutoCompleteSupport<Object> support;

        @Override
        public void run() {
            support = AutoCompleteSupport.install(new JComboBox<>(), new BasicEventList<>());
        }
        public AutoCompleteSupport<Object> getSupport() {
            return support;
        }
    }
}
