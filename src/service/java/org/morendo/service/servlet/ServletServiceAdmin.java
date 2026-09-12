package org.morendo.service.servlet;

import jakarta.servlet.ServletContext;

import org.morendo.service.EnginePool;
import org.morendo.service.RuleApplication;
import org.morendo.service.ServiceAdministration;
import org.morendo.service.ServiceConfiguration;

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

    private EnginePool pool(String ruleApplication, String version) {
        return this.ruleService.getEnginePool(ruleApplication, version);
    }

    public int getEnginePoolCount(String ruleApplication, String version) {
        EnginePool pool = pool(ruleApplication, version);
        return pool == null ? 0 : pool.idleCount();
    }

    public List<?> getEngines(String applicationName, String version) {
        EnginePool pool = pool(applicationName, version);
        return pool == null ? List.of() : pool.snapshot();
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
        EnginePool pool = pool(ruleApplication, version);
        if (pool != null) {
            pool.closeAll();
            pool.fill();
        }
        servletContext.log(
                "--- Finished reinitializing rule application: " + ruleApplication + " " + version);
    }

    public boolean reloadFunctionPackage(String ruleApplication, String version) {
        servletContext.log(
                "--- Start reloading Function Package: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        boolean reload =
                pool != null && pool.forEachIdle(pool.getApplication()::reloadFunctionGroups);
        servletContext.log(
                "--- Finished reloading Function Package: " + ruleApplication + " " + version);
        return reload;
    }

    public boolean reloadInitialData(String ruleApplication, String version) {
        servletContext.log("--- Start reloading Initial Data: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        boolean reload = pool != null && pool.forEachIdle(pool.getApplication()::reloadInitialData);
        servletContext.log(
                "--- Finished reloading Initial Data: " + ruleApplication + " " + version);
        return reload;
    }

    public boolean reloadRuleset(String ruleApplication, String version) {
        servletContext.log("--- Start reloading Ruleset: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        boolean reload = pool != null && pool.forEachIdle(pool.getApplication()::reloadRulesets);
        servletContext.log("--- Finished reloading Ruleset: " + ruleApplication + " " + version);
        return reload;
    }
}
