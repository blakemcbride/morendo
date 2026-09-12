package woolfel.examples.model.account;

import org.morendo.rete.macro.ReadMacro;

public class ReadNumber implements ReadMacro {
    public ReadNumber() {}

    public Object getProperty(Object instance) {
        return ((woolfel.examples.model.Account) instance).getNumber();
    }
}
