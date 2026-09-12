package woolfel.rete;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.functions.io.BatchFunction;

public class ExecutionTest {

    @Test
    public void testExecution() {
        org.morendo.rete.Rete engine = new org.morendo.rete.Rete();
        // exists_sample15 defines one rule and asserts its facts; the batch function
        // is exercised explicitly the way the original test did.
        Function batch = engine.findFunction(BatchFunction.BATCH);
        Parameter[] parameters =
                new Parameter[] {
                    new ValueParam(ValueType.STRING, "./samples/exists/exists_sample15.clp")
                };
        batch.executeFunction(engine, parameters);
        assertEquals(1, engine.getCurrentFocus().getRuleCount());
        int fired = engine.fire();
        assertEquals(1, fired);
    }
}
