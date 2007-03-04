/**
 * Copyright by Holger Brands
 * 2007
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Small test application to write and read some serialized BasicEventLists.
 * Intended to test serialization and deserialization on different JRE's.
 * 
 * @author Holger Brands
 */
public class SerializationTestApp {
    /** name of test file. */
    private static final String TESTDATA_FILENAME = "testdata.ser";
    
    /** main method for reading and writing test data. */
    public static void main(String[] args) throws Exception {
        if ((args.length != 1) || !args[0].equals("-write") && !args[0].equals("-read")) {
            System.err.println("Wrong commandline parameters!");
            System.err.println("Use 'SerializationTestApp -write' or 'SerializationTestApp -read'");
        }
        if (args[0].equals("-write")) {
            final Object rootObj = createTestData();
            writeTestData(rootObj);
            System.out.println("Test data written to " + TESTDATA_FILENAME);
        }
        if (args[0].equals("-read")) {
            final Object rootObj = readTestData();
            System.out.println("Test data read from " + TESTDATA_FILENAME);
            verifyTestData(rootObj);
            System.out.println("Test data verified");
        }        
    }

    /** verifies the test data. */
    private static void verifyTestData(Object rootObj) {
        // ensure deserialzed lists still share the lock and publisher
        final List<ListHolder> serializedCopy = (List<ListHolder>) rootObj;
        assert (serializedCopy != null && serializedCopy.size() > 0);
        
        final ListEventPublisher publisher = serializedCopy.get(0).names.getPublisher();
        final ReadWriteLock lock = serializedCopy.get(0).names.getReadWriteLock();        
        
        final CompositeList<String> compositeList = new CompositeList<String>(publisher, lock);
        
        for (Iterator<ListHolder> iter = serializedCopy.iterator(); iter.hasNext();) {
            compositeList.addMemberList(iter.next().names);            
        }

        for (int i = 0; i < compositeList.size(); i++) {
            assert(compositeList.get(i).equals("Test " + i));
        }
    }
    
    /** creates some test data to serialize. */
    private static Object createTestData() {
        final ReadWriteLock sharedLock = LockFactory.DEFAULT.createReadWriteLock();
        final ListEventPublisher sharedPublisher = ListEventAssembler.createListEventPublisher();
        final List<ListHolder> rootList = new ArrayList<ListHolder>();
        for (int i = 0; i < 4; i++) {
            final BasicEventList<String> eventList = new BasicEventList<String>(sharedPublisher, sharedLock);
            eventList.add("Test " + i);
            final ListHolder elem = new ListHolder(eventList);
            rootList.add(elem);
        }
        return rootList;
    }
    
    /** write test data to file. */
    private static void writeTestData(Object rootObj) throws Exception {
        final FileOutputStream fileOut = new FileOutputStream(new File(TESTDATA_FILENAME));
        final ObjectOutputStream objectsOut = new ObjectOutputStream(fileOut);
        objectsOut.writeObject(rootObj);
        objectsOut.close();
    }

    /** Read test data from file. */
    private static Object readTestData() throws Exception {
        final FileInputStream fileIn = new FileInputStream(new File(TESTDATA_FILENAME));
        final ObjectInputStream objectsIn = new ObjectInputStream(fileIn);
        final Object result = objectsIn.readObject();
        objectsIn.close();
        return result;
    }
    
    /** Serializable helper class to hold BasicEventList. */
    private static class ListHolder implements Serializable {
        private static final long serialVersionUID = 5097886240645131830L;
        
        private EventList<String> names;
        
        public ListHolder(BasicEventList<String> names) {
            this.names = names;
        }
    }
}