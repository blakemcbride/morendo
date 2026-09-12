package org.jamocha.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jamocha.rete.Rete;
import org.jamocha.rete.exception.AssertException;
import org.jamocha.rete.exception.RetractException;
import org.jamocha.rete.util.IOUtilities;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * JSON data expects an array of objects that match the name. If you have multiple types of initial
 * data, organize it in separate files. Each file should only have 1 type. The class uses Jackson
 * Databind to read the data. The name should be the fully qualified classname.
 *
 * @author peter
 * @param <T>
 * @param <T>
 */
public class JSONData<T> implements InitialData {

    @JsonIgnore private transient Logger log = LogManager.getLogger(JSONData.class);
    private String name = null;
    private String url;
    private List<Object> data = null;
    @JsonIgnore private static ObjectMapper mapper = new ObjectMapper();
    @JsonIgnore private ServletContext servletCtx = null;

    public JSONData() {}

    @JsonIgnore
    public void setServletContext(ServletContext ctx) {
        this.servletCtx = ctx;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonIgnore
    @Override
    public String getDataType() {
        return InitialData.JSON;
    }

    @Override
    public Object getData() {
        return this.data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    @Override
    public boolean loadData(Rete engine) {
        if (data == null && this.name != null) {
            try {
                Class<?> rootclz = Class.forName(this.name);
                if (rootclz != null && this.url != null) {
                    data = loadJsonData(this.url, rootclz);
                }
            } catch (ClassNotFoundException e) {
                log.warn(e.getMessage());
            }
        }
        if (data != null) {
            try {
                engine.assertObjects(data);
                return true;
            } catch (AssertException e) {
                log.info(e.toString(), e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean reloadData(Rete engine) {
        if (data != null) {
            for (int idx = 0; idx < data.size(); idx++) {
                try {
                    engine.retractObject(data.get(idx));
                } catch (RetractException e) {
                    return false;
                }
            }
            try {
                engine.assertObjects(data);
                return true;
            } catch (AssertException e) {
                log.info(e.toString(), e);
                return false;
            }
        }
        return false;
    }

    @JsonIgnore
    public List<Object> loadJsonData(String url, Class<?> T) {
        Reader reader;
        try {
            if (url.startsWith("http://")) {
                try {
                    URL urlObject = IOUtilities.toURL(url);
                    InputStream input = urlObject.openStream();
                    @SuppressWarnings("unchecked")
                    List<Object> data =
                            (List<Object>) mapper.readValue(input, new TypeReference<List<T>>() {});
                    return data;
                } catch (MalformedURLException e) {
                    Logger log = LogManager.getLogger(JSONData.class);
                    log.fatal(e.toString(), e);
                } catch (IOException e) {
                    Logger log = LogManager.getLogger(JSONData.class);
                    log.fatal(e.toString(), e);
                }
            } else if (url.startsWith("/WEB-INF")) {
                InputStream input = this.servletCtx.getResourceAsStream(url);
                @SuppressWarnings("unchecked")
                List<Object> data =
                        (List<Object>) mapper.readValue(input, new TypeReference<List<T>>() {});
                return data;
            } else {
                reader = new FileReader(url);
                @SuppressWarnings("unchecked")
                List<Object> data =
                        (List<Object>) mapper.readValue(reader, new TypeReference<List<T>>() {});
                return data;
            }
        } catch (Exception e) {
            Logger log = LogManager.getLogger(JSONData.class);
            log.fatal(e.toString(), e);
        }
        return null;
    }
}
