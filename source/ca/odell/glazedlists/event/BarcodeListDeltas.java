/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.impl.adt.Barcode;

/**
 * Manage the deltas between a List and a revision of that list. This is a
 * technology preview for an approach that may ultimately replace our current
 * {@link Block} strategy for tracking changes between Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class BarcodeListDeltas {

    /**
     * This barcode is black for every element in the snapshot that hasn't
     * yet been updated. It's white for each of those elements thave have been
     * updated. It's exactly the same size as the number of undeleted elements,
     * (ie. the number of blacks in the deletes barcode).
     */
    private final Barcode updates = new Barcode();

    /**
     * This barcode is black for every element in the snapshot that hasn't
     * yet been deleted. It's white for each of those elements that has been
     * deleted. It is always exactly the same size as the snapshot, and
     * does not change in length.
     */
    private final Barcode deletes = new Barcode();

    /**
     * This barcode is black for every element in the snapshot that still
     * exists. If an element has been removed, it's also removed from this.
     * It also includes a white for each element inserted. It's length is
     * always equal to the length of the current view.
     */
    private final Barcode inserts = new Barcode();

    public void reset(int length) {
        deletes.clear();
        deletes.addBlack(0, length);
        updates.clear();
        updates.addBlack(0, length);
        inserts.clear();
        inserts.addBlack(0, length);
    }

    /**
     * Make sure the deltas are big enough. This hack method is
     * necessary since we don't necessarily know how large the list
     * is that we're providing deltas for.
     */
    private void ensureCapacity(int size) {
        int delta = size - deletes.blackSize();
        if(delta <= 0) return;
        updates.addBlack(updates.size(), delta);
        deletes.addBlack(deletes.size(), delta);
        inserts.addBlack(inserts.size(), delta);
    }

    public int currentToSnapshot(int currentIndex) {
        if(currentIndex < 0 || currentIndex >= currentSize()) throw new IndexOutOfBoundsException();

        // if this is a new insert, it has no original value
        if(inserts.get(currentIndex) == Barcode.WHITE) {
            return -1;
        }

        // this element existed in the snapshot, so adjust it
        // for all nodes that have been inserted since the snapshot
        // was made (this decreases the value).
        int insertAdjusted = inserts.getBlackIndex(currentIndex);

        // adjust for deletes by adding back all elements
        // in the snapshot that have since been deleted
        // (this increases the value)
        return deletes.getIndex(insertAdjusted, Barcode.BLACK);
    }
    public int snapshotToCurrent(int snapshotIndex) {
        if(snapshotIndex < 0 || snapshotIndex >= (inserts.whiteSize() + snapshotSize())) throw new IndexOutOfBoundsException();

        // if this is beyond the snapshot, we want the white index
        if(snapshotIndex >= snapshotSize()) {
            return inserts.getIndex(snapshotIndex - snapshotSize(), Barcode.WHITE);
        }

        // this snapshot element has been deleted
        if(deletes.get(snapshotIndex) == Barcode.WHITE) {
            return -1;
        }

        // adjust for deleted snapshot elements
        int deleteAdjusted = deletes.getBlackIndex(snapshotIndex);

        // adjust for inserts by shifting past all elements
        // that have since been inserted. (this increases the value)
        return inserts.getIndex(deleteAdjusted, Barcode.BLACK);
    }

    public boolean currentUpdated(int currentIndex) {
        if(currentIndex < 0 || currentIndex >= currentSize()) throw new IndexOutOfBoundsException();

        // inserted elements are always updated
        if(inserts.get(currentIndex) == Barcode.WHITE) {
            return true;
        }

        // adjust for insert, we don't need to adjust for deletes
        int insertAdjusted = inserts.getBlackIndex(currentIndex);

        return updates.get(insertAdjusted) == Barcode.WHITE;
    }

    public void add(int currentIndex) {
        ensureCapacity(currentIndex + 1);
        if(currentIndex < 0 || currentIndex > currentSize()) throw new IndexOutOfBoundsException();
        inserts.addWhite(currentIndex, 1);
    }

    public void remove(int currentIndex) {
        ensureCapacity(currentIndex + 1);
        if(currentIndex < 0 || currentIndex >= currentSize()) throw new IndexOutOfBoundsException();

        // if this was a snapshot element, we need to mark this as deleted from the deletes list
        if(inserts.get(currentIndex) == Barcode.BLACK) {
            int insertAdjusted = inserts.getBlackIndex(currentIndex);
            int deleteAdjusted = deletes.getIndex(insertAdjusted, Barcode.BLACK);
            deletes.set(deleteAdjusted, Barcode.WHITE, 1);
            updates.remove(deleteAdjusted, 1);
            inserts.remove(currentIndex, 1);

        // if this was not a snapshot element, we can just de-insert it
        } else {
            inserts.remove(currentIndex, 1);
        }
    }

    public void update(int currentIndex) {
        ensureCapacity(currentIndex + 1);
        if(currentIndex < 0 || currentIndex >= currentSize()) throw new IndexOutOfBoundsException();

        // if this was a snapshot element, we need to mark it as an update
        if(inserts.get(currentIndex) == Barcode.BLACK) {
            int insertAdjusted = inserts.getBlackIndex(currentIndex);
            updates.set(insertAdjusted, Barcode.WHITE, 1);

        // this was an inserted element, its already 'updated'
        } else {
            // do nothing
        }
    }

    public int currentSize() {
        return inserts.size();
    }

    public int snapshotSize() {
        return deletes.size();
    }

    public String toString() {
        return "INSERTS: " + inserts + ", UPDATES: " + updates + ", DELETES: " + deletes;
    }

    public Iterator iterator() {
        return new Iterator();
    }

    /**
     * @return <code>true</code> if this event contains no changes.
     */
    public boolean isEmpty() {
        if(inserts.whiteSize() > 0) return false;
        else if(deletes.whiteSize() > 0) return false;
        else if(updates.whiteSize() > 0) return false;
        return true;
    }

    /**
     * Iterate the list of deltas.
     *
     * FILTER
     *
     * updates: _XX_
     * deletes: X_XX_X
     * inserts: XX__X_X
     *
     * BLASTED
     *
     *
     * UPDATE 0 FILTER  -> BILTER
     * DELETE 1 BILTER  -> FLTER
     * INSERT 2 BLTER   -> BLATER
     * INSERT 3 BLATER  -> BLASTER
     * DELETE 5 BLASTER -> BLASTR
     * INSERT 5 BLASTER -> BLASTER
     * UPDATE 6 BLASTER -> BLASTED
     *
     * "AA"
     *
     * updates: __
     * deletes: XX
     * inserts: __XX
     *
     * "BBaa"
     *
     */
    public class Iterator {
        int snapshotIndex = 0;
        int currentIndex = 0;
        int index = -1;
        int type = -1;

        private Iterator() {
            // do nothing
        }

        public int getIndex() {
            return index;
        }
        public int getType() {
            return type;
        }
        public boolean next() {

            while(snapshotIndex < deletes.size() || currentIndex < inserts.size()) {

                // first look for deletes at the snapshot index
                if(snapshotIndex < deletes.size() && deletes.get(snapshotIndex) == Barcode.WHITE) {
                    index = currentIndex;
                    type = ListEvent.DELETE;
                    snapshotIndex++;
                    return true;
                }

                // then look for inserts at the current index
                if(currentIndex < inserts.size() && inserts.get(currentIndex) == Barcode.WHITE) {
                    index = currentIndex;
                    type = ListEvent.INSERT;
                    currentIndex++;
                    return true;
                }

                // finally look for updates at the current index
                if(snapshotIndex < deletes.size()) {
                    int deleteAdjusted = deletes.getBlackIndex(snapshotIndex);
                    if(updates.get(deleteAdjusted) == Barcode.WHITE) {
                        index = currentIndex;
                        type = ListEvent.UPDATE;
                        currentIndex++;
                        snapshotIndex++;
                        return true;
                    }
                }

                // no changes at this index
                currentIndex++;
                snapshotIndex++;
            }

            return false;
        }

        public boolean hasNext() {
            // todo: do something smarter
            return true;
        }
    }
}