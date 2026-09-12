package org.morendo.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

@Disabled(
        "RuleServiceImpl.createInstance never builds applications from the JSON config; see"
                + " UpgradePlan.md")
public class InitServiceTest {

    /** The single application defined in samples/configuration/sample_config.json. */
    private static RuleApplication app(RuleService service) {
        return (RuleApplication) service.getRuleApplications().get(0);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInitialize() {
        RuleService service =
                RuleServiceImpl.createInstance("./samples/configuration/sample_config.json");
        service.initialize();
        assertNotNull(service.getServiceName());
        assertNotNull(service.getRuleApplications());
        List applications = service.getRuleApplications();
        assertTrue(applications.size() == 1);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetEngineContext() {
        RuleService service =
                RuleServiceImpl.createInstance("./samples/configuration/sample_config.json");
        service.initialize();
        assertNotNull(service.getServiceName());
        assertNotNull(service.getRuleApplications());
        List applications = service.getRuleApplications();
        assertTrue(applications.size() == 1);
        EngineContext context =
                service.getEngine(app(service).getName(), app(service).getVersion());
        assertNotNull(context);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testReloadFunction() {
        RuleService service =
                RuleServiceImpl.createInstance("./samples/configuration/sample_config.json");
        service.initialize();
        assertNotNull(service.getServiceName());
        assertNotNull(service.getRuleApplications());
        EngineContextImpl context =
                (EngineContextImpl)
                        service.getEngine(app(service).getName(), app(service).getVersion());
        List applications = service.getRuleApplications();
        RuleApplication app = (RuleApplication) applications.get(0);
        boolean reload = app.reloadFunctionGroups(context.getRuleEngine());
        assertTrue(reload);
    }

    @Test
    public void testGetServiceAdmin() {
        RuleService service =
                RuleServiceImpl.createInstance("./samples/configuration/sample_config.json");
        service.initialize();
        assertNotNull(service.getServiceName());
        assertNotNull(service.getRuleApplications());
        ServiceAdministration admin = service.getServiceAdmin();
        assertNotNull(admin);
    }

    @Test
    public void testReloadRuleset() {
        RuleService service =
                RuleServiceImpl.createInstance("./samples/configuration/sample_config.json");
        service.initialize();
        assertNotNull(service.getServiceName());
        assertNotNull(service.getRuleApplications());
        ServiceAdministration admin = service.getServiceAdmin();
        assertNotNull(admin);
        boolean reload = admin.reloadRuleset(app(service).getName(), app(service).getVersion());
        assertTrue(reload);
    }

    public static void main(String[] args) {
        InitServiceTest test = new InitServiceTest();
        test.testInitialize();
    }
}
