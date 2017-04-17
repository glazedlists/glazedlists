/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.matchers.MatcherEditor;

import javax.swing.*;

/**
 * A matcher editor plus a component to manipulate it with.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface FilterComponent<E> {

    JComponent getComponent();

    MatcherEditor<E> getMatcherEditor();
}