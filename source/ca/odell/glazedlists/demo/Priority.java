/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

/**
 * Enumeration class that provides available priorities.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class Priority implements Comparable {

	public static final Priority P1 = new Priority(1);
	public static final Priority P2 = new Priority(2);
	public static final Priority P3 = new Priority(3);
	public static final Priority P4 = new Priority(4);
	public static final Priority P5 = new Priority(5);

    /** the name of this particular priority */
	private int value;

	/**
	 * Lookup the correct static instance based on the given input string.
	 */
	public static Priority lookup( String priority_name ) {
		if(priority_name == null) return null;

		if(priority_name.equals("P1")) return P1;
		if(priority_name.equals("P2")) return P2;
		if(priority_name.equals("P3")) return P3;
		if(priority_name.equals("P4")) return P4;
		if(priority_name.equals("P5")) return P5;

		throw new IllegalArgumentException( "Priority \"" + priority_name +
			"\" not found." );
	}

    /**
     * Gets this priority as an integer between zero and one hundred
     */
    public int getRating() {
        return 125 - (value * 25);
    }


	private Priority(int value) {
		this.value = value;
	}

	public String toString() {
		return "P" + value;
	}

	public int hashCode() {
		return value;
	}

	public boolean equals( Object obj ) {
		if(obj == this) return true;

		if(obj == null || !getClass().equals(obj.getClass())) return false;

		Priority other = (Priority)obj;

		return value == other.value;
	}

	public int compareTo(Object o) {
		// Note: toggle the sign because P1 is a "higher" priority than P5, etc.
		return value - ((Priority)o).value;
	}
}
