/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import junit.framework.TestCase;

import java.awt.*;

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
        BeanProperty<Automobile> painter = new BeanProperty<Automobile>(Automobile.class, "color", true, true);
        Automobile myCar = new Automobile(false);

        // simple get
        myCar.setColor(Color.red);
        assertEquals(Color.red, painter.get(myCar));

        // simple set
        painter.set(myCar, Color.blue);
        assertEquals(Color.blue, myCar.getColor());

        // primitive get
        BeanProperty<Automobile> transmission = new BeanProperty<Automobile>(Automobile.class, "automatic", true, false);
        assertEquals(Boolean.FALSE, transmission.get(myCar));
        Automobile yourCar = new Automobile(true);
        assertEquals(Boolean.TRUE, transmission.get(yourCar));

        // superclass property set
        BeanProperty<Truck> truckColor = new BeanProperty<Truck>(Truck.class, "color", true, true);
        Truck myTruck = new Truck(3);
        myTruck.setColor(Color.yellow);
        assertEquals(Color.yellow, truckColor.get(myTruck));
        assertEquals(Color.class, truckColor.getValueClass());

        // superclass property get
        truckColor.set(myTruck, Color.green);
        assertEquals(Color.green, myTruck.getColor());

        // write-only properties
        BeanProperty<Automobile> gasUp = new BeanProperty<Automobile>(Automobile.class, "fullOfGas", false, true);
        gasUp.set(myCar, Boolean.TRUE);
        assertEquals(true, myCar.getDrivable());
        assertEquals(boolean.class, gasUp.getValueClass());
        gasUp.set(myCar, Boolean.FALSE);
        assertEquals(false, myCar.getDrivable());

        // read-only properties
        BeanProperty<Automobile> drivable = new BeanProperty<Automobile>(Automobile.class, "drivable", true, false);
        myCar.setFullOfGas(true);
        assertEquals(Boolean.TRUE, drivable.get(myCar));
        assertEquals(boolean.class, drivable.getValueClass());
        myCar.setFullOfGas(false);
        assertEquals(Boolean.FALSE, drivable.get(myCar));

        // interface property get
        BeanProperty<SupportsTrailerHitch> towedVehicle = new BeanProperty<SupportsTrailerHitch>(SupportsTrailerHitch.class, "towedVehicle", true, true);
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
        BeanProperty<SupportsTrailerHitch> towAndPaint = new BeanProperty<SupportsTrailerHitch>(SupportsTrailerHitch.class, "towedVehicle.color", true, true);
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
        BeanProperty<SupportsTrailerHitch> red = new BeanProperty<SupportsTrailerHitch>(SupportsTrailerHitch.class, "towedVehicle.color.red", true, false);
        rainbowCar.setColor(Color.blue);
        assertEquals(new Integer(0), red.get(towTruck));
        rainbowCar.setColor(Color.red);
        assertEquals(new Integer(255), red.get(towTruck));
        assertEquals(int.class, red.getValueClass());
    }

    public void testBadSetterMethod() {
        BeanProperty<Truck> painter = new BeanProperty<Truck>(Truck.class, "towedVehicle.color", true, true);
        Truck truck = new Truck(2);
        truck.setTowedVehicle(new Automobile(true));
        try {
            painter.set(truck, "this should break");
        } catch (IllegalArgumentException e) {
            assertEquals("Automobile.setColor(Color) cannot be called with an instance of String", e.getMessage());
        }
    }
    
    /**
     * Test that BeanProperties work for interfaces.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=183">Issue 183</a>
     */
    public void testInterfaces() {
        BeanProperty<SubInterface> codeProperty = new BeanProperty<SubInterface>(SubInterface.class, "code", true, true);
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
        this.numSeats = seats;
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