/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.matchers.MatcherEditor;

import javax.swing.*;

/**
 * // TODO:
 * 1. Convert priority to be a MatcherEditor instead of Threshold
 * 2. Figure out layout of users list
 * 3. Make 'em closable
 * 4. Make 'em addable
 * 5. Use CompositeMatcherEditor
 * 6. Dynamic Layout
 * 7. Date MatcherEditor
 * 8. Component/Subcomponent
 * 9. 
 *
 *
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface FilterComponent<E> {

    String getName();

    JComponent getComponent();

    MatcherEditor<E> getMatcherEditor();

    void dispose(); 
}