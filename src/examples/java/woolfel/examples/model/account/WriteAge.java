package woolfel.examples.model.account;

import org.morendo.rete.macro.WriteMacro;

public class WriteAge implements WriteMacro {
    public WriteAge() {}

    public void setProperty(Object instance, Object value) {
        ((woolfel.examples.model.Account) instance).setAge((Integer) value);
    }
}
