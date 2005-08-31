package ca.odell.glazedlists.demo.issuebrowser;

/**
 * Enumeration class that provides available priorities.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public final class Priority implements Comparable {

	public static final Priority P1 = new Priority(1);
	public static final Priority P2 = new Priority(2);
	public static final Priority P3 = new Priority(3);
	public static final Priority P4 = new Priority(4);
	public static final Priority P5 = new Priority(5);

	/** The value of this particular priority. */
	private final int value;

    private Priority(int value) {
        this.value = value;
    }

    /**
	 * Lookup the correct static instance based on the given input string.
	 */
	public static Priority lookup(String priority_name) {
		if ("P1".equals(priority_name)) return P1;
		if ("P2".equals(priority_name)) return P2;
		if ("P3".equals(priority_name)) return P3;
		if ("P4".equals(priority_name)) return P4;
		if ("P5".equals(priority_name)) return P5;

		throw new IllegalArgumentException("Priority \"" + priority_name + "\" not found.");
	}

	/**
	 * Gets this priority as an integer between zero and one hundred
	 */
	public int getRating() {
		return 125 - (value * 25);
	}

    public int compareTo(Object o) {
        // Note: toggle the sign because P1 is a "higher" priority than P5, etc.
        return value - ((Priority) o).value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Priority priority = (Priority) o;

        return value == priority.value;
    }

    public int hashCode() {
        return value;
    }

    public String toString() {
        return "P" + value;
    }
}