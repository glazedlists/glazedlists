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

    public static <E> Object flat(ObjectChange<E> event){
        if(event.getOldValue() == ListEvent.UNKNOWN_VALUE && event.getNewValue() == ListEvent.UNKNOWN_VALUE){
            return ListEvent.UNKNOWN_VALUE;
        }else if(event.getOldValue() != ListEvent.UNKNOWN_VALUE && event.getNewValue() != ListEvent.UNKNOWN_VALUE){
            return event;
        }else if(event.getNewValue() != ListEvent.UNKNOWN_VALUE){
            return event.getNewValue();
        }else {
            return event.getOldValue();
        }
    }

    public static <E> void flat(List<ObjectChange<E>> events, List<Object> target){
        for(int i = 0; i<events.size(); i++){
            target.add(ObjectChange.flat(events.get(i)));
        }
    }

    public static <E> E getValue(Object element, int changeType){
        if(changeType == ListEvent.INSERT){
            return getNewValue(element, changeType);
        }else {
            return getOldValue(element, changeType);
        }
    }

    public static <E> E getNewValue(Object element, int changeType){
        if(changeType == ListEvent.INSERT){
            return (E)element;
        }else if(changeType == ListEvent.DELETE){
            return ListEvent.<E>unknownValue();
        }else if(changeType == ListEvent.UPDATE) {
            final ObjectChange<E> event = (ObjectChange<E>)element;
            return event.getNewValue();
        }else{
            return ListEvent.<E>unknownValue();
        }
    }

    public static <E> E getOldValue(Object element, int changeType){
        if(changeType == ListEvent.INSERT){
            return ListEvent.<E>unknownValue();
        }else if(changeType == ListEvent.DELETE){
            return (E)element;
        }else if(changeType == ListEvent.UPDATE) {
            final ObjectChange<E> event = (ObjectChange<E>)element;
            return event.getOldValue();
        }else {
            return ListEvent.<E>unknownValue();
        }
    }

    public static <E> Object createElement(E oldValue, E newValue){
        if(oldValue == ListEvent.UNKNOWN_VALUE && newValue == ListEvent.UNKNOWN_VALUE){
            return ObjectChange.unknownChange();
        }else if(oldValue != ListEvent.UNKNOWN_VALUE && newValue != ListEvent.UNKNOWN_VALUE){
            return ObjectChange.create(oldValue, newValue);
        }else if(newValue != ListEvent.UNKNOWN_VALUE){
            return newValue;
        }else {
            return oldValue;
        }
    }

    public static <E> ObjectChange<E> create(E oldValue, E newValue){
        if(oldValue == ListEvent.UNKNOWN_VALUE && newValue == ListEvent.UNKNOWN_VALUE){
            return unknownChange();
        }else{
            return new ObjectChange<>(oldValue, newValue);
        }
    }

    public static <E> List<E> getNewValues(List<ObjectChange<E>> events){
        final List<E> newValues = new ArrayList<>(events.size());
        for(int i = 0; i<events.size(); i++){
            newValues.add(events.get(i).getNewValue());
        }
        return newValues;
    }

    public static <E> List<E> getOldValues(List<Object> events, int changeType){
        final List<E> list = new ArrayList<>(events.size());
        for(int i = 0; i<events.size(); i++){
            list.add(getOldValue(events.get(i), changeType));
        }
        return list;
    }

    public static <E> List<E> getNewValues(List<Object> events, int changeType){
        final List<E> list = new ArrayList<>(events.size());
        for(int i = 0; i<events.size(); i++){
            list.add(getNewValue(events.get(i), changeType));
        }
        return list;
    }

    public static <E> List<E> getOldValues(List<ObjectChange<E>> events){
        final List<E> oldValues = new ArrayList<>(events.size());
        for(int i = 0; i<events.size(); i++){
            oldValues.add(events.get(i).getOldValue());
        }
        return oldValues;
    }

    public static <E> ObjectChange<E> unknownChange(){
        return (ObjectChange<E>)UNKNOWN_CHANGE;
    }

    public static <E> List<ObjectChange<E>> unknownChange(int size){
        return Collections.nCopies(size, unknownChange());
    }
}
