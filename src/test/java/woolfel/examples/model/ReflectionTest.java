package woolfel.examples.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ReflectionTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testLookup1() {
        String methd = "setCashCountry";
        Class[] params = new Class[2];
        params[0] = java.lang.Double.class;
        params[1] = java.lang.String.class;
        Class account4 = Account4.class;
        try {
            // Account4 declares both setCashCountry(double, String) and
            // setCashCountry(Double, String); the boxed lookup finds the boxed overload.
            Method m = account4.getMethod(methd, params);
            assertNotNull(m);
            assertEquals(java.lang.Double.class, m.getParameterTypes()[0]);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(true);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(true);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testLookup2() {
        String methd = "setCashCountry";
        Class[] params = new Class[2];
        params[0] = double.class;
        params[1] = java.lang.String.class;
        Class account4 = Account4.class;
        try {
            Method m = account4.getMethod(methd, params);
            assertNotNull(m);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }
}
