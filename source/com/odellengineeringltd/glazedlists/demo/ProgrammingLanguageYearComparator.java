/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.demo;

import java.util.Comparator;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ProgrammingLanguageYearComparator implements Comparator {

    public int compare(Object a, Object b) {
        ProgrammingLanguage languageA = (ProgrammingLanguage)a;
        ProgrammingLanguage languageB = (ProgrammingLanguage)b;
        
        return languageA.getYear().compareTo(languageB.getYear());
    }
}
