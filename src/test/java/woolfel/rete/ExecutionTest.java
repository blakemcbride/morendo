package woolfel.rete;

import static org.junit.jupiter.api.Assertions.*;

import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;
import org.jamocha.rete.functions.io.BatchFunction;
import org.junit.jupiter.api.Test;

public class ExecutionTest {

    @Test
    public void testExecution() {
        org.jamocha.rete.Rete engine = new org.jamocha.rete.Rete();
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
