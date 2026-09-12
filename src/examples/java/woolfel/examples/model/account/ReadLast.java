package woolfel.examples.model.account;

import org.morendo.rete.macro.ReadMacro;

public class ReadLast implements ReadMacro {
    public ReadLast() {}

    public Object getProperty(Object instance) {
        return ((woolfel.examples.model.Account) instance).getLast();
    }
}
