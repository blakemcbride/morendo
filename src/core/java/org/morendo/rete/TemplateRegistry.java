/*
 * Copyright 2002-2008 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.morendo.rete.exception.TemplateAssociationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Java classes an engine knows as templates: each declared class gets a Defclass (its
 * introspection data) and a Deftemplate in the current module. Owned by Rete, which exposes the
 * same operations on its facade.
 */
public class TemplateRegistry {

    private static final Logger log = LogManager.getLogger(TemplateRegistry.class);

    private final Rete engine;

    private final Map<Object, Defclass> defclass = new HashMap<>();

    private final Map<String, Defclass> defclassByName = new HashMap<>();

    private final Map<String, Defclass> templateToDefclass = new HashMap<>();

    private final Map<Object, Template> classToTemplate = new HashMap<>();

    TemplateRegistry(Rete engine) {
        this.engine = engine;
    }

    /** Finds a template by name in any module, the main module last. */
    public Template findTemplate(String name) {
        WorkingMemory memory = engine.getWorkingMemory();
        String moduleName = null;
        if (name.indexOf("::") > 0) {
            String[] parts = name.split("::");
            moduleName = parts[0].toUpperCase();
            name = parts[1];
        }
        if (moduleName != null) {
            Module mod = memory.findModule(moduleName);
            return mod == null ? null : mod.getTemplate(name);
        }
        Template tmpl = memory.getCurrentFocus().getTemplate(name);
        if (tmpl == null) {
            tmpl = memory.getMain().getTemplate(name);
        }
        if (tmpl == null) {
            for (Module mod : memory.getModules()) {
                tmpl = mod.getTemplate(name);
                if (tmpl != null) {
                    break;
                }
            }
        }
        return tmpl;
    }

    public void declareObject(String className, String templateName, String parent)
            throws ClassNotFoundException {
        try {
            declareObject(Class.forName(className), templateName, parent);
        } catch (ClassNotFoundException e) {
            log.debug(e.toString(), e);
            throw e;
        }
    }

    /**
     * Declares a class as a template. The template is named after the class unless a name is given;
     * a parent template name makes the new template inherit its slots.
     */
    public void declareObject(Class<?> obj, String templateName, String parent) {
        if (this.defclass.containsKey(obj)) {
            return;
        }
        Defclass dclass = new Defclass(obj);
        this.defclassByName.put(obj.getName(), dclass);
        this.defclass.put(obj, dclass);
        if (templateName == null) {
            templateName = obj.getName();
        }
        this.templateToDefclass.put(templateName, dclass);
        Module focus = engine.getCurrentFocus();
        if (!focus.containsTemplate(dclass)) {
            Template dtemp = null;
            if (parent != null) {
                Template ptemp = focus.findParentTemplate(parent);
                if (ptemp != null) {
                    dtemp = dclass.createDeftemplate(templateName, ptemp);
                    dtemp.setParent(ptemp);
                }
            } else {
                dtemp = dclass.createDeftemplate(templateName);
            }
            this.classToTemplate.put(obj, dtemp);
            focus.addTemplate(dtemp, engine, engine.getWorkingMemory());
            if (this.announce) {
                engine.writeMessage(dtemp.getName() + Constants.LINEBREAK, "t");
            }
        }
    }

    private boolean announce = true;

    /** Whether declaring a class prints the template's name; the built-in graph classes do not. */
    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    /** Removes a declared class if no rule uses its template; false otherwise. */
    public boolean removeObjectType(Class<?> clzz) {
        Template template = this.classToTemplate.get(clzz);
        if (template.getSlotsUsed() == 0) {
            this.defclass.remove(clzz);
            this.defclassByName.remove(clzz.getName());
            this.templateToDefclass.remove(clzz.getName());
            this.classToTemplate.remove(clzz);
            return true;
        }
        return false;
    }

    public void declareCube(Cube cube) {
        if (!this.defclass.containsKey(cube)) {
            Defclass dclass = new Defclass(cube.getClass());
            this.defclass.put(cube, dclass);
            this.templateToDefclass.put(cube.getName(), dclass);
            Template dtemp = dclass.createCubeTemplate(cube);
            this.classToTemplate.put(cube, dtemp);
            engine.getCurrentFocus().addTemplate(dtemp, engine, engine.getWorkingMemory());
            engine.writeMessage(dtemp.getName() + Constants.LINEBREAK, "t");
        }
    }

    public void declareTemplate(Template temp) {
        Module focus = engine.getCurrentFocus();
        if (!focus.containsTemplate(temp.getName())) {
            focus.addTemplate(temp, engine, engine.getWorkingMemory());
        }
    }

    public Deftemplate findDeftemplate(Class<?> clazz) {
        return this.defclass.containsKey(clazz)
                ? (Deftemplate) this.classToTemplate.get(clazz)
                : null;
    }

    /** Associates a further class with an already declared template. */
    public void addAssociation(Class<?> clazz, Deftemplate template)
            throws TemplateAssociationException {
        if (this.defclass.containsKey(clazz)) {
            throw new TemplateAssociationException(
                    clazz.getName() + " has already been declared. Cannot add association");
        }
        Defclass dclass = this.templateToDefclass.get(template.getName());
        this.defclass.put(clazz, dclass);
        this.classToTemplate.put(clazz, template);
    }

    public void addAssociation(Class<?> clazz, String templateName)
            throws TemplateAssociationException {
        Defclass dclass = this.templateToDefclass.get(templateName);
        if (dclass == null) {
            throw new TemplateAssociationException(
                    templateName + " not found. Cound not add association");
        }
        addAssociation(clazz, findDeftemplate(dclass.getClassObject()));
    }

    public Defclass findDefclass(Class<?> clazz) {
        return this.defclass.get(clazz);
    }

    /** The Defclass of an object's class. */
    public Defclass findDefclass(Object key) {
        return this.defclass.get(key.getClass());
    }

    public Defclass findDefclassByTemplate(String templateName) {
        return this.templateToDefclass.get(templateName);
    }

    public Defclass findDefclassByName(String key) {
        return this.defclassByName.get(key);
    }

    public Set<Map.Entry<Object, Defclass>> getDefclasses() {
        return this.defclass.entrySet();
    }

    /** Forgets every declared class. */
    public void clear() {
        for (Defclass dclass : this.defclass.values()) {
            dclass.clear();
        }
        this.defclass.clear();
        this.defclassByName.clear();
        this.templateToDefclass.clear();
        this.classToTemplate.clear();
    }
}
