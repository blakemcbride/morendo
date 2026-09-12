package org.morendo.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import woolfel.examples.model.Account;

import java.util.List;

public class JsonDataTest {

    @Test
    public void testLoadJson() {
        JSONData<Object> jdata = new JSONData<>();
        jdata.setName("org.morendo.examples.model.Account");
        jdata.setUrl("./samples/configuration/accounts.json");

        jdata.setData(jdata.loadJsonData(jdata.getUrl(), Account.class));

        assertNotNull(jdata.getData());
        System.out.println("count: " + ((List<?>) jdata.getData()).size());
    }

    public static void main(String[] args) {}
}
