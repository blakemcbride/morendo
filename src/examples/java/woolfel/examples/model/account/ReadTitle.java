package woolfel.examples.model.account;

import org.morendo.rete.macro.ReadMacro;

public class ReadTitle implements ReadMacro {
    public ReadTitle() {}

    public Object getProperty(Object instance) {
        return ((woolfel.examples.model.Account) instance).getTitle();
    }
}
