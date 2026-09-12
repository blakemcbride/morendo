package org.morendo.rete.functions.macro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.morendo.rete.Constants;
import org.morendo.rete.Defclass;
import org.morendo.rete.Module;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MacroGenerator {

    protected Logger log = LogManager.getLogger(MacroGenerator.class);
    protected int tabs = 0;
    public static final String READ_PREFIX = "Read";
    public static final String WRITE_PREFIX = "Write";
    public static final String JAVA_FILE_EXT = ".java";

    public MacroGenerator() {}

    public void generateMacros(Defclass defclass, Module defmodule) {
        String packagename = defclass.getClassObject().getName().toLowerCase();
        PropertyDescriptor[] properties = defclass.getPropertyDescriptors();
        for (int idx = 0; idx < properties.length; idx++) {
            PropertyDescriptor prop = properties[idx];
            String formattedClassname = this.formatClassname(prop.getName());
            StringBuilder readbuf = new StringBuilder();
            StringBuilder writebuf = new StringBuilder();
            String basePath =
                    defmodule.getWorkingDirectory() + "/" + packagename.replace('.', '/') + "/";
            String readClass = basePath + READ_PREFIX + formattedClassname + JAVA_FILE_EXT;
            String writeClass = basePath + WRITE_PREFIX + formattedClassname + JAVA_FILE_EXT;

            // make the directories for the class
            File baseDir = new File(basePath);
            baseDir.mkdirs();

            // generate the classes
            generateReadMacro(
                    readbuf,
                    prop,
                    packagename,
                    formattedClassname,
                    defclass.getClassObject().getName());
            generateWriteMacro(
                    writebuf,
                    prop,
                    packagename,
                    formattedClassname,
                    defclass.getClassObject().getName());

            // write the classes
            writeClass(readbuf, readClass);
            writeClass(writebuf, writeClass);
        }
    }

    public void generateReadMacro(
            StringBuilder buf,
            PropertyDescriptor property,
            String packageName,
            String className,
            String castClassName) {
        String readName = READ_PREFIX + className;
        writeTabs(buf);
        buf.append("package " + packageName + ";" + Constants.LINEBREAK);
        buf.append(Constants.LINEBREAK);
        writeTabs(buf);
        buf.append("import org.morendo.rete.macro.ReadMacro;" + Constants.LINEBREAK);
        buf.append(Constants.LINEBREAK);
        writeTabs(buf);
        buf.append("public class " + readName + " implements ReadMacro {" + Constants.LINEBREAK);
        // push tab and generate body of the class
        pushTab();
        writeTabs(buf);
        buf.append("public " + readName + "() {}" + Constants.LINEBREAK);
        buf.append(Constants.LINEBREAK);
        generateReadMethod(buf, castClassName, className);
        // end body of the class
        popTab();
        writeTabs(buf);
        buf.append("}" + Constants.LINEBREAK);
    }

    public void generateReadMethod(StringBuilder buf, String castClassName, String propertyName) {
        writeTabs(buf);
        buf.append("public Object getProperty(Object instance) {" + Constants.LINEBREAK);
        pushTab();
        writeTabs(buf);
        buf.append(
                "return (("
                        + castClassName
                        + ")instance).get"
                        + propertyName
                        + "();"
                        + Constants.LINEBREAK);
        popTab();
        writeTabs(buf);
        buf.append("}" + Constants.LINEBREAK);
    }

    public void generateWriteMacro(
            StringBuilder buf,
            PropertyDescriptor property,
            String packageName,
            String className,
            String castClassName) {
        String writeName = WRITE_PREFIX + className;
        writeTabs(buf);
        buf.append("package " + packageName + ";" + Constants.LINEBREAK);
        buf.append(Constants.LINEBREAK);
        writeTabs(buf);
        buf.append("import org.morendo.rete.macro.WriteMacro;" + Constants.LINEBREAK);
        buf.append(Constants.LINEBREAK);
        writeTabs(buf);
        buf.append("public class " + writeName + " implements WriteMacro {" + Constants.LINEBREAK);
        // push tab and generate body of the class
        pushTab();
        writeTabs(buf);
        buf.append("public " + writeName + "() {}" + Constants.LINEBREAK);
        buf.append(Constants.LINEBREAK);
        generateWriteMethod(
                buf, castClassName, className, property.getReadMethod().getReturnType());
        // end body of the class
        popTab();
        writeTabs(buf);
        buf.append("}" + Constants.LINEBREAK);
    }

    public void generateWriteMethod(
            StringBuilder buf, String castClassName, String propertyName, Class<?> propertyType) {
        writeTabs(buf);
        buf.append(
                "public void setProperty(Object instance, Object value) {" + Constants.LINEBREAK);
        pushTab();
        writeTabs(buf);
        buf.append("((" + castClassName + ")instance).set" + propertyName + "(");
        // check the type
        buf.append("(" + formattedPropertyType(propertyType) + ")value");
        buf.append(");" + Constants.LINEBREAK);
        popTab();
        writeTabs(buf);
        buf.append("}" + Constants.LINEBREAK);
    }

    protected String formattedPropertyType(Class<?> propertyType) {
        if (propertyType.getName() == "int") {
            return "Integer";
        } else if (propertyType.getName() == "short") {
            return "Short";
        } else if (propertyType.getName() == "float") {
            return "Float";
        } else if (propertyType.getName() == "long") {
            return "Long";
        } else if (propertyType.getName() == "double") {
            return "Double";
        } else if (propertyType.getName() == "boolean") {
            return "Boolean";
        } else {
            return propertyType.getTypeName();
        }
    }

    public void writeTabs(StringBuilder buf) {
        for (int idx = 0; idx < tabs; idx++) {
            buf.append("    ");
        }
    }

    public void pushTab() {
        this.tabs++;
    }

    public void popTab() {
        this.tabs--;
    }

    public String formatClassname(String propertyName) {
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    public boolean writeClass(StringBuilder buf, String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(buf.toString());
            writer.close();
        } catch (IOException e) {
            log.fatal(e.toString(), e);
        }
        return true;
    }
}
