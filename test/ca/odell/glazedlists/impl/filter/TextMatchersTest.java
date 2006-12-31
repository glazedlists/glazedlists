/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import junit.framework.TestCase;

import java.util.Arrays;

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

    public void testNormalizeSearchTerms() {
        assertTrue(Arrays.equals(searchTerms(""), normalizedSearchTerms("")));
        assertTrue(Arrays.equals(searchTerms("x"), normalizedSearchTerms("x")));
        assertTrue(Arrays.equals(searchTerms("x Y z"), normalizedSearchTerms("x Y z")));
        assertTrue(Arrays.equals(searchTerms("xyz"), normalizedSearchTerms("x xy xyz")));
        assertTrue(Arrays.equals(searchTerms("xyz"), normalizedSearchTerms("xyz xy x")));
        assertTrue(Arrays.equals(searchTerms("xyz"), normalizedSearchTerms("xy xyz x")));
        assertTrue(Arrays.equals(searchTerms("xyz"), normalizedSearchTerms("xyz xyz xyz")));
        assertTrue(Arrays.equals(searchTerms("blackened"), normalizedSearchTerms("black blackened")));
        assertTrue(Arrays.equals(searchTerms("this"), normalizedSearchTerms("this his")));

        assertTrue(Arrays.equals(searchTerms("blackened this"), normalizedSearchTerms("blackened this")));
        assertTrue(Arrays.equals(searchTerms("blackened this"), normalizedSearchTerms("this blackened")));
    }

    public void testNormalizeNegatedSearchTerms() {
        assertTrue(Arrays.equals(searchTerms("-"), normalizedSearchTerms("-")));
        assertTrue(Arrays.equals(searchTerms("-x"), normalizedSearchTerms("-x")));
        assertTrue(Arrays.equals(searchTerms("-x -Y -z"), normalizedSearchTerms("-x -Y -z")));
        assertTrue(Arrays.equals(searchTerms("-x"), normalizedSearchTerms("-x -xy -xyz")));
        assertTrue(Arrays.equals(searchTerms("-x"), normalizedSearchTerms("-xyz -xy -x")));
        assertTrue(Arrays.equals(searchTerms("-x"), normalizedSearchTerms("-xy -xyz -x")));
        assertTrue(Arrays.equals(searchTerms("-xyz"), normalizedSearchTerms("-xyz -xyz -xyz")));
        assertTrue(Arrays.equals(searchTerms("-black"), normalizedSearchTerms("-black -blackened")));
        assertTrue(Arrays.equals(searchTerms("-his"), normalizedSearchTerms("-this -his")));

        assertTrue(Arrays.equals(searchTerms("-this -blackened"), normalizedSearchTerms("-blackened -this")));
        assertTrue(Arrays.equals(searchTerms("-this -blackened"), normalizedSearchTerms("-this -blackened")));
    }

    public void testNormalizeRequiredSearchTerms() {
        assertTrue(Arrays.equals(searchTerms("+"), normalizedSearchTerms("+")));
        assertTrue(Arrays.equals(searchTerms("+x"), normalizedSearchTerms("+x")));
        assertTrue(Arrays.equals(searchTerms("+x +Y +z"), normalizedSearchTerms("+x +Y +z")));
        assertTrue(Arrays.equals(searchTerms("+x +xy +xyz"), normalizedSearchTerms("+x +xy +xyz")));
        assertTrue(Arrays.equals(searchTerms("+xyz +x"), normalizedSearchTerms("+xyz xy +x")));
        assertTrue(Arrays.equals(searchTerms("+xyz +x"), normalizedSearchTerms("xy +xyz +x")));
        assertTrue(Arrays.equals(searchTerms("+xyz +xyz +xyz"), normalizedSearchTerms("+xyz +xyz +xyz")));
        assertTrue(Arrays.equals(searchTerms("+black +blackened"), normalizedSearchTerms("+black +blackened")));
        assertTrue(Arrays.equals(searchTerms("+this +his"), normalizedSearchTerms("+this +his")));

        assertTrue(Arrays.equals(searchTerms("+blackened +this"), normalizedSearchTerms("+blackened +this")));
        assertTrue(Arrays.equals(searchTerms("+this +blackened"), normalizedSearchTerms("+this +blackened")));
    }

    public void testNormalizeComplexSearchTerms() {
        assertTrue(Arrays.equals(searchTerms("+jay bluejay -jack"), normalizedSearchTerms("+jay bluejay -jack")));
        assertTrue(Arrays.equals(searchTerms("-black jailhouse"), normalizedSearchTerms("jail jailhouse -black -blackened")));
        assertTrue(Arrays.equals(searchTerms("-black \"jail house\""), normalizedSearchTerms("jail \"jail house\" -black -\"black knight\"")));
        assertTrue(Arrays.equals(searchTerms("-black +-\"black knight\" +jail \"jail house\""), normalizedSearchTerms("+jail \"jail house\" -black +-\"black knight\"")));
        assertTrue(Arrays.equals(searchTerms("-lamb -horse -alligator raccoon bird cat"), normalizedSearchTerms("bird cat raccoon -horse -lamb -alligator")));
    }

    public void testIsFilterRelaxed() {
        // removing last filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"x"}, new String[0]));
        // shortening filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx"}, new String[] {"x"}));
        // removing filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"xx"}));
        // removing filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"y"}));
        // removing and shorterning filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"x"}));
        // shortening filter term by multiple characters
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"x"}));
        // shortening filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xy"}));

        assertFalse(TextMatchers.isFilterRelaxed(new String[0], new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {""}, new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "xy", "x"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz", "xyz"}));
    }

    public void testIsFilterEqual() {
        assertTrue(TextMatchers.isFilterEqual(new String[0], new String[0]));
        assertTrue(TextMatchers.isFilterEqual(new String[] {"x"}, new String[] {"x"}));
        assertTrue(TextMatchers.isFilterEqual(new String[] {"x", "y"}, new String[] {"x", "y"}));
    }

    public void testIsFilterConstrained() {
        // adding the first filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[0], new String[] {"x"}));
        // lengthening filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"x"}, new String[] {"xx"}));
        // adding filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"x"}, new String[] {"x", "y"}));
        // lengthening filter term by multiple characters
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"x"}, new String[] {"xyz"}));
        // lengthening multi character filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"xy"}, new String[] {"xyz"}));
        // removing search terms but covering the old with a single new
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"xyz", "xy", "x"}, new String[] {"xyzz"}));

        assertFalse(TextMatchers.isFilterConstrained(new String[] {"abc"}, new String[0]));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"abc"}, new String[] {""}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz", "abc"}, new String[] {"xyz"}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz"}, new String[] {"xyz", ""}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz", "xyz"}, new String[] {"xyz", ""}));
    }

    public void testMatcherConstrainedAndRelaxed() {
        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("black"), textMatcher("black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("black"), textMatcher("black")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher(""), textMatcher("black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher(""), textMatcher("black")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher("black"), textMatcher("blackened")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("black"), textMatcher("blackened")));

        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("blackened"), textMatcher("black")));
        assertTrue(TextMatchers.isMatcherRelaxed(textMatcher("blackened"), textMatcher("black")));

        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("black"), textMatcher("")));
        assertTrue(TextMatchers.isMatcherRelaxed(textMatcher("black"), textMatcher("")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher("black"), textMatcher("black white")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("black"), textMatcher("black white")));
    }

    public void testMatcherConstrainedAndRelaxedWithNegation() {
        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("-black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("-black")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher(""), textMatcher("-black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher(""), textMatcher("-black")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("-blackened")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("-blackened")));

        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("-blackened"), textMatcher("-black")));
        assertTrue(TextMatchers.isMatcherRelaxed(textMatcher("-blackened"), textMatcher("-black")));

        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("")));
        assertTrue(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("-black -white")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("-black -white")));
    }

    public void testMatcherConstrainedWithModeDifferences() {
        TextMatcher<String> matcherA = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        TextMatcher<String> matcherB = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH, TextMatcherEditor.IDENTICAL_STRATEGY);

        assertFalse(TextMatchers.isMatcherRelaxed(matcherA, matcherB));
        assertTrue(TextMatchers.isMatcherConstrained(matcherA, matcherB));

        assertTrue(TextMatchers.isMatcherRelaxed(matcherB, matcherA));
        assertFalse(TextMatchers.isMatcherConstrained(matcherB, matcherA));
    }

    public void testMatcherConstrainedWithStrategyDifferences() {
        TextMatcher<String> matcherA = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        TextMatcher<String> matcherB = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.NORMALIZED_STRATEGY);

        assertFalse(TextMatchers.isMatcherRelaxed(matcherA, matcherB));
        assertFalse(TextMatchers.isMatcherConstrained(matcherA, matcherB));

        assertFalse(TextMatchers.isMatcherRelaxed(matcherB, matcherA));
        assertFalse(TextMatchers.isMatcherConstrained(matcherB, matcherA));
    }

    private SearchTerm[] normalizedSearchTerms(String text) {
        return TextMatchers.normalizeSearchTerms(searchTerms(text), (TextSearchStrategy.Factory) TextMatcherEditor.IDENTICAL_STRATEGY);
    }

    private SearchTerm[] searchTerms(String text) {
        return textMatcher(text).getSearchTerms();
    }

    private TextMatcher<String> textMatcher(String text) {
        return new TextMatcher<String>(TextMatchers.parse(text), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
    }
}