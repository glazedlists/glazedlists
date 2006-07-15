/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

/**
 * Enumeration class that provides available audience ratings.
 *
 * @author James Lemieux
 */
public final class AudienceRating implements Comparable {

	public static final AudienceRating G = new AudienceRating(1, "G");
	public static final AudienceRating PG = new AudienceRating(2, "PG");
	public static final AudienceRating PG_13 = new AudienceRating(3, "PG-13");
	public static final AudienceRating R = new AudienceRating(4, "R");
	public static final AudienceRating NC_17 = new AudienceRating(5, "NC-17");
	public static final AudienceRating X = new AudienceRating(6, "X");
	public static final AudienceRating NR = new AudienceRating(7, "NR");

	private final int rating;

    private final String name;

    private AudienceRating(int value, String name) {
        this.rating = value;
        this.name = name;
    }

	/**
	 * Lookup the correct static instance based on the given input string.
	 */
	public static AudienceRating lookup(String rating_name) {
		if (rating_name == null) return null;

		if (rating_name.equals("G (General Audience)")) return G;
		if (rating_name.equals("PG (Parental Guidance Suggested)")) return PG;
		if (rating_name.equals("PG-13 (Parental Guidance Suggested)")) return PG_13;
		if (rating_name.equals("R (Restricted)")) return R;
		if (rating_name.equals("NC-17")) return NC_17;
		if (rating_name.equals("X (Mature Audiences Only)")) return X;
		if (rating_name.equals("NR (Not Rated)")) return NR;
		if (rating_name.equals("Unrated")) return NR;

		throw new IllegalArgumentException("AudienceRating \"" + rating_name + "\" not found.");
	}

	/**
     * Returns this priority as an int between 1 (for G) and 7 (for Unrated).
     */
	public int getRating() {
		return rating;
	}

	public String toString() {
		return name;
	}

	public int compareTo(Object o) {
		return rating - ((AudienceRating) o).rating;
	}
}