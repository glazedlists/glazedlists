/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class TextMatchersTest {

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testParseFields() {
        SearchEngineTextMatcherEditor.Field<String> blahField = new SearchEngineTextMatcherEditor.Field<String>("blah", GlazedLists.toStringTextFilterator());
        SearchEngineTextMatcherEditor.Field<String> burpField = new SearchEngineTextMatcherEditor.Field<String>("burp", GlazedLists.toStringTextFilterator());

        Set<SearchEngineTextMatcherEditor.Field<String>> fields = new HashSet<SearchEngineTextMatcherEditor.Field<String>>();
        fields.add(blahField);
        fields.add(burpField);

        SearchTerm[] terms = TextMatchers.parse("blah:burp");
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah:burp", false, false, null);

        terms = TextMatchers.parse("\"blah:stuff\"", fields);
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "blah:stuff", false, false, null);

        terms = TextMatchers.parse("blah:burp", fields);
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "burp", false, false, blahField);

        terms = TextMatchers.parse("blah:stuff burp:blech", fields);
        assertEquals(2, terms.length);
        checkFilterTerm(terms[0], "stuff", false, false, blahField);
        checkFilterTerm(terms[1], "blech", false, false, burpField);

        terms = TextMatchers.parse("blah: stuff burp: blech", fields);
        assertEquals(2, terms.length);
        checkFilterTerm(terms[0], "stuff", false, false, null);
        checkFilterTerm(terms[1], "blech", false, false, null);

        terms = TextMatchers.parse("blah:\" stuff burp: blech \"", fields);
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], " stuff burp: blech ", false, false, blahField);

        terms = TextMatchers.parse("blah:burp:blech", fields);
        assertEquals(1, terms.length);
        checkFilterTerm(terms[0], "burp:blech", false, false, blahField);

        terms = TextMatchers.parse("blah: burp:", fields);
        assertEquals(0, terms.length);
    }

    private void checkFilterTerm(SearchTerm term, String text, boolean negated, boolean required) {
        checkFilterTerm(term, text, negated, required, null);
    }

    private void checkFilterTerm(SearchTerm term, String text, boolean negated, boolean required, SearchEngineTextMatcherEditor.Field field) {
        assertEquals(text, term.getText());
        assertEquals(negated, term.isNegated());
        assertEquals(required, term.isRequired());
        assertSame(field, term.getField());
    }

    @Test
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

    @Test
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

    @Test
    public void testNormalizeFieldfulSearchTerms() {
        SearchEngineTextMatcherEditor.Field<String> blahField = new SearchEngineTextMatcherEditor.Field<String>("blah", GlazedLists.toStringTextFilterator());
        SearchEngineTextMatcherEditor.Field<String> burpField = new SearchEngineTextMatcherEditor.Field<String>("burp", GlazedLists.toStringTextFilterator());

        Set<SearchEngineTextMatcherEditor.Field<String>> fields = new HashSet<SearchEngineTextMatcherEditor.Field<String>>();
        fields.add(blahField);
        fields.add(burpField);

        assertTrue(Arrays.equals(searchTerms("-blah:cola", fields), normalizedSearchTerms("-blah:cola", fields)));
        assertTrue(Arrays.equals(searchTerms("-blah:cola XX", fields), normalizedSearchTerms("-blah:cola X XX", fields)));
        assertTrue(Arrays.equals(searchTerms("-blah:cola cocacola", fields), normalizedSearchTerms("cola cocacola -blah:cola", fields)));
        assertTrue(Arrays.equals(searchTerms("-blah:cola -law cocacola", fields), normalizedSearchTerms("-law -lawyer cola cocacola -blah:cola", fields)));
    }

    @Test
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

    @Test
    public void testNormalizeComplexSearchTerms() {
        assertTrue(Arrays.equals(searchTerms("+jay bluejay -jack"), normalizedSearchTerms("+jay bluejay -jack")));
        assertTrue(Arrays.equals(searchTerms("-black jailhouse"), normalizedSearchTerms("jail jailhouse -black -blackened")));
        assertTrue(Arrays.equals(searchTerms("-black \"jail house\""), normalizedSearchTerms("jail \"jail house\" -black -\"black knight\"")));
        assertTrue(Arrays.equals(searchTerms("-black +-\"black knight\" +jail \"jail house\""), normalizedSearchTerms("+jail \"jail house\" -black +-\"black knight\"")));
        assertTrue(Arrays.equals(searchTerms("-lamb -horse -alligator raccoon bird cat"), normalizedSearchTerms("bird cat raccoon -horse -lamb -alligator")));
    }

    @Test
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

    @Test
    public void testMatcherConstrainedAndRelaxedWithNegation() {
        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("-black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("-black")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher(""), textMatcher("-black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher(""), textMatcher("-black")));

        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("-blackened")));
        assertTrue(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("-blackened")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher("-blackened"), textMatcher("-black")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("-blackened"), textMatcher("-black")));

        assertFalse(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("")));
        assertTrue(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("")));

        assertTrue(TextMatchers.isMatcherConstrained(textMatcher("-black"), textMatcher("-black -white")));
        assertFalse(TextMatchers.isMatcherRelaxed(textMatcher("-black"), textMatcher("-black -white")));
    }

    @Test
    public void testMatcherConstrainedAndRelaxedWithFields() {
        SearchEngineTextMatcherEditor.Field<String> blahField = new SearchEngineTextMatcherEditor.Field<String>("blah", GlazedLists.toStringTextFilterator());
        SearchEngineTextMatcherEditor.Field<String> burpField = new SearchEngineTextMatcherEditor.Field<String>("burp", GlazedLists.toStringTextFilterator());

        Set<SearchEngineTextMatcherEditor.Field<String>> fields = new HashSet<SearchEngineTextMatcherEditor.Field<String>>();
        fields.add(blahField);
        fields.add(burpField);

        final TextMatcher blackBlahMatcher = textMatcher("blah:black", Collections.singleton(blahField));
        final TextMatcher blackBurpMatcher = textMatcher("burp:black", Collections.singleton(burpField));

        assertFalse(blackBlahMatcher.equals(blackBurpMatcher));
        assertFalse(TextMatchers.isMatcherRelaxed(blackBlahMatcher, blackBurpMatcher));
        assertFalse(TextMatchers.isMatcherConstrained(blackBlahMatcher, blackBurpMatcher));

        assertTrue(blackBlahMatcher.equals(textMatcher("blah:black", Collections.singleton(blahField))));
        assertFalse(TextMatchers.isMatcherRelaxed(blackBlahMatcher, blackBlahMatcher));
        assertFalse(TextMatchers.isMatcherConstrained(blackBurpMatcher, blackBurpMatcher));
    }

    @Test
    public void testMatcherConstrainedWithModeDifferences() {
        TextMatcher<String> matcherA = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        TextMatcher<String> matcherB = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH, TextMatcherEditor.IDENTICAL_STRATEGY);

        assertFalse(TextMatchers.isMatcherRelaxed(matcherA, matcherB));
        assertTrue(TextMatchers.isMatcherConstrained(matcherA, matcherB));

        assertTrue(TextMatchers.isMatcherRelaxed(matcherB, matcherA));
        assertFalse(TextMatchers.isMatcherConstrained(matcherB, matcherA));
    }

    @Test
    public void testMatcherConstrainedWithStrategyDifferences() {
        TextMatcher<String> matcherA = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        TextMatcher<String> matcherB = new TextMatcher<String>(TextMatchers.parse(""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.NORMALIZED_STRATEGY);

        assertFalse(TextMatchers.isMatcherRelaxed(matcherA, matcherB));
        assertFalse(TextMatchers.isMatcherConstrained(matcherA, matcherB));

        assertFalse(TextMatchers.isMatcherRelaxed(matcherB, matcherA));
        assertFalse(TextMatchers.isMatcherConstrained(matcherB, matcherA));
    }

    @Test
    public void testRegularExpressionMatcher() {
        TextMatcher<String> matcher = new TextMatcher<String>(TextMatchers.parse("[a-z]"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.REGULAR_EXPRESSION, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertEquals(TextMatcherEditor.REGULAR_EXPRESSION, matcher.getMode());
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("x"));
        assertFalse(matcher.matches("A"));
    }

    @Test
    public void testExactExpressionMatcher() {
        TextMatcher<String> matcher = new TextMatcher<String>(TextMatchers.parse("abc"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.EXACT, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertEquals(TextMatcherEditor.EXACT, matcher.getMode());
        assertFalse(matcher.matches("a"));
        assertFalse(matcher.matches("ab"));
        assertTrue(matcher.matches("abc"));
        assertFalse(matcher.matches("abcd"));
    }

    @Test
    public void testExactExpressionConstrainedAndRelaxed() {
        TextMatcher<String> matcherA = new TextMatcher<String>(TextMatchers.parse("reactor core"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.EXACT, TextMatcherEditor.IDENTICAL_STRATEGY);
        TextMatcher<String> matcherB = new TextMatcher<String>(TextMatchers.parse("reactor"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.EXACT, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertFalse(TextMatchers.isMatcherRelaxed(matcherA, matcherB));
        assertFalse(TextMatchers.isMatcherRelaxed(matcherB, matcherA));
        assertFalse(TextMatchers.isMatcherConstrained(matcherA, matcherB));
        assertFalse(TextMatchers.isMatcherConstrained(matcherB, matcherA));

        assertFalse(TextMatchers.isMatcherRelaxed(matcherA, matcherA));
        assertFalse(TextMatchers.isMatcherRelaxed(matcherB, matcherB));
        assertFalse(TextMatchers.isMatcherConstrained(matcherA, matcherA));
        assertFalse(TextMatchers.isMatcherConstrained(matcherB, matcherB));
    }

    @Test
    public void testConstructor() {
        new TextMatcher<String>(TextMatchers.parse("[a-z]"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.REGULAR_EXPRESSION, TextMatcherEditor.IDENTICAL_STRATEGY);

        try {
            new TextMatcher<String>(TextMatchers.parse("[a-z]"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.REGULAR_EXPRESSION, TextMatcherEditor.NORMALIZED_STRATEGY);
            fail("failed to receive an IllegalArgumentException using REGULAR_EXPRESSION and NORMALIZED_STRATEGY together");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private SearchTerm[] normalizedSearchTerms(String text) {
        return TextMatchers.normalizeSearchTerms(searchTerms(text), (TextSearchStrategy.Factory) TextMatcherEditor.IDENTICAL_STRATEGY);
    }

    private SearchTerm[] normalizedSearchTerms(String text, Set<SearchEngineTextMatcherEditor.Field<String>> fields) {
        return TextMatchers.normalizeSearchTerms(searchTerms(text, fields), (TextSearchStrategy.Factory) TextMatcherEditor.IDENTICAL_STRATEGY);
    }

    private SearchTerm[] searchTerms(String text) {
        return textMatcher(text).getSearchTerms();
    }

    private SearchTerm[] searchTerms(String text, Set<SearchEngineTextMatcherEditor.Field<String>> fields) {
        return textMatcher(text, fields).getSearchTerms();
    }

    private TextMatcher<String> textMatcher(String text) {
        return new TextMatcher<String>(TextMatchers.parse(text), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
    }

    private TextMatcher<String> textMatcher(String text, Set<SearchEngineTextMatcherEditor.Field<String>> fields) {
        return new TextMatcher<String>(TextMatchers.parse(text, fields), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
    }
}
