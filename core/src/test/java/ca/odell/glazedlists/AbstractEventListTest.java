package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventAssembler;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class AbstractEventListTest {

    // Ensure calling replaceAll on a list with an unimplemented set() doesn't completely
    // destroy assembler state
    @Test
    public void replaceAll_unimplementedSet() {
        AbstractEventList<String> my_list = new AbstractEventList<String>() {
            @Override
            public int size() {
                return 1;
            }

            @Override
            public String get(int index) {
                if (index != 0) throw new IndexOutOfBoundsException("Wha??");
                return "Zero";
            }

            @Override
            public void dispose() {}
        };
        assertFalse(my_list.updates.isEventInProgress());

        try {
            my_list.replaceAll(s -> "Not gonna work");
            fail("Shouldn't have worked");
        }
        catch(UnsupportedOperationException ex) {
            // expected
        }

        assertFalse(my_list.updates.isEventInProgress());
    }
}