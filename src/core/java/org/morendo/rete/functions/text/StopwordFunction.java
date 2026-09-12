package org.morendo.rete.functions.text;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Function will parse a string and return a Map<String,Integer> after it has filtered out the
 * stopwords. The parameters it takes is raw text and array of stop words.
 *
 * @author peter
 */
public class StopwordFunction implements Function {

    /** */
    public static final String STOPWORD = "stop-word";

    public StopwordFunction() {}

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Map<String, Integer> wordcount = new HashMap<>();
        if (params != null && params.length == 2) {
            try {
                String rawText = params[0].getStringValue();
                BoundParam bp = (BoundParam) params[1];
                Object resolvedValue = bp.getValue(engine, ValueType.OBJECT);
                if (resolvedValue instanceof String[]) {
                    Set<String> stop = this.read((String[]) resolvedValue);
                    for (String t : rawText.trim().split("\\s+")) {
                        t = t.replaceAll("[/./,/!/?/)/(/:/`/^/]]", "");
                        if (!stop.contains(t)) {
                            Integer c = wordcount.get(t);
                            if (c == null) {
                                c = 0;
                            }
                            c++;
                            wordcount.put(t, c);
                        }
                    }
                }
            } catch (Exception e) {
                //
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT, wordcount);
        ret.addReturnValue(rv);
        return ret;
    }

    private Set<String> read(String[] words) {
        Set<String> wordset = new HashSet<>();
        for (String s : words) {
            wordset.add(s);
        }
        return wordset;
    }

    public String getName() {
        return STOPWORD;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, BoundParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            return buf.toString();
        } else {
            return "(stop-word <string> <array>)";
        }
    }
}
