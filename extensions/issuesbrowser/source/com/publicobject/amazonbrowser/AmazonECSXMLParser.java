/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import com.publicobject.misc.xml.*;
import com.publicobject.misc.util.concurrent.RateGate;
import com.publicobject.misc.util.concurrent.QueuedExecutor;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import HTTPClient.HTTPConnection;
import HTTPClient.ModuleException;
import HTTPClient.HTTPResponse;

import javax.swing.*;

public class AmazonECSXMLParser {

    private static final RateGate amazonRequestRate = new RateGate(200);

    private static final DateFormat[] dateFormats = {new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("yyyy")};
    private static final Parser ITEM_LOOKUP_PARSER = new Parser();
    static {
        // configure the Parser for Item Lookups

        // Parsing instructions for Item
        final XMLTagPath startItemTag = XMLTagPath.startTagPath("ItemLookupResponse Items Item");
        final XMLTagPath endItemTag = startItemTag.end();
        ITEM_LOOKUP_PARSER.addProcessor(startItemTag,                                        Processors.createNewObject(Item.class));
        ITEM_LOOKUP_PARSER.addProcessor(endItemTag.child("ASIN"),                            Processors.setterMethod(Item.class, "aSIN"));
        ITEM_LOOKUP_PARSER.addProcessor(endItemTag.child("DetailPageURL"),                   Processors.setterMethod(Item.class, "detailPageURL"));
        ITEM_LOOKUP_PARSER.addProcessor(endItemTag,                                          Processors.addObjectToTargetList());

        // Parsing instructions for ItemAttributes
        final XMLTagPath startItemAttributesTag = startItemTag.child("ItemAttributes");
        final XMLTagPath endItemAttributesTag = startItemAttributesTag.end();
        ITEM_LOOKUP_PARSER.addProcessor(startItemAttributesTag,                              Processors.createNewObject(ItemAttributes.class));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag.child("AudienceRating"),        Processors.setterMethod(ItemAttributes.class, "audienceRating", new AudienceRatingConverter()));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag.child("Director"),              Processors.setterMethod(ItemAttributes.class, "director"));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag.child("ReleaseDate"),           Processors.setterMethod(ItemAttributes.class, "releaseDate", Converters.date(dateFormats)));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag.child("TheatricalReleaseDate"), Processors.setterMethod(ItemAttributes.class, "theatricalReleaseDate", Converters.date(dateFormats)));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag.child("Title"),                 Processors.setterMethod(ItemAttributes.class, "title"));
        ITEM_LOOKUP_PARSER.addProcessor(endItemAttributesTag,                                Processors.setterMethod(Item.class, "itemAttributes"));

        // Parsing instructions for ListPrice
        final XMLTagPath startListPriceTag = startItemAttributesTag.child("ListPrice");
        final XMLTagPath endListPriceTag = startListPriceTag.end();
        ITEM_LOOKUP_PARSER.addProcessor(startListPriceTag,                                   Processors.createNewObject(ListPrice.class));
        ITEM_LOOKUP_PARSER.addProcessor(endListPriceTag.child("Amount"),                     Processors.setterMethod(ListPrice.class, "amount", Converters.integer()));
        ITEM_LOOKUP_PARSER.addProcessor(endListPriceTag.child("CurrencyCode"),               Processors.setterMethod(ListPrice.class, "currencyCode"));
        ITEM_LOOKUP_PARSER.addProcessor(endListPriceTag.child("FormattedPrice"),             Processors.setterMethod(ListPrice.class, "formattedPrice"));
        ITEM_LOOKUP_PARSER.addProcessor(endListPriceTag,                                     Processors.setterMethod(ItemAttributes.class, "listPrice"));
    }

    private static final Parser ITEM_SEARCH_PARSER = new Parser();
    static {
        // configure the Parser for Item Searches
        XMLTagPath endASINTag = XMLTagPath.endTagPath("ItemSearchResponse Items Item ASIN");
        ITEM_SEARCH_PARSER.addProcessor(endASINTag, Processors.addObjectToTargetList());
    }

    private static final XMLTagPath ITEM_SEARCH_RESULTS_PAGE_COUNT = XMLTagPath.endTagPath("ItemSearchResponse Items TotalPages");
    private static final XMLTagPath ITEM_SEARCH_RESULTS_TOTAL_RESULTS = XMLTagPath.endTagPath("ItemSearchResponse Items TotalResults");

    private static ItemFetcher itemFetcher;

    /**
     * When executed, this opens a file specified on the command line, parses
     * it for Issuezilla XML and writes the issues to the command line.
     */
    public static void main(String[] args) throws IOException {
        EventList<Item> itemsList = new BasicEventList<Item>();
//        loadItem(itemsList, "B000B5XOW0");

        searchAndLoadItems(itemsList, "king", null);
    }

    public static void searchAndLoadItems(EventList<Item> target, String keywords, JProgressBar progressBar) throws IOException {
        searchAndLoadItems("webservices.amazon.com", target, keywords, progressBar);
    }

    /**
     * Search for all Items with the given <code>keywords</code> and load them.
     */
    public static void searchAndLoadItems(String host, EventList<Item> target, String keywords, JProgressBar progressBar) throws IOException {
        if (itemFetcher != null)
            itemFetcher.dispose();

        // create a new ItemFetcher
        itemFetcher = new ItemFetcher(target, host, progressBar);

        // This EventList acts as a buffer between the ItemSearch and the ItemLookups.
        // As ASINs are added to it by parsing ItemSearch results, the ITEM_FETCHER
        // is notified and responds by placing a Runnable in a Channel that will eventually
        // be processed in a PooledExecutor. That Runnable executes the ItemLookup based on
        // the ASIN and the Item that is parsed out of the returned XML is placed into target.
        final EventList<String> itemASINs = new BasicEventList<String>();
        itemASINs.addListEventListener(itemFetcher);

        try {
            // create a connection for figuring out what we need to grab
            HTTPConnection searchConnection = new HTTPConnection(host);

            // prepare a stream to determine the number of pages in the result set
            final String searchUrlPath = "/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=10GN481Z3YQ4S67KNSG2&Operation=ItemSearch&SearchIndex=DVD&ResponseGroup=ItemIds&Keywords=" + keywords;

            // parse the number of results from the stream
            InputStream itemSearchResultsStream = getInputStream(searchConnection, searchUrlPath, 10);
            final Map<XMLTagPath, Object> parseContext = ITEM_SEARCH_PARSER.parse(itemSearchResultsStream);

            // fetch the total number of results
            final String totalResultsString = (String) parseContext.get(ITEM_SEARCH_RESULTS_TOTAL_RESULTS);
            final int totalResults = Integer.parseInt(totalResultsString);

            // fetch the total number of pages in the result set
            final String pageCountString = (String) parseContext.get(ITEM_SEARCH_RESULTS_PAGE_COUNT);
            final int pageCount = Integer.parseInt(pageCountString);

            // reset the progress bar's maximum
            if (progressBar != null) {
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setValue(0);
                progressBar.setMaximum(totalResults);
            }

            tryClose(itemSearchResultsStream);

            // retrieve each page in the result set and parse the ASINs out of them
            for (int i = 1; i <= pageCount; i++) {
                // build a URL appropriate for fetching 1 page of 10 search results
                final String searchPagePath = searchUrlPath + "&ItemPage=" + i;

                try {
                    // parse the ASINs from the search results page
                    itemSearchResultsStream = getInputStream(searchConnection, searchPagePath, 20);
                    ITEM_SEARCH_PARSER.parse(itemASINs, itemSearchResultsStream);

                } catch (IOException ioe) {
                    // simply log the error to Standard Error and continue - these errors are fairly routinely from Amazon's webservice
                    System.err.println("IOException while fetching page " + i + " of " + pageCount + " pages in the search result, \"" + ioe.getMessage() + "\"");

                } finally {
                    tryClose(itemSearchResultsStream);
                }
            }

        } finally {
            // always stop listening for new ASINs to fetch when we're done parsing all pages of the search result
            itemASINs.removeListEventListener(itemFetcher);
        }
    }

    /**
     * A convenience method to attempt to establish a URL connection to the
     * given <code>urlString</code> and return its {@link InputStream}. If no
     * connection is established after  the given number of <code>tries</code>,
     * the last {@link IOException} that was received is thrown.
     *
     * @param httpConnection the connection to the host of interest
     * @param path the http path, including possibly  parameters
     * @param tries the number of attempts to make before reporting a failure
     * @return the {@link InputStream} of the URL connection that was established
     * @throws IOException if no connection could be established to the <code>urlString</code>
     */
    private static InputStream getInputStream(HTTPConnection httpConnection, String path, int tries) throws IOException {
        while(true) {
            try {
                amazonRequestRate.passThrough();
                HTTPResponse response = httpConnection.Get(path);
                if(response.getStatusCode() != 200) throw new IOException("Unexpected status code " + response.getStatusCode());
                return response.getInputStream();
            } catch(ModuleException e) {
                tries--;
                if(tries <= 0) {
                    IOException ioException = new IOException();
                    ioException.initCause(e);
                    throw ioException;
                }
            }
        }
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
     * addition by enqueueing requests (and response handlers) for each ASIN.
     * Separate threads are used for requests as for responses in order to
     * pipeline the request.
     */
    private static class ItemFetcher implements ListEventListener<String> {
        private static final int NUMBER_OF_RETRIES = 10;

        /** The list of all Items to populate */
        private final EventList<Item> target;

        private final ProgressBarUpdater progressBarUpdater;

        /** A pool of Threads servicing an unbounded Channel. */
        private final QueuedExecutor responseQueue = new QueuedExecutor();
        private final QueuedExecutor requestQueue = new QueuedExecutor();

        /** a connection to the HTTP server of interest */
        private final HTTPConnection httpConnection;

        public ItemFetcher(EventList<Item> target, String host, JProgressBar progressBar) {
            this.target = target;
            this.progressBarUpdater = new ProgressBarUpdater(progressBar);
            this.target.addListEventListener(this.progressBarUpdater);

            this.httpConnection = new HTTPConnection(host);
        }

        private static class ProgressBarUpdater implements ListEventListener<Item> {
            private final JProgressBar progressBar;

            public ProgressBarUpdater(JProgressBar progressBar) {
                this.progressBar = progressBar;
            }

            public void listChanged(ListEvent<Item> listChanges) {
                if (progressBar != null) {
                    final int numLoaded = listChanges.getSourceList().size();
                    progressBar.setString(numLoaded + " of " + progressBar.getMaximum());
                    progressBar.setValue(numLoaded);
                }
            }
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
                    enqueue(requestQueue, new ItemRequester(NUMBER_OF_RETRIES, asin));
                }
            }
        }

        public void dispose() {
            target.removeListEventListener(this.progressBarUpdater);
        }

        /**
         * Post a request for an Amazon ASIN, and schedule a handler to receive
         * its response.
         */
        private class ItemRequester implements Runnable {
            private final int remainingRetries;
            private final String asin;

            public ItemRequester(int remainingRetries, String asin) {
                this.remainingRetries = remainingRetries;
                this.asin = asin;
            }

            public void run() {
                try {
                    // do the request
                    final String path = "/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=10GN481Z3YQ4S67KNSG2&Operation=ItemLookup&ResponseGroup=ItemAttributes&ItemId=" + asin;
                    amazonRequestRate.passThrough();
                    HTTPResponse response = httpConnection.Get(path);
                    // tell someone else to handle the response
                    enqueue(responseQueue, new ItemReceiver(remainingRetries, asin, response));
                } catch(IOException e) {
                    handleError(remainingRetries, asin, e);
                } catch(ModuleException e) {
                    handleError(remainingRetries, asin, e);
                }
            }
        }

        /**
         * Read an HTTP response containing data of an Amazon ASIN.
         */
        private class ItemReceiver implements Runnable {
            private final int remainingRetries;
            private final String asin;
            private final HTTPResponse response;
            public ItemReceiver(int remainingRetries, String asin, HTTPResponse response) {
                this.remainingRetries = remainingRetries;
                this.asin = asin;
                this.response = response;
            }
            public void run() {
                InputStream inputStream = null;
                try {
                    // get the stream
                    if(response.getStatusCode() != 200) {
                        throw new IOException("Received response code " + response.getStatusCode());
                    }
                    inputStream = response.getInputStream();
                    // parse it
                    ITEM_LOOKUP_PARSER.parse(target, inputStream);
                    System.out.println("loaded item " + target.size() + ": " + asin);
                } catch (ModuleException e) {
                    handleError(remainingRetries, asin, e);
                } catch (IOException e) {
                    handleError(remainingRetries, asin, e);
                } finally {
                    tryClose(inputStream);
                }
            }
        }

        public void enqueue(QueuedExecutor executor, Runnable runnable) {
            try {
                executor.execute(runnable);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void handleError(int retries, String asin, Exception exception) {
            if(retries > 0) {
                enqueue(requestQueue, new ItemRequester(retries - 1, asin));
            } else {
                System.err.println("Failed permanently on " + asin + ", \"" + exception.getMessage() + "\"");
            }
        }
    }

    private static void tryClose(InputStream inputStream) {
        try {
            if(inputStream != null) {
                inputStream.close();
            }
        } catch(Exception e) {
            // closing failed, we can't do anything
        }
    }
}