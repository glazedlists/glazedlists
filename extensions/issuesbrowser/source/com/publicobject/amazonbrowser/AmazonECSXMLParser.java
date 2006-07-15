/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import com.publicobject.misc.xml.*;
import com.publicobject.misc.util.concurrent.PooledExecutor;
import com.publicobject.misc.util.concurrent.LinkedQueue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AmazonECSXMLParser {

    private static final DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat dateFormat2 = new SimpleDateFormat("yyyy");
    private static final DateFormat[] dateFormats = {dateFormat1, dateFormat2};

    private static final Parser ITEM_LOOKUP_PARSER = new Parser();
    static {
        // configure the Parser for Item Lookups

        // Parsing instructions for Item
        final XMLTagPath startItemTag = XMLTagPath.startTagPath("ItemLookupResponse Items Item");
        final XMLTagPath endItemTag = startItemTag.end();
        ITEM_LOOKUP_PARSER.addProcessor(startItemTag, Processors.createNewObject(Item.class, startItemTag));

        final XMLTagPath endASINTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ASIN");
        final XMLTagPath endDetailPageURLTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item DetailPageURL");
        ITEM_LOOKUP_PARSER.addProcessor(endASINTag,          Processors.setterMethod(startItemTag, endASINTag, Item.class, "aSIN"));
        ITEM_LOOKUP_PARSER.addProcessor(endDetailPageURLTag, Processors.setterMethod(startItemTag, endDetailPageURLTag, Item.class, "detailPageURL"));
        ITEM_LOOKUP_PARSER.addProcessor(endItemTag,          Processors.addObjectToTargetList(startItemTag));

        // Parsing instructions for ItemAttributes
        final XMLTagPath startItemAttributesTag = XMLTagPath.startTagPath("ItemLookupResponse Items Item ItemAttributes");
        final XMLTagPath endItemAttributesTag = startItemAttributesTag.end();
        ITEM_LOOKUP_PARSER.addProcessor(startItemAttributesTag, Processors.createNewObject(ItemAttributes.class, startItemAttributesTag));

        final XMLTagPath endAudienceRatingTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes AudienceRating");
        final XMLTagPath endDirectorTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes Director");
        final XMLTagPath endReleaseDateTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes ReleaseDate");
        final XMLTagPath endTheatricalReleaseDateTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes TheatricalReleaseDate");
        final XMLTagPath endTitleTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes Title");
        ITEM_LOOKUP_PARSER.addProcessor(endAudienceRatingTag,        Processors.setterMethod(startItemAttributesTag, endAudienceRatingTag, ItemAttributes.class, "audienceRating", new AudienceRatingConverter()));
        ITEM_LOOKUP_PARSER.addProcessor(endDirectorTag,              Processors.setterMethod(startItemAttributesTag, endDirectorTag, ItemAttributes.class, "director"));
        ITEM_LOOKUP_PARSER.addProcessor(endReleaseDateTag,           Processors.setterMethod(startItemAttributesTag, endReleaseDateTag, ItemAttributes.class, "releaseDate", Converters.date(dateFormats)));
        ITEM_LOOKUP_PARSER.addProcessor(endTheatricalReleaseDateTag, Processors.setterMethod(startItemAttributesTag, endTheatricalReleaseDateTag, ItemAttributes.class, "theatricalReleaseDate", Converters.date(dateFormats)));
        ITEM_LOOKUP_PARSER.addProcessor(endTitleTag,                 Processors.setterMethod(startItemAttributesTag, endTitleTag, ItemAttributes.class, "title"));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag,    Processors.setterMethod(startItemTag, startItemAttributesTag, Item.class, "itemAttributes"));

        // Parsing instructions for ListPrice
        final XMLTagPath startListPriceTag = XMLTagPath.startTagPath("ItemLookupResponse Items Item ItemAttributes ListPrice");
        final XMLTagPath endListPriceTag = startListPriceTag.end();
        ITEM_LOOKUP_PARSER.addProcessor(startListPriceTag, Processors.createNewObject(ListPrice.class, startListPriceTag));

        final XMLTagPath endAmountTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes ListPrice Amount");
        final XMLTagPath endCurrencyCodeTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes ListPrice CurrencyCode");
        final XMLTagPath endFormattedPriceTag = XMLTagPath.endTagPath("ItemLookupResponse Items Item ItemAttributes ListPrice FormattedPrice");
        ITEM_LOOKUP_PARSER.addProcessor(endAmountTag,         Processors.setterMethod(startListPriceTag, endAmountTag, ListPrice.class, "amount", Converters.integer()));
        ITEM_LOOKUP_PARSER.addProcessor(endCurrencyCodeTag,   Processors.setterMethod(startListPriceTag, endCurrencyCodeTag, ListPrice.class, "currencyCode"));
        ITEM_LOOKUP_PARSER.addProcessor(endFormattedPriceTag, Processors.setterMethod(startListPriceTag, endFormattedPriceTag, ListPrice.class, "formattedPrice"));
        ITEM_LOOKUP_PARSER.addProcessor(endListPriceTag,  Processors.setterMethod(startItemAttributesTag, startListPriceTag, ItemAttributes.class, "listPrice"));
    }

    private static final Parser ITEM_SEARCH_PARSER = new Parser();
    static {
        // configure the Parser for Item Searches
        XMLTagPath endASINTag = XMLTagPath.endTagPath("ItemSearchResponse Items Item ASIN");
        ITEM_SEARCH_PARSER.addProcessor(endASINTag, Processors.addObjectToTargetList(endASINTag));
    }

    private static final XMLTagPath ITEM_SEARCH_RESULTS_PAGE_COUNT = XMLTagPath.endTagPath("ItemSearchResponse Items TotalPages");

    private static ItemFetcher ITEM_FETCHER;

    /**
     * When executed, this opens a file specified on the command line, parses
     * it for Issuezilla XML and writes the issues to the command line.
     */
    public static void main(String[] args) throws IOException {
        EventList<Item> itemsList = new BasicEventList<Item>();
//        loadItem(itemsList, "B000B5XOW0");

        searchAndLoadItems(itemsList, "king");
    }

    /**
     * Search for all Items with the given <code>keywords</code> and load them.
     */
    public static void searchAndLoadItems(EventList<Item> target, String keywords) throws IOException {
        // clean up the previous fetcher, which may be currently executing
        if (ITEM_FETCHER != null)
            ITEM_FETCHER.shutdownNow();

        // create a new ItemFetcher
        ITEM_FETCHER = new ItemFetcher(target);

        // This EventList acts as a buffer between the ItemSearch and the ItemLookups.
        // As ASINs are added to it by parsing ItemSearch results, the ITEM_FETCHER
        // is notified and responds by placing a Runnable in a Channel that will eventually
        // be processed in a PooledExecutor. That Runnable executes the ItemLookup based on
        // the ASIN and the Item that is parsed out of the returned XML is placed into target.
        final EventList<String> itemASINs = new BasicEventList<String>();
        itemASINs.addListEventListener(ITEM_FETCHER);

        try {
            // prepare a stream to determine the number of pages in the result set
            final String searchURLStringBase = "http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=10GN481Z3YQ4S67KNSG2&Operation=ItemSearch&SearchIndex=DVD&ResponseGroup=ItemIds&Keywords=" + keywords;
            InputStream itemSearchResultsStream = getInputStream(searchURLStringBase, 10);

            // parse the page count from the stream
            final String pageCountString = (String) ITEM_SEARCH_PARSER.parse(ITEM_SEARCH_RESULTS_PAGE_COUNT, itemSearchResultsStream);
            final int pageCount = Integer.parseInt(pageCountString);
            itemSearchResultsStream.close();

            // retrieve each page in the result set and parse the ASINs out of them
            for (int i = 1; i <= pageCount; i++) {
                // build a URL appropriate for fetching 1 page of 10 search results
                final String searchPageURLString = searchURLStringBase + "&ItemPage=" + i;

                try {
                    // parse the ASINs from the search results page
                    itemSearchResultsStream = getInputStream(searchPageURLString, 20);
                    ITEM_SEARCH_PARSER.parse(itemASINs, itemSearchResultsStream);

                } catch (IOException ioe) {
                    // simply log the error to Standard Error and continue - these errors are fairly routinely from Amazon's webservice
                    System.err.println("IOException while fetching page " + i + " of " + pageCount + " pages in the search result.");
                    ioe.printStackTrace(System.err);

                } finally {
                    itemSearchResultsStream.close();
                }
            }

        } finally {
            // always stop listening for new ASINs to fetch when we're done parsing all pages of the search result
            itemASINs.removeListEventListener(ITEM_FETCHER);
        }
    }

    /**
     * Loads the item with the specified <code>asin</code>.
     */
    public static void loadItem(EventList<Item> target, String asin) throws IOException {
        final String urlString = "http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=10GN481Z3YQ4S67KNSG2&Operation=ItemLookup&ResponseGroup=ItemAttributes&ItemId=" + asin;
        final InputStream itemsInputStream = getInputStream(urlString, 1);

        try {
            // parse the stream
            ITEM_LOOKUP_PARSER.parse(target, itemsInputStream);
        } finally {
            itemsInputStream.close();
        }

        System.out.println("loaded item " + target.size() + ": " + asin);
    }

    /**
     * A convenience method to attempt to establish a URL connection to the
     * given <code>urlString</code> and return its {@link InputStream}. If no
     * connection is established after  the given number of <code>tries</code>,
     * the last {@link IOException} that was received is thrown.
     *
     * @param urlString the URL to open and return the {@link InputStream} from
     * @param tries the number of attempts to make before reporting a failure
     * @return the {@link InputStream} of the URL connection that was established
     * @throws IOException if no connection could be established to the <code>urlString</code>
     */
    private static InputStream getInputStream(String urlString, int tries) throws IOException {
        final URL issuesUrl = new URL(urlString);

        while (tries > 0) {
            try {
                return issuesUrl.openStream();
            } catch (IOException e) {
                tries--;
                if (tries == 0)
                    throw e;
            }
        }

        // this should never happen in practice, but the compiler doesn't know that!
        throw new RuntimeException("Unable to obtain URL connection to: " + urlString);
    }

    /**
     * This Converter can lookup AudienceRating objects using Strings.
     */
    private static class AudienceRatingConverter implements Converter {
        public Object convert(String value) {
            return AudienceRating.lookup(value);
        }
    }

    /**
     * This class listens for ASINs to be added to a List and reacts to each new
     * addition by enqueuing a {@link Runnable} to be executed by a pool of
     * Threads. When the Runnable executes it executes an ItemLookup against
     * Amazon's ECS webservice to fetch the details for the product identified
     * by the ASIN.
     */
    private static class ItemFetcher implements ListEventListener<String> {

        /** The list of all Items to populate */
        private final EventList<Item> target;

        /** A pool of 50 Threads servicing an unbounded Channel. */
        private final PooledExecutor threadPool = new PooledExecutor(new LinkedQueue(), 50);

        public ItemFetcher(EventList<Item> target) {
            this.target = target;
        }

        /**
         * This method stops the internal Thread Pool from servicing any further Runnables.
         */
        public void shutdownNow() {
            threadPool.shutdownNow();
        }

        public void listChanged(ListEvent<String> listChanges) {
            final EventList<String> source = listChanges.getSourceList();

            while (listChanges.next()) {
                final int changeType = listChanges.getType();

                // only react to ASIN insertions
                if (changeType == ListEvent.INSERT) {
                    // 1. lookup the ASIN
                    final int sourceIndex = listChanges.getIndex();
                    final String asin = source.get(sourceIndex);

                    // 2. build a Runnable that will fetch the details for the ASIN
                    final LoadItemRunnable command = new LoadItemRunnable(target, asin);

                    // enqueue the Runnable in the Thread Pool's Channel
                    try {
                        threadPool.execute(command);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException while looking up item: " + asin);
                    }
                }
            }
        }

        /**
         * This Runnable fetches the details for an individual Amazon product
         * identified by its ASIN (Amazon Standard Identification Number)
         */
        private static class LoadItemRunnable implements Runnable {
            private static final int MAX_RETRIES = 10;

            private final EventList<Item> target;
            private final String asin;

            public LoadItemRunnable(EventList<Item> target, String asin) {
                this.target = target;
                this.asin = asin;
            }

            public void run() {
                for (int retries = MAX_RETRIES; retries > 0; retries--) {
                    try {
                        loadItem(target, asin);

                        if (retries != MAX_RETRIES)
                            System.out.println("SUCCEEDED IN FETCHING " + asin + " AFTER " + (MAX_RETRIES - retries) + " ATTEMPTS.");
                        return;
                    } catch (IOException e) {
                        retries--;

                        if (retries == 0) {
                            System.err.println("IOException while looking up item: " + asin);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}