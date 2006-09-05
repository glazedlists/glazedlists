/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.GlazedLists;

/**
 * Models a price on Amazon.
 *
 * @author James Lemieux
 */
public class ListPrice implements Comparable<ListPrice> {

    private int amount;
    private String currencyCode;
    private String formattedPrice;

    /**
     * The price of the Item in pennies, with no decimal (e.g. 2399).
     */
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    /**
     * The code of the currency (e.g. USD).
     */
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    /**
     * A formatted version of the price (e.g. $23.99).
     */
    public String getFormattedPrice() { return formattedPrice; }
    public void setFormattedPrice(String formattedPrice) { this.formattedPrice = formattedPrice; }

    /**
     * ListPrice are ordered by their amount by default.
     */
    public int compareTo(ListPrice o) {
        return GlazedLists.comparableComparator().compare(new Integer(getAmount()), new Integer(o.getAmount()));
    }

    /** inheritDoc */
    public String toString() {
        return formattedPrice;
    }
}