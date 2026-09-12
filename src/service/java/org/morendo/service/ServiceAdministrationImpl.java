package org.morendo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ServiceAdministrationImpl implements ServiceAdministration {

    private Logger log = LogManager.getLogger(ServiceAdministrationImpl.class);
    private RuleServiceImpl ruleService = null;

    public ServiceAdministrationImpl(RuleServiceImpl service) {
        this.ruleService = service;
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

    public List<?> getEngines(String ruleApplication, String version) {
        EnginePool pool = pool(ruleApplication, version);
        return pool == null ? List.of() : pool.snapshot();
    }

    public List<?> getRuleApplications() {
        return this.ruleService.getRuleApplications();
    }

    public ServiceConfiguration getServiceConfiguration() {
        return ruleService.getServiceConfiguration();
    }

    public void reinitialize(String ruleApplication, String version) {
        log.info("--- Start reinitializing rule application: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        if (pool != null) {
            pool.closeAll();
            pool.fill();
        }
        log.info(
                "--- Finished reinitializing rule application: " + ruleApplication + " " + version);
    }

    public boolean reloadFunctionPackage(String ruleApplication, String version) {
        log.info("--- Start reloading Function Package: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        boolean reload =
                pool != null && pool.forEachIdle(pool.getApplication()::reloadFunctionGroups);
        log.info("--- Finished reloading Function Package: " + ruleApplication + " " + version);
        return reload;
    }

    public boolean reloadInitialData(String ruleApplication, String version) {
        log.info("--- Start reloading Initial Data: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        boolean reload = pool != null && pool.forEachIdle(pool.getApplication()::reloadInitialData);
        log.info("--- Finished reloading Initial Data: " + ruleApplication + " " + version);
        return reload;
    }

    public boolean reloadRuleset(String ruleApplication, String version) {
        log.info("--- Start reloading Ruleset: " + ruleApplication + " " + version);
        EnginePool pool = pool(ruleApplication, version);
        boolean reload = pool != null && pool.forEachIdle(pool.getApplication()::reloadRulesets);
        log.info("--- Finished reloading Ruleset: " + ruleApplication + " " + version);
        return reload;
    }
}
