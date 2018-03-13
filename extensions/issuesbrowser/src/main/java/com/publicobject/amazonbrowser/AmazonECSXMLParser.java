/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import com.publicobject.misc.util.concurrent.QueuedExecutor;
import com.publicobject.misc.util.concurrent.RateGate;
import com.publicobject.misc.xml.*;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class knows how to fetch and parse XML streams of product data from the
 * Amazon ECS webservice.
 *
 * @author James Lemieux
 */
public class AmazonECSXMLParser {

    private static final RateGate amazonRequestRate = new RateGate(200);

    private static final DateFormat[] dateFormats = {new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("yyyy")};
    private static final Parser ITEM_LOOKUP_PARSER = new Parser();
    static {
        // configure the Parser for Item Lookups

        // Parsing instructions for Item
        final XMLTagPath itemTag = new XMLTagPath("ItemLookupResponse").child("Items").child("Item");
        ITEM_LOOKUP_PARSER.addProcessor(itemTag.start(),                                  Processors.createNewObject(Item.class));
        ITEM_LOOKUP_PARSER.addProcessor(itemTag.child("ASIN"),                            Processors.setterMethod(Item.class, "aSIN"));
        ITEM_LOOKUP_PARSER.addProcessor(itemTag.child("DetailPageURL"),                   Processors.setterMethod(Item.class, "detailPageURL"));
        ITEM_LOOKUP_PARSER.addProcessor(itemTag.end(),                                    Processors.addObjectToTargetList());

        // Parsing instructions for ItemAttributes
        final XMLTagPath itemAttributesTag = itemTag.child("ItemAttributes");
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.start(),                        Processors.createNewObject(ItemAttributes.class));
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.child("AudienceRating"),        Processors.setterMethod(ItemAttributes.class, "audienceRating", new AudienceRatingConverter()));
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.child("Director"),              Processors.setterMethod(ItemAttributes.class, "director"));
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.child("ReleaseDate"),           Processors.setterMethod(ItemAttributes.class, "releaseDate", Converters.date(dateFormats)));
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.child("TheatricalReleaseDate"), Processors.setterMethod(ItemAttributes.class, "theatricalReleaseDate", Converters.date(dateFormats)));
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.child("Title"),                 Processors.setterMethod(ItemAttributes.class, "title"));
        ITEM_LOOKUP_PARSER.addProcessor(itemAttributesTag.end(),                          Processors.setterMethod(Item.class, "itemAttributes"));

        // Parsing instructions for ListPrice
        final XMLTagPath listPriceTag = itemAttributesTag.child("ListPrice");
        ITEM_LOOKUP_PARSER.addProcessor(listPriceTag.start(),                             Processors.createNewObject(ListPrice.class));
        ITEM_LOOKUP_PARSER.addProcessor(listPriceTag.child("Amount"),                     Processors.setterMethod(ListPrice.class, "amount", Converters.integer()));
        ITEM_LOOKUP_PARSER.addProcessor(listPriceTag.child("CurrencyCode"),               Processors.setterMethod(ListPrice.class, "currencyCode"));
        ITEM_LOOKUP_PARSER.addProcessor(listPriceTag.child("FormattedPrice"),             Processors.setterMethod(ListPrice.class, "formattedPrice"));
        ITEM_LOOKUP_PARSER.addProcessor(listPriceTag.end(),                               Processors.setterMethod(ItemAttributes.class, "listPrice"));
    }

    private static final Parser ITEM_SEARCH_PARSER = new Parser();
    static {
        // configure the Parser for Item Searches
        XMLTagPath itemsTag = new XMLTagPath("ItemSearchResponse").child("Items");
        ITEM_SEARCH_PARSER.addProcessor(itemsTag.child("Item").child("ASIN"), Processors.addToCollection(PageInformation.class, "itemAsins"));
        ITEM_SEARCH_PARSER.addProcessor(itemsTag.child("TotalPages"),         Processors.setterMethod(PageInformation.class, "pageCount", Converters.integer()));
        ITEM_SEARCH_PARSER.addProcessor(itemsTag.child("TotalResults"),       Processors.setterMethod(PageInformation.class, "totalResults", Converters.integer()));
    }


    private static ItemSearch itemSearch;

    /**
     * When executed, this opens a file specified on the command line, parses
     * it for Issuezilla XML and writes the issues to the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        EventList<Item> itemsList = new BasicEventList<>();
//        loadItem(itemsList, "B000B5XOW0");

        searchAndLoadItems(itemsList, "king", null);
    }

    public static void searchAndLoadItems(EventList<Item> target, String keywords, JProgressBar progressBar) throws IOException, InterruptedException {
        searchAndLoadItems("webservices.amazon.com", target, keywords, progressBar);
    }

    /**
     * Search for all Items with the given <code>keywords</code> and load them.
     */
    public static void searchAndLoadItems(String host, EventList<Item> target, String keywords, JProgressBar progressBar) throws IOException, InterruptedException {
        // cancel the previous item search
        if (itemSearch != null)
            itemSearch.cancel();

        // clear away an existing items
        target.clear();

        Thread.sleep(300);

        // reset the progress bar's maximum
        if (progressBar != null) {
            progressBar.setString("");
            progressBar.setValue(0);
        }

        // create a connection for figuring out what we need to grab
        final HTTPConnection searchConnection = new HTTPConnection(host);

        // prepare a stream to determine the number of pages in the result set
        final String searchUrlPath = "/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=10GN481Z3YQ4S67KNSG2&Operation=ItemSearch&SearchIndex=DVD&ResponseGroup=ItemIds&Keywords=" + keywords;

        // parse the number of results from the stream
        final InputStream itemSearchResultsStream = getInputStream(searchConnection, searchUrlPath, 10);
        PageInformation pageInformation = new PageInformation();
        ITEM_SEARCH_PARSER.parse(itemSearchResultsStream, pageInformation);

        tryClose(itemSearchResultsStream);

        // fetch the total number of results
        if (progressBar != null) {
            progressBar.setMaximum(pageInformation.getTotalResults());
        }

        // fetch the total number of pages in the result set
        final int pageCount = pageInformation.getPageCount();

        // create a new ItemSearch and start it
        itemSearch = new ItemSearch(target, host, searchUrlPath, pageCount, progressBar);
        itemSearch.start();
    }

    public static final class PageInformation {
        private int pageCount;
        private int totalResults;
        private EventList<String> itemAsins;

        public PageInformation(EventList<String> itemAsins) {
            this.itemAsins = GlazedLists.threadSafeList(itemAsins);
        }

        public PageInformation() {
            this(new BasicEventList<String>());
        }

        public int getPageCount() { return pageCount; }
        public void setPageCount(int pageCount) { this.pageCount = pageCount; }

        public int getTotalResults() { return totalResults; }
        public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

        public EventList<String> getItemAsins() { return itemAsins; }
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
    private static class AudienceRatingConverter implements Converter<String,AudienceRating> {
        @Override
        public AudienceRating convert(String value) {
            return AudienceRating.lookup(value);
        }
    }

    /**
     * This class listens for ASINs to be added to a List and reacts to each new
     * addition by enqueueing requests (and response handlers) for each ASIN.
     * Separate threads are used for requests as for responses in order to
     * pipeline the request.
     */
    private static class ItemSearch {
        private static final int NUMBER_OF_RETRIES = 10;

        /** The list of all Items to populate */
        private final EventList<Item> target;

        // This EventList acts as a buffer between the ItemSearch and the ItemLookups.
        // As ASINs are added to it by parsing ItemSearch results, the ITEM_FETCHER
        // is notified and responds by placing a Runnable in a Channel that will eventually
        // be processed in a PooledExecutor. That Runnable executes the ItemLookup based on
        // the ASIN and the Item that is parsed out of the returned XML is placed into target.
        private final EventList<String> itemASINs = new BasicEventList<>();

        private final ProgressBarUpdater progressBarUpdater;
        private final RequestEnqueuer requestEnqueuer;

        private final String searchUrlPath;
        private final int pageCount;

        /** A pool of Threads servicing an unbounded Channel. */
        private QueuedExecutor responseQueue = new QueuedExecutor();
        private QueuedExecutor requestQueue = new QueuedExecutor();

        /** a connection to the HTTP server of interest */
        private final HTTPConnection httpConnection;

        public ItemSearch(EventList<Item> target, String host, String searchUrlPath, int pageCount, JProgressBar progressBar) {
            this.target = target;
            this.searchUrlPath = searchUrlPath;
            this.pageCount = pageCount;
            this.progressBarUpdater = new ProgressBarUpdater(progressBar);
            this.requestEnqueuer = new RequestEnqueuer();

            this.target.addListEventListener(this.progressBarUpdater);
            this.itemASINs.addListEventListener(requestEnqueuer);

            this.httpConnection = new HTTPConnection(host);
        }

        private static class ProgressBarUpdater implements ListEventListener<Item> {
            private final JProgressBar progressBar;

            public ProgressBarUpdater(JProgressBar progressBar) {
                this.progressBar = progressBar;
            }

            @Override
            public void listChanged(ListEvent<Item> listChanges) {
                if (progressBar != null) {
                    final int numLoaded = listChanges.getSourceList().size();

                    progressBar.setString(numLoaded + " of " + progressBar.getMaximum());
                    progressBar.setValue(numLoaded);
                }
            }
        }

        private class RequestEnqueuer implements ListEventListener<String> {
            @Override
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
        }

        public void start() throws InterruptedException {
            InputStream itemSearchResultsStream = null;

            // retrieve each page in the result set and parse the ASINs out of them
            for (int i = 1; i <= pageCount; i++) {
                // build a URL appropriate for fetching 1 page of 10 search results
                final String searchPagePath = searchUrlPath + "&ItemPage=" + i;

                try {
                    // parse the ASINs from the search results page
                    itemSearchResultsStream = getInputStream(httpConnection, searchPagePath, 20);
                    PageInformation pageInformation = new PageInformation(itemASINs);
                    ITEM_SEARCH_PARSER.parse(itemSearchResultsStream, pageInformation);

                    // if we have been interrupted, bail early
                    if (Thread.interrupted()) throw new InterruptedException();

                } catch (IOException ioe) {
                    // simply log the error to Standard Error and continue - these errors are fairly routinely from Amazon's webservice
                    System.err.println("IOException while fetching page " + i + " of " + pageCount + " pages in the search result, \"" + ioe.getMessage() + "\"");

                } finally {
                    tryClose(itemSearchResultsStream);
                }
            }
        }

        public void cancel() throws InterruptedException {
            target.removeListEventListener(this.progressBarUpdater);
            itemASINs.removeListEventListener(this.requestEnqueuer);

            final Thread thread = responseQueue.getThread();

            requestQueue.shutdownNow();
            responseQueue.shutdownNow();

            // we have a problem where a single response is processed after the
            // responseQueue has been shutdown, so to accomodate this case we
            // join on the responseQueue's Thread to ensure the queue's are
            // fully shutdown before returning
            if (thread != null)
                thread.join();
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

            @Override
            public void run() {
                try {
                    // do the request
                    final String path = "/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=10GN481Z3YQ4S67KNSG2&Operation=ItemLookup&ResponseGroup=ItemAttributes&ItemId=" + asin;
                    amazonRequestRate.passThrough();
                    HTTPResponse response = httpConnection.Get(path);
                    // tell someone else to handle the response
                    enqueue(responseQueue, new ItemReceiver(remainingRetries, asin, response));

                } catch (IOException e) {
                    handleError(remainingRetries, asin, e);
                } catch (ModuleException e) {
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
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    // get the stream
                    if(response.getStatusCode() != 200) {
                        throw new IOException("Received response code " + response.getStatusCode());
                    }
                    inputStream = response.getInputStream();
                    ITEM_LOOKUP_PARSER.parse(inputStream, target);
                } catch (ModuleException e) {
                    handleError(remainingRetries, asin, e);
                } catch (IOException e) {
                    handleError(remainingRetries, asin, e);
                } finally {
                    tryClose(inputStream);
                }
            }
        }

        public static void enqueue(QueuedExecutor executor, Runnable runnable) {
            try {
                executor.execute(runnable);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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