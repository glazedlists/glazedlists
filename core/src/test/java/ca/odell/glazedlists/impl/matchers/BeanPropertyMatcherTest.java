package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matchers;

import org.junit.Test;

import static org.junit.Assert.*;

public class BeanPropertyMatcherTest {

    @Test
    public void testConstructor() {
        new BeanPropertyMatcher<>(Dog.class, "legs", new Integer(4));
        new BeanPropertyMatcher<>(Dog.class, "name", null);

        try {
            new BeanPropertyMatcher<Dog>(null, "name", "should fail");
            fail("failed to receive IllegalArgumentException on null BeanProperty");
        } catch (IllegalArgumentException iae) { }

        try {
            new BeanPropertyMatcher<>(Dog.class, null, "should fail");
            fail("failed to receive IllegalArgumentException on null BeanProperty");
        } catch (IllegalArgumentException iae) { }
    }

    @Test
    public void testMatching() {
        final EventList<Dog> dogs = new FilterList<>(new BasicEventList<Dog>(), Matchers.beanPropertyMatcher(Dog.class, "name", "Fido"));

        assertEquals(0, dogs.size());

        dogs.add(new Dog("Fido"));
        assertEquals(1, dogs.size());

        dogs.add(new Dog("Barry"));
        assertEquals(1, dogs.size());

        dogs.add(new Dog("Fido"));
        assertEquals(2, dogs.size());
    }

    public static class Dog {
        private int legs = 4;
        private boolean hasTail = true;
        private String name;

        public Dog(String name) {
            this.name = name;
        }

        public int getLegs() {
            return legs;
        }

        public boolean getHasTail() {
            return hasTail;
        }

        public String getName() {
            return name;
        }
    }
}
