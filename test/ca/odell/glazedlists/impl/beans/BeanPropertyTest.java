/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;
// test objects
import java.awt.Color;

/**
 * This test verifies that the BeanProperty works as expected.
 *
 * @author <a href="mailto;kevin@swank.ca">Kevin Maltby</a>
 * @author manningj
 */
public class BeanPropertyTest extends TestCase {

    /**
     * Tests that simple properties work.
     */
    public void testSimpleProperties() {
        BeanProperty painter = new BeanProperty(Automobile.class, "color", true, true);
        Automobile myCar = new Automobile(false);

        // simple get
        myCar.setColor(Color.red);
        assertEquals(Color.red, painter.get(myCar));

        // simple set
        painter.set(myCar, Color.blue);
        assertEquals(Color.blue, myCar.getColor());

        // primitive get
        BeanProperty transmission = new BeanProperty(Automobile.class, "automatic", true, false);
        assertEquals(Boolean.FALSE, transmission.get(myCar));
        Automobile yourCar = new Automobile(true);
        assertEquals(Boolean.TRUE, transmission.get(yourCar));

        // superclass property set
        BeanProperty truckColor = new BeanProperty(Truck.class, "color", true, true);
        Truck myTruck = new Truck(3);
        myTruck.setColor(Color.yellow);
        assertEquals(Color.yellow, truckColor.get(myTruck));
        assertEquals(Color.class, truckColor.getValueClass());

        // superclass property get
        truckColor.set(myTruck, Color.green);
        assertEquals(Color.green, myTruck.getColor());

        // write-only properties
        BeanProperty gasUp = new BeanProperty(Automobile.class, "fullOfGas", false, true);
        gasUp.set(myCar, Boolean.TRUE);
        assertEquals(true, myCar.getDrivable());
        assertEquals(boolean.class, gasUp.getValueClass());
        gasUp.set(myCar, Boolean.FALSE);
        assertEquals(false, myCar.getDrivable());

        // read-only properties
        BeanProperty drivable = new BeanProperty(Automobile.class, "drivable", true, false);
        myCar.setFullOfGas(true);
        assertEquals(Boolean.TRUE, drivable.get(myCar));
        assertEquals(boolean.class, drivable.getValueClass());
        myCar.setFullOfGas(false);
        assertEquals(Boolean.FALSE, drivable.get(myCar));

        // interface property get
        BeanProperty towedVehicle = new BeanProperty(SupportsTrailerHitch.class, "towedVehicle", true, true);
        myTruck.setTowedVehicle(myCar);
        assertEquals(myCar, towedVehicle.get(myTruck));
        assertEquals(Automobile.class, towedVehicle.getValueClass());

        // interface property set
        towedVehicle.set(myTruck, yourCar);
        assertEquals(yourCar, myTruck.getTowedVehicle());
    }

    /**
     * Tests that navigating properties work.
     */
    public void testNavigatedProperties() {
        // navigated get
        BeanProperty towAndPaint = new BeanProperty(SupportsTrailerHitch.class, "towedVehicle.color", true, true);
        Truck towTruck = new Truck(3);
        Automobile rainbowCar = new Automobile(true);
        towTruck.setTowedVehicle(rainbowCar);
        rainbowCar.setColor(Color.red);
        assertEquals(Color.red, towAndPaint.get(towTruck));
        assertEquals(Color.class, towAndPaint.getValueClass());

        // navigated set
        towAndPaint.set(towTruck, Color.gray);
        assertEquals(Color.gray, rainbowCar.getColor());

        // deeply navigated get
        BeanProperty red = new BeanProperty(SupportsTrailerHitch.class, "towedVehicle.color.red", true, false);
        rainbowCar.setColor(Color.blue);
        assertEquals(new Integer(0), red.get(towTruck));
        rainbowCar.setColor(Color.red);
        assertEquals(new Integer(255), red.get(towTruck));
        assertEquals(int.class, red.getValueClass());
    }
    
    /**
     * Test that BeanProperties work for interfaces.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=183">Issue 183</a>
     */
    public void testInterfaces() {
        BeanProperty codeProperty = new BeanProperty(SubInterface.class, "code", true, true);
        NamedCode namedCode = new NamedCode();
        codeProperty.set(namedCode, "C++");
        assertEquals("C++", codeProperty.get(namedCode));
    }
}

/**
 * Test interfaces.
 */
interface BaseInterface {
    public String getCode();
    public void setCode(String code);
}
interface SubInterface extends BaseInterface {
    public String getName();
    public void setName(String name);
}
class NamedCode implements SubInterface {
    private String name = "JManning";
    private String code = "Java!";
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}

/**
 * A test object.
 */
class Automobile {
    private boolean automatic;
    private Color color;
    private boolean fullOfGas;
    public Automobile(boolean automatic) {
        this.automatic = automatic;
        color = Color.BLACK;
        fullOfGas = true;
    }
    public Color getColor() {
        return color;
    }
    public boolean getDrivable() {
        return fullOfGas;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public boolean isAutomatic() {
        return automatic;
    }
    public void setFullOfGas(boolean fullOfGas) {
        this.fullOfGas = fullOfGas;
    }
}
class Truck extends Automobile implements SupportsTrailerHitch {
    private int numSeats;
    private Automobile towedVehicle;
    public Truck(int seats) {
        super(false);
    }
    public int getNumSeats() {
        return numSeats;
    }
    public void setTowedVehicle(Automobile towedVehicle) {
        this.towedVehicle = towedVehicle;
    }
    public Automobile getTowedVehicle() {
        return towedVehicle;
    }
}
interface SupportsTrailerHitch {
    public void setTowedVehicle(Automobile towedVehicle);
    public Automobile getTowedVehicle();
}
class TowingCompany {
    public SupportsTrailerHitch getTowTruck() {
        return new Truck(3);
    }
}
