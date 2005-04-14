/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

/**
 * Map an {@link Object} to an alternate value, for use with a {@link SwapList}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(1), writes O(1), source changes O(C*N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>4 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface AlternateFinder {
    
    /**
     * Create an alternate value for the specified element from the source
     * {@link EventList}.
     */
    public Object createAlternate(Object sourceElement);
    
    /**
     * Update the specified alternate in response to the element in the source
     * {@link EventList} being updated.
     *
     * @param sourceElement the Object from the source {@link EventList} that
     *      has been updated.
     * @param previousAlternate the original alternate for the source element.
     * @return the new alternate for the source element. This may be the same
     *      Object as the previous alternate.
     */
    public Object updateAlternate(Object sourceElement, Object previousAlternate);
    
    /**
     * Delete the specified alternate in response to the element in the source
     * {@link EventList} being removed. 
     *
     * @param previousAlternate the original alternate for the source element.
     */
    public void deleteAlternate(Object previousAlternate);
    
}
