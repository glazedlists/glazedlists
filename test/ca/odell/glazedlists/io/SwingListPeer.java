/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import java.io.IOException;

/**
 * A peer that publishes and subscribes to lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SwingListPeer {

    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(String[] args) throws IOException {
        if(args.length != 2 && args.length != 4) {
            System.out.println("Usage: SwingListPeer <localhost> <localport> [<targethost> <targetport>]");
            return;
        }

        String localHost = args[0];
        int localPort = Integer.parseInt(args[1]);

        ListPeer peer = new ListPeer(localPort);
        peer.start();

        // start the publisher
        if(args.length >= 2) {
            new PublishFrame(peer, localHost, localPort, "/slp");
        }

        // start the subscriber
        if(args.length >= 4) {
            String targetHost = args[2];
            int targetPort = Integer.parseInt(args[3]);
            new SubscribeFrame(peer, targetHost, targetPort, "/slp");
        }
    }
}
