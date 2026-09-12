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
// import java.util.HashSet;
import java.util.Map;

// import java.util.Set;

/**
 * Function will parse a string, strip punctuation and return a HashMap<String,Integer> of the
 * tokens.
 *
 * @author peter
 */
public class TokenizeFunction implements Function {

    /** */
    public static final String TOKENIZE = "tokenize";

    public TokenizeFunction() {}

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Map<String, Integer> wordcount = new HashMap<>();
        if (params != null && params.length == 1) {
            try {
                String rawText = params[0].getStringValue();
                if (rawText != null && rawText.length() > 0) {
                    for (String t : rawText.trim().split("\\s+")) {
                        t = t.replaceAll("[/./,/!/?/)/(/:/`/^/]]", "");
                        Integer c = wordcount.get(t);
                        if (c == null) {
                            c = 0;
                        }
                        c++;
                        wordcount.put(t, c);
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

    /* TODO - check if may be needed in future
    private Set<String> read(String[] words) {
    	Set<String> wordset = new HashSet<>();
    	for (String s: words) {
    		wordset.add(s);
    	}
    	return wordset;
    }
    */

    public String getName() {
        return TOKENIZE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, BoundParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            return buf.toString();
        } else {
            return "(tokenize <string>)";
        }
    }
}
