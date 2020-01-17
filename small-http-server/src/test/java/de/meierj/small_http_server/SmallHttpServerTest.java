package de.meierj.small_http_server;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;

public class SmallHttpServerTest implements HttpTest {

	private SmallHttpServer smallHttpServer;

	@Override
	public String getBaseUrl() {
		return "http://localhost:1234";
	}

	@Before
	public void before() throws IOException {
		smallHttpServer = new SmallHttpServer(1234);
	}

	@After
	public void after() throws IOException {
		smallHttpServer.stop();
	}

	@Test
	public void testRequest() throws Exception {
		smallHttpServer.addHandler("/handler", exchange -> exchange.sendResponse("Response test"));

		assertGETResponse("/test", 404, "<h1>404 Not Found</h1>No context found for request");
		assertGETResponse("/handler", 200, "Response test");
	}

	@Test
	public void testRequestAuthenticator() throws Exception {

		smallHttpServer.addHandler("/public", exchange -> exchange.sendResponse("Response public"));

		Authenticator authenticator = new BasicAuthenticator("realm") {
			@Override
			public boolean checkCredentials(String username, String password) {
				return username.equals("user") && password.equals("pass");
			}
		};

		smallHttpServer.addHandler("/private", authenticator, exchange -> exchange.sendResponse("Response private"));

		assertGETResponse("/test", 404, "<h1>404 Not Found</h1>No context found for request");
		assertGETResponse("/public", 200, "Response public");
		assertGETResponse("/private", 401, "");
		assertGETResponse("/private", "user:falsche", 401, "");
		assertGETResponse("/private", "user:pass", 200, "Response private");
	}

	@Test
	public void testRequestParameter() throws Exception {
		smallHttpServer.addHandler("/ohneParameter", exchange -> exchange
				.sendResponse(Joiner.on(",").withKeyValueSeparator("=").join(exchange.getParameterMap())));
		assertGETResponse("/ohneParameter", 200, "");
		assertGETResponse("/ohneParameter?", 200, "");
		assertGETResponse("/ohneParameter?a", 200, "a=");
		assertGETResponse("/ohneParameter?a=test", 200, "a=test");
		assertGETResponse("/ohneParameter?a=hello&b=world", 200, "a=hello,b=world");
	}

}
