package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jessewilson
 */
public class CircularArrayListTest {

  private final CircularArrayList<Object> list = new CircularArrayList<Object>();

  @Test
  public void testRightShifts() {
    Object[] innerArray = new Object[] { "A", "B", "C", "D", "E", "a", "b", "c" };
    list.values = innerArray;
    list.arrayLength = innerArray.length;

    list.shift(0, 5, 1);
    assertInnerArrayWithNullFlip("_ABCDEbc");

    list.shift(1, 6, 1);
    assertInnerArrayWithNullFlip("__ABCDEc");

    list.shift(2, 7, 1);
    assertInnerArrayWithNullFlip("___ABCDE");

    innerArray[0] = "a";
    innerArray[1] = "b";
    innerArray[2] = "c";
    assertInnerArrayWithNullFlip("abcABCDE");

    list.shift(3, 0, 1);
    assertInnerArrayWithNullFlip("Ebc_ABCD");

    list.shift(4, 1, 1);
    assertInnerArrayWithNullFlip("DEc__ABC");

    list.shift(5, 2, 1);
    assertInnerArrayWithNullFlip("CDE___AB");

    innerArray[3] = "a";
    innerArray[4] = "b";
    innerArray[5] = "c";
    assertInnerArrayWithNullFlip("CDEabcAB");

    list.shift(6, 3, 1);
    assertInnerArrayWithNullFlip("BCDEbc_A");

    list.shift(7, 4, 1);
    assertInnerArrayWithNullFlip("ABCDEc__");

    list.shift(0, 5, 1);
    assertInnerArrayWithNullFlip("_ABCDE__");

  }

  @Test
  public void testLeftShifts() {
    Object[] innerArray = new Object[] { "A", "B", "C", "D", "E", "a", "b", "c" };
    list.values = innerArray;
    list.arrayLength = innerArray.length;

    list.shift(0, 5, -1);
    assertInnerArrayWithNullFlip("BCDE_abA");

    list.shift(7, 4, -1);
    assertInnerArrayWithNullFlip("CDE__aAB");

    list.shift(6, 3, -1);
    assertInnerArrayWithNullFlip("DE___ABC");

    innerArray[2] = "a";
    innerArray[3] = "b";
    innerArray[4] = "c";
    assertInnerArrayWithNullFlip("DEabcABC");

    list.shift(5, 2, -1);
    assertInnerArrayWithNullFlip("E_abABCD");

    list.shift(4, 1, -1);
    assertInnerArrayWithNullFlip("__aABCDE");

    list.shift(3, 0, -1);
    assertInnerArrayWithNullFlip("__ABCDE_");

    innerArray[7] = "a";
    innerArray[0] = "b";
    innerArray[1] = "c";
    assertInnerArrayWithNullFlip("bcABCDEa");

    list.shift(2, 7, -1);
    assertInnerArrayWithNullFlip("bABCDE_a");

    list.shift(1, 6, -1);
    assertInnerArrayWithNullFlip("ABCDE__a");
  }

  private void assertInnerArrayWithNullFlip(String expected) {
    List<String> innerArrayAsList = (List)Arrays.asList(list.values);
    for (int i = 0; i < innerArrayAsList.size(); i++) {
      if (innerArrayAsList.get(i) == null) {
        innerArrayAsList.set(i, "_");
      }
    }

    assertEquals(GlazedListsTests.stringToList(expected), innerArrayAsList);
  }

  @Test
  public void testAdd() {
    list.add("c");
    list.add("d");
    list.add(0, "a");
    list.add(1, "b");
    list.add(4, "e");
    list.add(5, "f");
    assertEquals(list, GlazedListsTests.stringToList("abcdef"));
  }

  @Test
  public void testRemove() {
    list.addAll(GlazedListsTests.stringToList("abcdef"));

    list.remove(2);
    list.remove(3);
    assertEquals(list, GlazedListsTests.stringToList("abdf"));

    list.remove(0);
    list.remove(0);
    assertEquals(list, GlazedListsTests.stringToList("df"));

    list.remove(1);
    list.remove(0);
    assertEquals(list, GlazedListsTests.stringToList(""));
  }

  @Test
  public void testLargeCircularArray() {
    for(int i = 0; i < 1000; i++) {
      list.add(0, new Integer(i));
      assertEquals(i + 1, list.size());
    }

    for(int i = 0; i < 1000; i++) {
      list.add(list.size(), new Integer(i));
      assertEquals(1000 + i + 1, list.size());
    }

    for(int i = 0; i < 1000; i++) {
      list.remove(0);
      assertEquals(2000 - i - 1, list.size());
    }

    for(int i = 0; i < 1000; i++) {
      list.remove(list.size() - 1);
      assertEquals(1000 - i - 1, list.size());
    }
  }

  @Test
  public void testListMethods() {
    list.addAll(GlazedListsTests.stringToList("helloworld"));

    list.removeAll(GlazedListsTests.stringToList("lower"));
    assertEquals(GlazedListsTests.stringToList("hd"), list);

    List expected = new ArrayList(GlazedListsTests.stringToList("hd"));

    Random dice = new Random(0);
    for (int i = 0; i < 100; i++) {
      int index = dice.nextInt(list.size() + 1);
      Integer value = new Integer(dice.nextInt(1000));
      list.add(index, value);
      expected.add(index, value);
      assertEquals(expected, list);
    }

    for (int i = 0; i < 100; i++) {
      int index = dice.nextInt(list.size());
      list.remove(index);
      expected.remove(index);
      assertEquals(expected, list);
    }
  }
}
