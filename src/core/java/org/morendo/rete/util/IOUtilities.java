/*
 * Copyright 2002-2007 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete.util;

import org.morendo.rete.Fact;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * IOUtilities contains some commonly used static methods for saving and loading files.
 *
 * @author Peter
 */
public class IOUtilities {

    public static boolean saveFacts(List<?> facts, String output) {
        try {
            FileWriter writer = new FileWriter(output);
            java.util.Iterator<?> itr = facts.iterator();
            while (itr.hasNext()) {
                Fact f = (Fact) itr.next();
                writer.write(f.toFactString());
            }
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Prefix for locations that name a classpath resource, e.g. "classpath:rules/init.clp". */
    public static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Opens a location for reading. The location may be a URL with a scheme (http:, https:, file:,
     * jar:, ...), a classpath resource ("classpath:dir/file"), or a plain file-system path. Callers
     * close the returned stream.
     */
    public static InputStream open(String location) throws IOException {
        if (location.startsWith(CLASSPATH_PREFIX)) {
            String resource = location.substring(CLASSPATH_PREFIX.length());
            InputStream in = IOUtilities.class.getClassLoader().getResourceAsStream(resource);
            if (in == null) {
                throw new FileNotFoundException(location);
            }
            return in;
        }
        if (hasScheme(location)) {
            return toURL(location).openStream();
        }
        return new FileInputStream(location);
    }

    /** Converts a URL string to a URL without the deprecated URL(String) constructor. */
    public static URL toURL(String location) throws MalformedURLException {
        try {
            return URI.create(location).toURL();
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException(location + ": " + e.getMessage());
            mue.initCause(e);
            throw mue;
        }
    }

    /**
     * True if the location starts with a URL scheme such as "http://" (a Windows drive letter does
     * not count).
     */
    public static boolean hasScheme(String location) {
        return location.matches("^[a-zA-Z][a-zA-Z0-9+.\\-]+://.*");
    }
}
