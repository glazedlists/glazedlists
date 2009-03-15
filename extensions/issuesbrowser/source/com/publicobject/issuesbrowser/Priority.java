/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

/**
 * Enumeration class that provides available priorities.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public final class Priority implements Comparable<Priority> {

	public static final Priority P1 = new Priority(1);
	public static final Priority P2 = new Priority(2);
	public static final Priority P3 = new Priority(3);
	public static final Priority P4 = new Priority(4);
	public static final Priority P5 = new Priority(5);

	private final int value;

	private final int rating;

    private final String name;

	/**
	 * Lookup the correct static instance based on the given input string.
	 */
	public static Priority lookup(String priority_name) {
		if (priority_name == null) return null;

		if (priority_name.equals("P1")) return P1;
		if (priority_name.equals("P2")) return P2;
		if (priority_name.equals("P3")) return P3;
		if (priority_name.equals("P4")) return P4;
		if (priority_name.equals("P5")) return P5;

		throw new IllegalArgumentException("Priority \"" + priority_name + "\" not found.");
	}

    private Priority(int value) {
        this.value = value;
        this.rating = 125 - (value * 25);
        this.name = "P" + value;
    }

	/**
     * Returns this priority as an int between 0 and 100.
     */
	public int getRating() {
		return rating;
	}

	@Override
    public String toString() {
		return name;
	}

	public int compareTo(Priority p) {
		// Note: toggle the sign because P1 is a "higher" priority than P5, etc.
		return value - p.value;
	}
}