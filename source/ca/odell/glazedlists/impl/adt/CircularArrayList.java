package ca.odell.glazedlists.impl.adt;

import java.util.AbstractList;

/**
 * @author jessewilson
 */
public class CircularArrayList<T> extends AbstractList<T> {

  // [ 3 4 _ _ _ 0 1 2 ]
  // head = 5
  // tail = 2
  // size = 5
  // arrayLength = 8

  // [ _ _ 0 1 2 3 4 _ ]
  // head = 2
  // tail = 7
  // size = 5
  // arrayLength = 8

  int head = 0;
  int size = 0;
  Object[] values = new Object[10];

  int arrayLength = values.length;

  @Override
public T get(int index) {
    return (T)values[toCircularIndex(index)];
  }

  @Override
public void add(int index, T element) {
    growIfNecessary();

    int indexToAdd = toCircularIndex(index);
    int distToHead = distanceToHead(indexToAdd);
    int distToTail = distanceToTail(indexToAdd);

    // shift values to the right, that's less work
    if (distToTail <= distToHead) {
        int tail = tail();
        shift(indexToAdd, tail, 1);
        values[indexToAdd] = element;

    // shift values to the left, that's less work
    } else {
      shift(head, indexToAdd, -1);
      values[modIndex(indexToAdd - 1)] = element;
      head = modIndex(head - 1);

    }

    size++;
  }

  int tail() {
    return modIndex(head + size);
  }

  void growIfNecessary() {
    int size = size();
    if (size < arrayLength) {
      return;
    }
    
    Object[] biggerValues = new Object[values.length * 2];
    int tail = tail();

    // [ _ _ 0 1 2 3 4 _ ] ==> [ 0 1 2 3 4 _ _ _ _ _ _ _ _ _ _ _ ]
    if (head < tail) {
      System.arraycopy(values, head, biggerValues, 0, tail - head);

    // [ 2 3 4 _ _ _ 0 1 ] ==> [ 0 1 2 3 4 _ _ _ _ _ _ _ _ _ _ _ ]
    } else {
      System.arraycopy(values, head, biggerValues, 0, arrayLength - head);
      System.arraycopy(values, 0, biggerValues, arrayLength - head, tail);

    }
    values = biggerValues;
    arrayLength = biggerValues.length;
    head = 0;
  }

  @Override
public T remove(int index) {
    int indexToRemove = toCircularIndex(index);
    int distToHead = distanceToHead(indexToRemove);
    int distToTail = distanceToTail(indexToRemove);

    T removed = get(index);

    // shift values to the left, that's less work
    if (distToTail < distToHead) {
      int tail = tail();
      shift(indexToRemove + 1, tail, -1);
      values[modIndex(tail - 1)] = null;

    // shift values to the right, that's less work
    } else {
      shift(head, indexToRemove, 1);
      values[head] = null;
      head = modIndex(head + 1);

    }
    size--;

    return removed;
  }

  /**
   * Shift the values at the specified indices in the specified direction.
   *
   * <p>The ultimate implementation of a shift left would break the array
   * into 4 parts:
   *  <li>stuff that isn't moved
   *  <li>stuff at the end that needs to be moved to to the left
   *  <li>stuff at the very beginning that needs to be moved to the end
   *  <li>stuff near the beginning that needs to be moved to the beginning
   * <p>This implementation isn't ultimate.
   *
   * @param first inclusive
   * @param last exclusive
   * @param distance either -1 or 1
   */
  void shift(int first, int last, int distance) {
    if (distance != 1 && distance != -1) {
      throw new IllegalArgumentException();
    }
    if (first == last) {
      return;
    }
    if (last == 0) {
      last = arrayLength;
    }

    // a split shift
    if (first > last && last != 0) {

      // [ 3 4 _ _ _ 0 1 2 ] ==> [ 2 3 4 _ _ _ 0 1 ]
      if (distance == 1) {
        System.arraycopy(values, 0, values, 1, last);
        values[0] = values[arrayLength - 1];
        System.arraycopy(values, first, values, first + 1, arrayLength - first - 1);
        values[first] = null;

      // [ 1 2 3 4 _ _ _ 0 ] ==> [ 2 3 4 _ _ _ 0 1 ]
      } else if(distance == -1) {
        System.arraycopy(values, first, values, first - 1, arrayLength - first);
        values[arrayLength - 1] = values[0];
        System.arraycopy(values, 1, values, 0, last - 1);
        values[last - 1] = null;

      }

    // a single shift
    } else {

      // [ 0 1 2 3 4 _ _ _ ] ==> [ 1 2 3 4 _ _ _ 0 ]
      if (distance == -1 && first == 0) {
        values[arrayLength - 1] = values[0];
        System.arraycopy(values, 1, values, 0, last - 1);
        values[last - 1] = null;

      // [ _ _ _ 0 1 2 3 4 ] ==> [ 4 _ _ _ 0 1 2 3 ]
      } else if (distance == 1 && last == arrayLength) {
        values[0] = values[arrayLength - 1];
        System.arraycopy(values, first, values, first + 1, last - first - 1);
        values[first] = null;

      // [ _ _ 0 1 2 3 4 _ ] ==> [ _ _ _ 0 1 2 3 4 ]
      } else if(distance == 1) {
        System.arraycopy(values, first, values, first + 1, last - first);
        values[first] = null;

      // [ _ _ _ 0 1 2 3 4 ] ==> [ _ _ 0 1 2 3 4 _ ]
      } else if(distance == -1) {
        System.arraycopy(values, first, values, first - 1, last - first);
        values[last - 1] = null;
      }
    }
  }

  private int distanceToTail(int index) {
    int tail = tail();
    return index <= tail
      ? tail - index
      : tail + arrayLength - index;
  }

  private int distanceToHead(int index) {
    return index >= head
      ? index - head
      : index + arrayLength - head;
  }

  @Override
public int size() {
    return size;
  }

  int toCircularIndex(int index) {

    // validate bounds
    if (index < 0 || index > size()) {
      throw new IndexOutOfBoundsException("Index " + index + " on List of size: " + size);
    }

    return modIndex(index + head);
  }

  /**
   * Returns the index modded within the values array.
   */
  int modIndex(int value) {
    return (value + arrayLength) % arrayLength;
  }
}
