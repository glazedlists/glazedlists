/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Overcome flaws in {@link HttpURLConnection} to download files from http://java.net,
 * which requires cookies to be passed between redirects for downloads to work.
 *
 * @author <a href="jesse@swank.ca">Jesse Wilson</a>
 */
public class HttpClient {

    public static final int MAX_REDIRECTS = 20;

    public static void main(String[] args) throws Exception {
        // expect a single argument starting with "http" or "https"
        if(args.length != 2 || !args[0].startsWith("http")) {
            System.out.println("Usage: HttpClient <url> <filename>");
            System.out.println();
            System.out.println("This program demonstrates how to download from java.net's");
            System.out.println("Documents & Files area using URLConnection");
            return;
        }

        // fast fail if the file already exists
        File targetFile = new File(args[1]);
        if(targetFile.exists()) {
            System.out.println("Skipping " + args[0] + ", file already exists");
            return;
        }

        // read from the webserver specified
        InputStream httpIn = getInputStream(args[0]);

        // write from the stream to our target file
        System.out.println("Downloading " + args[0]);
        OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(args[1]));
        pushStreams(httpIn, fileOut);
        fileOut.close();
    }

    /**
     * Move bytes from the specified input stream to the specified output
     * stream until the input stream is exhaused.
     *
     * <p>We could optimize this to use byte buffers if it ever became a bottleneck.
     */
    private static void pushStreams(InputStream source, OutputStream target) throws IOException {
        while(true) {
            int aByte = source.read();
            if(aByte < 0) break;
            target.write(aByte);
        }
    }

    /**
     * Follow redirects as necessary to get an InputStream from the specified URL.
     */
    private static InputStream getInputStream(String url) throws IOException {
        List cookies = new ArrayList();

        for(int i = 0; i < MAX_REDIRECTS; i++) {
            // prepare the connection
            HttpURLConnection urlConnection = (HttpURLConnection)new URL(url).openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            writeCookies(cookies, urlConnection);
            urlConnection.connect();

            // if this is a redirect, keep the cookies
            // and load the next location
            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                    || urlConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                url = urlConnection.getHeaderField("Location");
                acceptCookies(cookies, urlConnection);

            // success!
            } else {
                return urlConnection.getInputStream();
            }
        }

        throw new IOException("Max redirects " + MAX_REDIRECTS + " exceeded!");
    }

    /**
     * Accept all cookies provided by the HTTP response.
     *
     * <p>This method fails to perform some important aspects of cookie management,
     * and is potentially dangerous for all but the most basic of cookie problems.
     * This method fails at:
     * <li>Ensuring cookie names are unique
     * <li>Handling cookie expiry dates
     * <li>Keeping cookies private to the hosts that provide them
     * <li>Verifying the hosts specified match the hosts providing the cookies
     */
    private static void acceptCookies(List cookies, HttpURLConnection urlConnection) {
        for(Iterator i = urlConnection.getHeaderFields().entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
            String key = (String)entry.getKey();
            if(!"Set-Cookie".equalsIgnoreCase(key)) continue;

            List values = (List)entry.getValue();
            for(Iterator v = values.iterator(); v.hasNext(); ) {
                String value = (String)v.next();
                String cookie = value.split(";")[0];
                cookies.add(cookie);
            }
        }
    }

    /**
     * Encode the cookie header for the specified connection.
     */
    private static void writeCookies(List cookies, HttpURLConnection urlConnection) {
        StringBuffer cookiesString = new StringBuffer();
        for(Iterator i = cookies.iterator(); i.hasNext(); ) {
            if(cookiesString.length() > 0) cookiesString.append("; ");
            cookiesString.append(i.next());
        }
        if(cookiesString.length() > 0) urlConnection.addRequestProperty("Cookie", cookiesString.toString());
    }
}