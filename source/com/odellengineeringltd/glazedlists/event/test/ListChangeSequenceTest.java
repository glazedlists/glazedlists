/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.event.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// standard collections
import java.util.*;

/**
 * This test creates a sequence of changes and verifies that the ListChangeSequence
 * correctly sorts the changes.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListChangeSequenceTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random();
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

    /**
     * Test to verify that the ListChangeSequence correctly combines two inserts
     * where the second insert is within the first insert.
     */
    public void testInsertCombineSecondTails() {
        ListChangeSequence updates = new ListChangeSequence();
        
        updates.beginAtomicChange();
        updates.appendChange(0, 9, ListChangeBlock.INSERT);
        updates.appendChange(5, 14, ListChangeBlock.INSERT);
        updates.commitAtomicChange();
        
        assertEquals(1, updates.getAtomicCount());
        assertEquals(1, updates.getBlockCount(0));
        ListChangeBlock block = updates.getBlock(0, 0);
        
        assertEquals(0, block.getStartIndex());
        assertEquals(19, block.getEndIndex());
        assertEquals(ListChangeBlock.INSERT, block.getType());
    }

    /**
     * Test to verify that the ListChangeSequence correctly does not combine
     * two inserts where the second insert is before the first insert.
     */
    public void testInsertCombineSecondLeads() {
        ListChangeSequence updates = new ListChangeSequence();
        
        updates.beginAtomicChange();
        updates.appendChange(5, 14, ListChangeBlock.INSERT);
        updates.appendChange(0, 9, ListChangeBlock.INSERT);
        updates.commitAtomicChange();
        
        assertEquals(1, updates.getAtomicCount());
        assertEquals(2, updates.getBlockCount(0));

        ListChangeBlock blockOne = updates.getBlock(0, 0);
        assertEquals(0, blockOne.getStartIndex());
        assertEquals(9, blockOne.getEndIndex());
        assertEquals(ListChangeBlock.INSERT, blockOne.getType());

        ListChangeBlock blockTwo = updates.getBlock(0, 1);
        assertEquals(15, blockTwo.getStartIndex());
        assertEquals(24, blockTwo.getEndIndex());
        assertEquals(ListChangeBlock.INSERT, blockTwo.getType());
    }

    /**
     * Test to verify that the ListChangeSequence correctly combines two
     * deletes.
     */
    public void testDeleteCombineSecondLeads() {
        ListChangeSequence updates = new ListChangeSequence();
        
        updates.beginAtomicChange();
        updates.appendChange(5, 14, ListChangeBlock.DELETE);
        updates.appendChange(0, 9, ListChangeBlock.DELETE);
        updates.commitAtomicChange();
        
        assertEquals(1, updates.getAtomicCount());
        assertEquals(1, updates.getBlockCount(0));

        ListChangeBlock block = updates.getBlock(0, 0);
        assertEquals(0, block.getStartIndex());
        assertEquals(19, block.getEndIndex());
        assertEquals(ListChangeBlock.DELETE, block.getType());
    }

    /**
     * Test to verify that the ListChangeSequence correctly combines two
     * deletes.
     */
    public void testDeleteCombineSecondTails() {
        ListChangeSequence updates = new ListChangeSequence();
        
        updates.beginAtomicChange();
        updates.appendChange(0, 9, ListChangeBlock.DELETE);
        updates.appendChange(5, 14, ListChangeBlock.DELETE);
        updates.commitAtomicChange();
        
        assertEquals(1, updates.getAtomicCount());
        assertEquals(2, updates.getBlockCount(0));

        ListChangeBlock blockOne = updates.getBlock(0, 0);
        assertEquals(0, blockOne.getStartIndex());
        assertEquals(9, blockOne.getEndIndex());
        assertEquals(ListChangeBlock.DELETE, blockOne.getType());

        ListChangeBlock blockTwo = updates.getBlock(0, 1);
        assertEquals(5, blockTwo.getStartIndex());
        assertEquals(14, blockTwo.getEndIndex());
        assertEquals(ListChangeBlock.DELETE, blockTwo.getType());
    }

    /**
     * Test to verify that the ListChangeSequence correctly combines an
     * insert and a delete
     */
    public void testInsertDeleteOverlap() {
        ListChangeSequence updates = new ListChangeSequence();
        
        updates.beginAtomicChange();
        updates.appendChange(0, 9, ListChangeBlock.INSERT);
        updates.appendChange(5, 14, ListChangeBlock.DELETE);
        updates.commitAtomicChange();
        
        assertEquals(1, updates.getAtomicCount());
        assertEquals(2, updates.getBlockCount(0));

        ListChangeBlock blockOne = updates.getBlock(0, 0);
        assertEquals(0, blockOne.getStartIndex());
        assertEquals(4, blockOne.getEndIndex());
        assertEquals(ListChangeBlock.INSERT, blockOne.getType());

        ListChangeBlock blockTwo = updates.getBlock(0, 1);
        assertEquals(5, blockTwo.getStartIndex());
        assertEquals(9, blockTwo.getEndIndex());
        assertEquals(ListChangeBlock.DELETE, blockTwo.getType());
    }
}
