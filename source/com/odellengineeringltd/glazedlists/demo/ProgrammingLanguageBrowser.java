/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.demo;

import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.jcombobox.*;
import com.odellengineeringltd.glazedlists.jtable.*;
import com.odellengineeringltd.glazedlists.jlist.*;
import com.odellengineeringltd.glazedlists.util.*;
import com.odellengineeringltd.glazedlists.test.*;
import javax.swing.*;
import java.awt.*;

// test
import java.lang.reflect.*;
// endtest

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ProgrammingLanguageBrowser {

    EventList languages;
    SortedList sortedLanguages;
    CaseInsensitiveFilterList filteredLanguages;
    ProgrammingLanguageFilter customFilteredLanguages;

    ProgrammingLanguageNameComparator sortByName = new ProgrammingLanguageNameComparator();
    ProgrammingLanguageYearComparator sortByYear = new ProgrammingLanguageYearComparator();

    public ProgrammingLanguageBrowser() {
        languages = new BasicEventList();
        languages.add(new ProgrammingLanguage("Java", "1995", "Object-oriented virtual machine language by Sun Microsystems", true, true));
        languages.add(new ProgrammingLanguage("C", "1973", "The UNIX language originally developed for the PDP-11", false, false));
        languages.add(new ProgrammingLanguage("C++", "1983", "Object-oriented C", true, false));
        languages.add(new ProgrammingLanguage("BASIC", "1964", "Beginner's All Purpose Symbolic Instruction Code", false, false));
        languages.add(new ProgrammingLanguage("COBOL", "1960", "COmmon Business Oriented Language", false, false));
        languages.add(new ProgrammingLanguage("Eiffel", "1987", "Object-oriented language encouraging code simplicity", true, false));
        languages.add(new ProgrammingLanguage("Fortran", "1954", "Formula Translation language for scientific computation", false, false));
        languages.add(new ProgrammingLanguage("Lisp", "1958", "Heavily Recursive language for AI programming", false, false));
        languages.add(new ProgrammingLanguage("Perl", "1987", "The \"More than one way to do it\" scripting language", false, true));
        languages.add(new ProgrammingLanguage("PHP", "1994", "HTML-embedded scripting language", true, false));
        languages.add(new ProgrammingLanguage("Python", "1986", "Clear syntax object-oriented programming language", true, false));
        languages.add(new ProgrammingLanguage("Ruby", "1993", "Object-oriented scripting language", true, false));
        languages.add(new ProgrammingLanguage("Visual Basic", "1992", "QuickBasic with a visual UI designer", false, false));
        
        sortedLanguages = new SortedList(languages, sortByName);
        filteredLanguages = new CaseInsensitiveFilterList(sortedLanguages);
        customFilteredLanguages = new ProgrammingLanguageFilter(filteredLanguages);
    }
    
    public void display() {
        // start with a list of tabs for each display type
        JTabbedPane listWidgetTabs = new JTabbedPane(JTabbedPane.BOTTOM);

        // add a JTable
        ListTable listTable = new ListTable(customFilteredLanguages, new ProgrammingLanguageTableCell());
        TableComparatorChooser tableSorter = new TableComparatorChooser(listTable, sortedLanguages, false);
        tableSorter.getComparatorsForColumn(0).clear();
        tableSorter.getComparatorsForColumn(0).add(new ProgrammingLanguageNameComparator());
        tableSorter.getComparatorsForColumn(0).add(new ProgrammingLanguageYearComparator());
        listWidgetTabs.addTab("JTable", listTable.getTableScrollPane());
        
        // add a JList
        EventJList eventJList = new EventJList(customFilteredLanguages);
        listWidgetTabs.addTab("JList", new JScrollPane(eventJList.getJList()));
        
        // add a JComboBox
        ListComboBoxModel comboModel = new ListComboBoxModel(customFilteredLanguages);
        JComboBox eventJComboBox = new JComboBox(comboModel);
        listWidgetTabs.addTab("JComboBox", new JScrollPane(eventJComboBox));

        // assemble the window
        JFrame frame = new JFrame("Programming Languages");
        ExitOnCloseHandler.addToFrame(frame);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(new JLabel("Filter"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        frame.getContentPane().add(filteredLanguages.getFilterEdit(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        frame.getContentPane().add(customFilteredLanguages.getObjectOrientedCheckBox(), new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        frame.getContentPane().add(customFilteredLanguages.getVirtualMachineCheckBox(), new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        frame.getContentPane().add(listWidgetTabs, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        
        // display the window
        frame.setSize(640, 480);
        frame.show();
    }
    
    class BackwardsStringComparator implements java.util.Comparator {
        private ComparableComparator cc = new ComparableComparator();
        public int compare(Object alpha, Object beta) {
            ProgrammingLanguage alphaString = (ProgrammingLanguage)alpha;
            ProgrammingLanguage betaString = (ProgrammingLanguage)beta;
            return cc.compare(reverse(alphaString.getName()), reverse(betaString.getName()));
        }
        public String reverse(String a) {
            char[] ch = a.toCharArray();
            char[] ar = new char[ch.length];
            for(int i = 0; i < ch.length; i++) {
                ar[ch.length - i - 1] = ch[i];
            }
            return new String(ar);
        }
    }
    
    public static void main(String[] args) {
        ProgrammingLanguageBrowser browser = new ProgrammingLanguageBrowser();
        browser.display();
    }
}
