package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.matchers.Matchers;
import junit.framework.TestCase;

public class BeanPropertyMatcherTest extends TestCase {

    public void testConstructor() {
        new BeanPropertyMatcher<Dog>(new BeanProperty<Dog>(Dog.class, "legs", true, false), new Integer(4));
        new BeanPropertyMatcher<Dog>(new BeanProperty<Dog>(Dog.class, "name", true, false), null);

        try {
            new BeanPropertyMatcher<Dog>(null, "should fail");
            fail("failed to receive IllegalArgumentException on null BeanProperty");
        } catch (IllegalArgumentException iae) { }
    }

    public void testMatching() {
        final EventList<Dog> dogs = new FilterList<Dog>(new BasicEventList<Dog>(), Matchers.beanPropertyMatcher(Dog.class, "name", "Fido"));

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