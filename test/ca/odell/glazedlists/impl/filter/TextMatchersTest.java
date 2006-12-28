/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import junit.framework.TestCase;

public class TextMatchersTest extends TestCase {

    public void testParseSimple() {
        SearchTerm[] terms = TextMatchers.parse("blah");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);

        terms = TextMatchers.parse("blah boo blee");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", false, false);

        terms = TextMatchers.parse("\tblah     boo \t blee      ");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", false, false);
    }

    public void testParseSimpleWithNegation() {
        SearchTerm[] terms = TextMatchers.parse("-blah");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", true, false);

        terms = TextMatchers.parse("-blah boo -blee");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", true, false);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", true, false);

        terms = TextMatchers.parse("\tblah     -boo \t blee      ");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", true, false);
        checkFilterTerm(terms[2], "blee", false, false);

        terms = TextMatchers.parse("-blah --boo - - ---blee");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", true, false);
        checkFilterTerm(terms[1], "boo", true, false);
        checkFilterTerm(terms[2], "blee", true, false);
    }

    public void testParseSimpleWithRequired() {
        SearchTerm[] terms = TextMatchers.parse("+blah");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);

        terms = TextMatchers.parse("+blah boo +blee");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", false, true);

        terms = TextMatchers.parse("\tblah     +boo \t blee      ");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", false, true);
        checkFilterTerm(terms[2], "blee", false, false);

        terms = TextMatchers.parse("+blah ++boo + + +++blee");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);
        checkFilterTerm(terms[1], "boo", false, true);
        checkFilterTerm(terms[2], "blee", false, true);
    }

    public void testParseQuotes() {
        SearchTerm[] terms = TextMatchers.parse("\"blah\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);

        terms = TextMatchers.parse("\"blah\" \"boo\" \"blee\"");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", false, false);

        terms = TextMatchers.parse("\t\"blah\"     \"boo\" \t \"blee\"      ");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", false, false);

        terms = TextMatchers.parse("\"blee \t    boo   \t\t blah   \t\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blee \t    boo   \t\t blah   \t", false, false);
    }

    public void testParseQuotesWithNegation() {
        SearchTerm[] terms = TextMatchers.parse("-\"blah\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", true, false);

        terms = TextMatchers.parse("-\"blah\" \"boo\" -\"blee\"");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", true, false);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", true, false);

        terms = TextMatchers.parse("\t-\"blah\"     -\"boo\" \t -\"blee\"      ");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", true, false);
        checkFilterTerm(terms[1], "boo", true, false);
        checkFilterTerm(terms[2], "blee", true, false);

        terms = TextMatchers.parse("-\"blee \t    boo   \t\t blah   \t\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blee \t    boo   \t\t blah   \t", true, false);
    }

    public void testParseQuotesWithRequired() {
        SearchTerm[] terms = TextMatchers.parse("+\"blah\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);

        terms = TextMatchers.parse("+\"blah\" \"boo\" +\"blee\"");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);
        checkFilterTerm(terms[1], "boo", false, false);
        checkFilterTerm(terms[2], "blee", false, true);

        terms = TextMatchers.parse("\t+\"blah\"     +\"boo\" \t +\"blee\"      ");
        assertEquals(3, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);
        checkFilterTerm(terms[1], "boo", false, true);
        checkFilterTerm(terms[2], "blee", false, true);

        terms = TextMatchers.parse("+\"blee \t    boo   \t\t blah   \t\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blee \t    boo   \t\t blah   \t", false, true);
    }

    public void testComplexParsing() {
        SearchTerm[] terms = TextMatchers.parse("+\"blah\" in the -\"plough shares\"");
        assertEquals(4, terms.length);
        checkFilterTerm(terms[0], "blah", false, true);
        checkFilterTerm(terms[1], "in", false, false);
        checkFilterTerm(terms[2], "the", false, false);
        checkFilterTerm(terms[3], "plough shares", true, false);

        terms = TextMatchers.parse("\"+blah\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "+blah", false, false);

        terms = TextMatchers.parse("+ blah");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);

        terms = TextMatchers.parse("- blah");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
    }

    public void testParsePartialQuotes() {
        SearchTerm[] terms = TextMatchers.parse("\"blah");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);

        terms = TextMatchers.parse("blah\"");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);

        terms = TextMatchers.parse("\"blah\"  \"boo");
        assertEquals(2, terms.length);
        checkFilterTerm(terms[0], "blah", false, false);
        checkFilterTerm(terms[1], "boo", false, false);
    }

    private void checkFilterTerm(SearchTerm term, String text, boolean negated, boolean required) {
        assertEquals(text, term.getText());
        assertEquals(negated, term.isNegated());
        assertEquals(required, term.isRequired());
    }
}
