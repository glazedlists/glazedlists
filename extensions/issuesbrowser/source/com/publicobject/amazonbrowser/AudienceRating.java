/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

/**
 * Enumeration class that provides available audience ratings. Valid ratings
 * include:
 *
 * <ul>
 *   <li>G - General Audience
 *   <li>PG - Parental Guidance Suggested
 *   <li>PG-13 - Parents Strongly Cautioned
 *   <li>R - Restricted
 *   <li>NC-17 - No Children Under 17 Admitted
 *   <li>X - No Children Under 17 Admitted
 *   <li>NR - Not Rated
 * </ul>
 *
 * @author James Lemieux
 */
public final class AudienceRating implements Comparable<AudienceRating> {

	public static final AudienceRating G = new AudienceRating(1, "G", "General Audiences");
	public static final AudienceRating PG = new AudienceRating(2, "PG", "Parental Guidance Suggested");
	public static final AudienceRating PG_13 = new AudienceRating(3, "PG-13", "Parents Strongly Cautioned");
	public static final AudienceRating R = new AudienceRating(4, "R", "Restricted");
	public static final AudienceRating NC_17 = new AudienceRating(5, "NC-17", "No One Under 17");
	public static final AudienceRating X = new AudienceRating(6, "X", "No One Under 18");
	public static final AudienceRating NR = new AudienceRating(7, "NR", "Not Rated");

	private final int rating;

    private final String name;

    private final String description;

    private AudienceRating(int value, String name, String description) {
        this.rating = value;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
		return name;
	}

	@Override
    public int compareTo(AudienceRating ar) {
		return rating - ar.rating;
	}
}