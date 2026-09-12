package woolfel.examples.model.account;

import org.morendo.rete.macro.ReadMacro;

public class ReadAreaCode implements ReadMacro {
    public ReadAreaCode() {}

    public Object getProperty(Object instance) {
        return ((woolfel.examples.model.Account) instance).getAreaCode();
    }
}
