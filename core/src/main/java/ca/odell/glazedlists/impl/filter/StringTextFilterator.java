/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.TextFilterator;

import java.util.List;

/**
 * TextFilterator that uses an Object's toString() value.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class StringTextFilterator<E> implements TextFilterator<E> {
    /** {@inheritDoc} */
    @Override
    public void getFilterStrings(List<String> baseList, E element) {
        if (element != null) baseList.add(element.toString());
    }
}