/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;
import ca.odell.glazedlists.impl.io.Bufferlo;
import java.text.ParseException;

/**
 * The address of a resource. This will be either local ("/hello") or remote
 * ("glazedlists://host:port/hello").
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class ResourceUri implements Comparable {
    
    /** the resource host */
    private String host;
    private int port;
    
    /** the resource path */
    private String path;
    
    /** true if this resource is sourced locally */
    private boolean local;
    
    /**
     * Creates a new {@link ResourceUri}.
     */
    private ResourceUri(String host, int port, String path, boolean local) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.local = local;
    }
    
    /**
     * Creates a new returns a {@link ResourceUri} for the specified local path.
     */
    public static ResourceUri local(String path) {
        return new ResourceUri(null, -1, path, true);
    }
    
    /**
     * Creates and returns a {@link ResourceUri} for the specified uri. This resource
     * will be considered local if the host specified matches the host in the uri.
     * Otherwise the uri will be considered remote.
     */
    public static ResourceUri localOrRemote(String uri, String localHost, int localPort) {
        try {
            Bufferlo parser = new Bufferlo();
            parser.write(uri);
            
            parser.consume("glazedlists\\:\\/\\/");
            String host = parser.readUntil("\\:");
            String portString = parser.readUntil("\\/");
            int port = Integer.parseInt(portString);
            String path = "/" + parser.toString();
            
            boolean local = (localHost.equals(host)) && (localPort == port);
            return new ResourceUri(host, port, path, local);
        } catch(ParseException e) {
            throw new IllegalStateException(e.getMessage());
        } catch(NumberFormatException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    /**
     * Creates a new remote {@link ResourceUri}.
     */
    public static ResourceUri remote(String host, int port, String path) {
        return new ResourceUri(host, port, path, false);
    }
    
    /**
     * Test if this URI is local.
     */
    public boolean isLocal() {
        return local;
    }
    /**
     * Test if this URI is remote.
     */
    public boolean isRemote() {
        return !local;
    }
    
    /**
     * Get the URI host.
     */
    public String getHost() {
        return host;
    }
    /**
     * Get the URI port.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Gets this resource as a String, substituting the specified local host and
     * local port as necessary.
     */
    public String toString(String localHost, int localPort) {
        if(local) {
            return "glazedlists://" + localHost + ":" + localPort + path;
        } else {
            return "glazedlists://" + host + ":" + port + path;
        }
    }
    public String toString() {
        return "Resource URI [local=" + local + ", host=" + host + ", port=" + port + ", path=" + path + "]";
    }
    
    /**
     * Computes a hash of this resource uri.
     */
    public int hashCode() {
        int result = path.hashCode();
        if(!local) {
            result = 37 * result + host.hashCode();
            result = 37 * result + port;
        }
        return result;
    }
    
    /**
     * Compares two resources.
     */
    public int compareTo(Object other) {
        if(other == null) throw new NullPointerException();
        ResourceUri otherUri = (ResourceUri)other;
        if(otherUri.local != this.local) throw new IllegalStateException("Cannot compare local URI with remote URI: " + other + " vs. " + this);
        
        int result = path.compareTo(otherUri.path);
        if(result != 0) return result;
        
        if(!local) {
            result = host.compareTo(otherUri.host);
            if(result != 0) return result;

            result = port - otherUri.port;
            if(result != 0) return result;
        }
        
        return 0;
    }
    
    /**
     * Tests if this ResourceUri equals that ResourceUri.
     */
    public boolean equals(Object other) {
        return (compareTo(other) == 0);
    }
}
