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

import java.util.Set;

/**
 * Function takes a string and Set<String> of tokens. It will return the total match count. For
 * example, say you want to see if the response in a chat has words related to a topic.
 *
 * <p>the text of their response is the first parameter. The second parameter is a Set<String> of
 * the language specific tokens for the topic. The function will count how many matches are in the
 * chat response
 *
 * @author peter
 */
public class TokenMatchFunction implements Function {

    /** */
    public static final String TOKENMATCH = "token-match";

    public TokenMatchFunction() {}

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Integer total = 0;
        if (params != null && params.length == 2) {
            try {
                String rawText = params[1].getStringValue();
                rawText = rawText.replaceAll("[/./,/!/?/)/(/:/`/^/]]", "");
                BoundParam bp = (BoundParam) params[0];
                Object resolvedValue = null;
                if (bp.isObjectBinding()) {
                    resolvedValue = bp.getValue(engine, ValueType.OBJECT);
                } else {
                    resolvedValue = bp.getValue();
                }
                if (resolvedValue instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<String> stop = (Set<String>) resolvedValue;
                    for (String word : stop) {
                        for (int i = 0; i < rawText.length(); i++) {
                            int idof = rawText.substring(i).indexOf(word);
                            if (idof > -1) {
                                total++;
                                i = idof + word.length();
                            } else {
                                break;
                            }
                        }
                    }
                } else if (resolvedValue instanceof String[]) {
                    String[] stop = (String[]) resolvedValue;
                    for (int s = 0; s < stop.length; s++) {
                        for (int i = 0; i < rawText.length(); i++) {
                            if (rawText.substring(i).indexOf(stop[s]) > -1) {
                                total++;
                                i = i + stop[s].length();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.INTEGER_OBJECT, total);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return TOKENMATCH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, BoundParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            return buf.toString();
        } else {
            return "(stop-word <set> <string>)";
        }
    }
}
