package com.publicobject.amazonbrowser;

/**
 * Models a price on Amazon.
 *
 * @author James Lemieux
 */
public class ListPrice {

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

    public String toString() {
        return formattedPrice;
    }
}