package ca.odell.glazedlists.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObjectChange<T> {
    private static final ObjectChange<Object> UNKNOWN_CHANGE = new ObjectChange<>(ListEvent.UNKNOWN_VALUE, ListEvent.UNKNOWN_VALUE);

    private final T oldValue;
    private final T newValue;

    private ObjectChange(T oldValue, T newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectChange<?> that = (ObjectChange<?>) o;
        return Objects.equals(oldValue, that.oldValue) &&
                Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldValue, newValue);
    }

    @Override
    public String toString() {
        return "ObjectChange{" +
                "oldValue=" + oldValue +
                ", newValue=" + newValue +
                '}';
    }

    public static <E> ObjectChange<E> create(E oldValue, E newValue){
        if(oldValue == ListEvent.UNKNOWN_VALUE && newValue == ListEvent.UNKNOWN_VALUE){
            return unknownChange();
        }else{
            return new ObjectChange<>(oldValue, newValue);
        }
    }

    public static <E> List<ObjectChange<E>> create(int size, E oldValue, E newValue){
        return create(size, create(oldValue, newValue));
    }

    public static <E> List<ObjectChange<E>> create(int size, ObjectChange<E> change){
        return Collections.nCopies(size, change);
    }

    public static <E> ObjectChange<E> unknownChange(){
        return (ObjectChange<E>)UNKNOWN_CHANGE;
    }

    public static <E> List<ObjectChange<E>> unknownChange(int size){
        return Collections.nCopies(size, unknownChange());
    }
}
