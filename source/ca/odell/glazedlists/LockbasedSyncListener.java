package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;


/**
 * This {@link ListEventListener} updates another {@link EventList} so that its contents match those of a source
 * {@link EventList}. When updating the target EventList, this listener aquires and holds its write lock.
 *
 * @author Holger Brands
 */
public class LockbasedSyncListener<E> extends SyncListener<E> {

    private final EventList<E> targetList;

   /**
    * Constructor with source and target {@link EventList}.
    */
   public LockbasedSyncListener(EventList<E> aSource, EventList<E> aTarget) {
       super(aSource, aTarget);
       targetList = aTarget;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void listChanged(ListEvent<E> listChanges) {
       targetList.getReadWriteLock().writeLock().lock();
       try {
           super.listChanged(listChanges);
       } finally {
           targetList.getReadWriteLock().writeLock().unlock();
       }
   }
}