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

	public static final Priority P1 = new Priority("P1");
	public static final Priority P2 = new Priority("P2");
	public static final Priority P3 = new Priority("P3");
	public static final Priority P4 = new Priority("P4");
	public static final Priority P5 = new Priority("P5");

    /** the name of this particular priority */
	private String name;

	/**
	 * Lookup the correct static instance based on the given input string.
	 */
	public static Priority lookup( String priority_name ) {
		if(priority_name == null) return null;

		if(priority_name.equals(P1.name)) return P1;
		if(priority_name.equals(P2.name)) return P2;
		if(priority_name.equals(P3.name)) return P3;
		if(priority_name.equals(P4.name)) return P4;
		if(priority_name.equals(P5.name)) return P5;

		throw new IllegalArgumentException( "Priority \"" + priority_name +
			"\" not found." );
	}


	private Priority(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals( Object obj ) {
		if(obj == this) return true;

		if(obj == null || !getClass().equals(obj.getClass())) return false;

		Priority other = (Priority)obj;

		return name.equals(other.name);
	}

	public int compareTo(Object o) {
		// Note: toggle the sign because P1 is a "higher" priority than P5, etc.
		return -name.compareTo(((Priority) o).name);
	}
}
