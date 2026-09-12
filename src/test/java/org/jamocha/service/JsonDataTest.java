package org.jamocha.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import woolfel.examples.model.Account;
import static org.junit.jupiter.api.Assertions.*;

public class JsonDataTest {


	@Test
	public void testLoadJson() {
		JSONData<Object> jdata = new JSONData<>();
		jdata.setName("org.jamocha.examples.model.Account");
		jdata.setUrl("./samples/configuration/accounts.json");
		
		jdata.setData(jdata.loadJsonData(jdata.getUrl(), Account.class));
		
		assertNotNull(jdata.getData());
		System.out.println("count: " + ((List<?>)jdata.getData()).size());
	}
	
	public static void main(String[] args) {
	}

}
