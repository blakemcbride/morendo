package org.morendo.service.servlet;

import jakarta.servlet.ServletContext;

import org.morendo.rete.Rete;
import org.morendo.service.RuleApplication;
import org.morendo.service.ServiceAdministration;
import org.morendo.service.ServiceConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ServletServiceAdmin implements ServiceAdministration {

    private RuleStartupService ruleService = null;
    private ServletContext servletContext = null;

    public ServletServiceAdmin(RuleStartupService service) {
        this.ruleService = service;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public RuleApplication getApplication(String applicationName, String version) {
        String key = applicationName + "::" + version;
        return this.ruleService.getRuleApplicationMap().get(key);
    }

    public int getEnginePoolCount(String ruleApplication, String version) {
        String key = ruleApplication + "::" + version;
        List<Rete> queue = this.ruleService.getEngineMap().get(key);
        return queue == null ? 0 : queue.size();
    }

    public List<?> getEngines(String applicationName, String version) {
        String key = applicationName + "::" + version;
        List<Rete> queue = this.ruleService.getEngineMap().get(key);
        return queue == null ? List.of() : new ArrayList<>(queue);
    }

    public List<?> getRuleApplications() {
        return this.ruleService.getRuleApplications();
    }

    public ServiceConfiguration getServiceConfiguration() {
        return ruleService.getServiceConfiguration();
    }

    public void reinitialize(String ruleApplication, String version) {
        servletContext.log(
                "--- Start reinitializing rule application: " + ruleApplication + " " + version);
        String key = ruleApplication + "::" + version;
        List<Rete> queue = this.ruleService.getEngineMap().remove(key);
        // first close all the engine instances.
        if (queue != null) {
            for (Rete engine : queue) {
                engine.close();
            }
            queue.clear();
        }

        // Now reload the RuleApplication and recreate the engine instances
        RuleApplication app = this.ruleService.getRuleApplicationMap().get(key);
        queue = new ArrayList<>();
        this.ruleService.getEngineMap().put(key, queue);
        for (int idx = 0; idx < app.getInitialPool(); idx++) {
            Rete engine = new Rete();
            queue.add(engine);
            app.reinitializeEngine(engine);
        }
        servletContext.log(
                "--- Finished reinitializing rule application: " + ruleApplication + " " + version);
    }

    public boolean reloadFunctionPackage(String ruleApplication, String version) {
        servletContext.log(
                "--- Start reloading Function Package: " + ruleApplication + " " + version);
        boolean reload = false;
        String key = ruleApplication + "::" + version;
        RuleApplication app = this.ruleService.getRuleApplicationMap().get(key);
        List<Rete> queue = this.ruleService.getEngineMap().get(key);
        for (Rete engine : queue == null ? List.<Rete>of() : queue) {
            reload = app.reloadFunctionGroups(engine);
            if (!reload) {
                break;
            }
        }
        servletContext.log(
                "--- Finished reloading Function Package: " + ruleApplication + " " + version);
        return reload;
    }

    public boolean reloadInitialData(String ruleApplication, String version) {
        servletContext.log("--- Start reloading Initial Data: " + ruleApplication + " " + version);
        boolean reload = false;
        String key = ruleApplication + "::" + version;
        RuleApplication app = this.ruleService.getRuleApplicationMap().get(key);
        List<Rete> queue = this.ruleService.getEngineMap().get(key);
        for (Rete engine : queue == null ? List.<Rete>of() : queue) {
            reload = app.reloadInitialData(engine);
            if (!reload) {
                break;
            }
        }
        servletContext.log(
                "--- Finished reloading Initial Data: " + ruleApplication + " " + version);
        return reload;
    }

    public boolean reloadRuleset(String ruleApplication, String version) {
        servletContext.log("--- Start reloading Ruleset: " + ruleApplication + " " + version);
        boolean reload = false;
        String key = ruleApplication + "::" + version;
        RuleApplication app = this.ruleService.getRuleApplicationMap().get(key);
        List<Rete> queue = this.ruleService.getEngineMap().get(key);
        for (Rete engine : queue == null ? List.<Rete>of() : queue) {
            reload = app.reloadRulesets(engine);
            if (!reload) {
                break;
            }
        }
        servletContext.log("--- Finished reloading Ruleset: " + ruleApplication + " " + version);
        return reload;
    }
}
