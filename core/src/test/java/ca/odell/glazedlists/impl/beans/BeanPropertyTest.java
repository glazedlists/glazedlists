/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import java.awt.Color;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the BeanProperty works as expected.
 *
 * @author <a href="mailto;kevin@swank.ca">Kevin Maltby</a>
 * @author manningj
 */
public class BeanPropertyTest {

    /**
     * Tests that simple properties work.
     */
    @Test
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
    @Test
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

    @Test
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

    @Test
    public void testThisProperty() {
        try {
            new BeanProperty<Truck>(Truck.class, "this", true, true);
            fail("failed to throw an exception when creating a writable BeanProperty with 'this'");
        } catch (IllegalArgumentException e) {
            // expected
        }

        BeanProperty<Truck> identity = new BeanProperty<Truck>(Truck.class, "this", true, false);

        Truck truck = new Truck(2);
        assertSame(truck, identity.get(truck));
        assertSame(Truck.class, identity.getValueClass());
    }

    @Test
    public void testResolveGenericReturnType() {
        final BeanProperty<Bus> busPassengers = new BeanProperty<Bus>(Bus.class, "passenger", true, true);
        assertSame(People.class, busPassengers.getValueClass());
    }

    @Test
    public void testResolveGenericParameterType() {
        final BeanProperty<Bus> busDriver = new BeanProperty<Bus>(Bus.class, "driver", false, true);
        assertSame(People.class, busDriver.getValueClass());
    }

    /**
     * Test that BeanProperties work for interfaces.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=183">Issue 183</a>
     */
    @Test
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
    @Override
	public String getCode() {
        return code;
    }
    @Override
	public void setCode(String code) {
        this.code = code;
    }
    @Override
	public String getName() {
        return name;
    }
    @Override
	public void setName(String name) {
        this.name = name;
    }
}

/**
 * A test object.
 */
class Automobile<E extends Passenger> {

    private boolean automatic;
    private Color color;
    private boolean fullOfGas;
    private E passenger;

    public Automobile(boolean automatic) {
        this.automatic = automatic;
        color = Color.BLACK;
        fullOfGas = true;
    }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public boolean getDrivable() { return fullOfGas; }
    public void setFullOfGas(boolean fullOfGas) { this.fullOfGas = fullOfGas; }

    public boolean isAutomatic() { return automatic; }

    public E getPassenger() { return passenger; }
    public void setPassenger(E passenger) { this.passenger = passenger; }

    // generic array
    public E[] getAllPassengers() { return null; }

    // generic setter only
    public void setDriver(E driver) { }
}

class Bus extends Automobile<People> {
    Bus(boolean automatic) {
        super(automatic);
    }
}

class Truck<E extends Passenger,T extends Passenger> extends Automobile<E> implements SupportsTrailerHitch<T> {

    private int numSeats;
    private Automobile<T> towedVehicle;

    public Truck(int seats) {
        super(false);
        this.numSeats = seats;
    }
    public int getNumSeats() {
        return numSeats;
    }
    @Override
	public void setTowedVehicle(Automobile<T> towedVehicle) {
        this.towedVehicle = towedVehicle;
    }
    @Override
	public Automobile<T> getTowedVehicle() {
        return towedVehicle;
    }
}

interface SupportsTrailerHitch<T extends Passenger> {
    public void setTowedVehicle(Automobile<T> towedVehicle);
    public Automobile<T> getTowedVehicle();
}

class TowingCompany {
    public SupportsTrailerHitch getTowTruck() {
        return new Truck(3);
    }
}

@FunctionalInterface
interface Passenger {
    public int getNumLegs();
}

class People implements Passenger {
    @Override
	public int getNumLegs() { return 2; }
}
class Cattle implements Passenger {
    @Override
	public int getNumLegs() { return 4; }
}
