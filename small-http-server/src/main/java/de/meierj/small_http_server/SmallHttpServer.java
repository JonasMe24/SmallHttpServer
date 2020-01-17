package de.meierj.small_http_server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Strings;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class SmallHttpServer {

	private HttpServer httpServer;

	public SmallHttpServer(int port) throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(port), 0);
		httpServer.start();
	}

	public void addHandler(String path, Authenticator authenticator, RequestHandler handler,
			String... allowedhttpMethods) {
		HttpContext context = httpServer.createContext(path, exchange -> {
			if (allowedhttpMethods.length == 0
					|| Arrays.asList(allowedhttpMethods).contains(exchange.getRequestMethod())) {
				handler.handle(new ExchangeUtil(exchange));
			} else {
				exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
			}
			exchange.close();
		});

		if (authenticator != null) {
			context.setAuthenticator(authenticator);
		}
	}

	public void addHandler(String path, RequestHandler handler, String... allowedhttpMethods) {
		addHandler(path, null, handler, allowedhttpMethods);
	}

	public void stop() {
		httpServer.stop(0);
	}

	@FunctionalInterface
	public interface RequestHandler {

		public abstract void handle(ExchangeUtil exchange) throws IOException;

	}

	public class ExchangeUtil {
		private HttpExchange exchange;
		private Optional<Map<String, String>> parameterMap = Optional.empty();

		public ExchangeUtil(HttpExchange exchange) {
			this.exchange = exchange;
		}

		public void sendResponse(String responseMessage) throws IOException {
			exchange.sendResponseHeaders(200, responseMessage.length());
			OutputStream output = exchange.getResponseBody();
			output.write(responseMessage.getBytes());
			output.flush();
		}

		public Map<String, String> getParameterMap() {
			return parameterMap.orElseGet(() -> {
				String query = exchange.getRequestURI().getRawQuery();
				if (Strings.isNullOrEmpty(query)) {
					return Collections.emptyMap();
				}

				return queryToMap(query);
			});
		}

		private Map<String, String> queryToMap(String query) {
			Map<String, String> result = new HashMap<>();
			for (String param : query.split("&")) {
				String[] entry = param.split("=");
				if (entry.length > 1) {
					result.put(entry[0], entry[1]);
				} else {
					result.put(entry[0], "");
				}
			}
			return result;
		}

		public String getParameter(String name) {
			return getParameterMap().get(name);
		}
	}

}
