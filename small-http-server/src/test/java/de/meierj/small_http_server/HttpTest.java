package de.meierj.small_http_server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public interface HttpTest {

	final CloseableHttpClient httpClient = HttpClients.createDefault();

	public default void assertGETResponse(String uri, String credentials, int expectedStatuscode,
			String expectedResponse) throws ClientProtocolException, IOException {

		HttpGet request = new HttpGet(getBaseUrl() + "/" + uri);

		if (credentials != null) {
			byte[] encodedAuth = Base64.encodeBase64(credentials.getBytes(StandardCharsets.ISO_8859_1));
			String authHeader = "Basic " + new String(encodedAuth);
			request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}

		CloseableHttpResponse httpResponse = httpClient.execute(request);
		assertThat(httpResponse.getStatusLine().getStatusCode(), is(expectedStatuscode));

		String result = EntityUtils.toString(httpResponse.getEntity());
		assertThat(result, is(expectedResponse));
	}

	public default void assertGETResponse(String uri, int expectedStatuscode, String expectedResponse)
			throws ClientProtocolException, IOException {
		assertGETResponse(uri, null, expectedStatuscode, expectedResponse);
	}

	public String getBaseUrl();
}
