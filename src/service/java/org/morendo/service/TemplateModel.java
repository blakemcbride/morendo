package org.morendo.service;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.functions.io.BatchFunction;
import org.morendo.rete.util.IOUtilities;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Template model defines the deftemplates for a rule application configuration.
 *
 * @author Peter Lin
 */
public class TemplateModel implements Model {

    private String contents;
    private String URL;

    public TemplateModel() {
        super();
    }

    /**
     * The contents of the file to pass to Jamocha's parser.
     *
     * @return
     */
    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    /**
     * The URL to get the file or contents
     *
     * @return
     */
    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        URL = url;
    }

    public URL getURLObject() {
        try {
            return IOUtilities.toURL(this.URL);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Current implementation uses BatchFunction to load the deftemplate model. It assumes the URL
     * has all the deftemplate declarations.
     */
    public void loadModel(Rete engine) {
        Function batch = engine.findFunction(BatchFunction.BATCH);
        batch.executeFunction(engine, new Parameter[] {new ValueParam(ValueType.STRING, this.URL)});
    }
}
