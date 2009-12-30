/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2009  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package davmail.http;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethodBase;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.log4j.Logger;

import davmail.exception.DavMailException;
import davmail.exception.HttpForbiddenException;
import davmail.exception.HttpNotFoundException;
import davmail.util.DavResponseUtil;

/**
 * Create HttpClient instance according to DavGateway Settings
 */
public final class DavGatewayHttpClientFacade {
	static final Logger LOGGER = Logger
			.getLogger("davmail.http.DavGatewayHttpClientFacade");

	static final String IE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";
	static final int MAX_REDIRECTS = 10;
	static final Object LOCK = new Object();

	static final long ONE_MINUTE = 60000;

	private DavGatewayHttpClientFacade() {
	}

	/**
	 * Create a configured HttpClient instance.
	 * 
	 * @return httpClient
	 */
	public static DefaultHttpClient getInstance() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, IE_USER_AGENT);

		// Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);

		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);

		return httpClient;
	}

	/**
	 * Build an HttpClient instance for the provided url and credentials.
	 * 
	 * @param url
	 *            http(s) url
	 * @param userName
	 *            user name
	 * @param password
	 *            user password
	 * @return HttpClient instance
	 * @throws DavMailException
	 *             on error
	 */
	public static DefaultHttpClient getInstance(String url, String userName,
			String password) throws DavMailException {
		DefaultHttpClient httpClient = getInstance();
		CredentialsProvider credsProvider = new BasicCredentialsProvider();

		AuthScope authScope = new AuthScope(null, -1);
		credsProvider.setCredentials(authScope,
				new UsernamePasswordCredentials(userName, password));

		return httpClient;
	}

	/**
	 * Get Http Status code for the given URL
	 * 
	 * @param url
	 *            url string
	 * @return HttpStatus code
	 * @throws IOException
	 *             on error
	 */
	public static int getHttpStatus(String url) throws IOException {
		int status = 0;
		DefaultHttpClient httpClient = DavGatewayHttpClientFacade.getInstance();
		HttpGet testMethod = new HttpGet(url);
		HttpClientParams.setAuthenticating(testMethod.getParams(), false);
		try {
			HttpResponse response = httpClient.execute(testMethod);
			status = response.getStatusLine().getStatusCode();
		} finally {
			testMethod.abort();
		}
		return status;
	}

	/**
	 * Check if status is a redirect (various 30x values).
	 * 
	 * @param status
	 *            Http status
	 * @return true if status is a redirect
	 */
	public static boolean isRedirect(int status) {
		return status == HttpStatus.SC_MOVED_PERMANENTLY
				|| status == HttpStatus.SC_MOVED_TEMPORARILY
				|| status == HttpStatus.SC_SEE_OTHER
				|| status == HttpStatus.SC_TEMPORARY_REDIRECT;
	}

	/**
	 * Execute given url, manually follow redirects. Workaround for HttpClient
	 * bug (GET full URL over HTTPS and proxy)
	 * 
	 * @param httpClient
	 *            HttpClient instance
	 * @param url
	 *            url string
	 * @return executed method
	 * @throws IOException
	 *             on error
	 * @deprecated
	 */
	public static HttpResponse executeFollowRedirects(
			DefaultHttpClient httpClient, String url) throws IOException {
		HttpRequestBase method = new HttpGet(url);
		HttpClientParams.setRedirecting(method.getParams(), false);
		return executeFollowRedirects(httpClient, method);
	}

	/**
	 * Execute method with httpClient, follow 30x redirects.
	 * 
	 * @param httpClient
	 *            Http client instance
	 * @param method
	 *            Http method
	 * @return last http method after redirects
	 * @throws IOException
	 *             on error
	 */
	public static HttpResponse executeFollowRedirects(
			DefaultHttpClient httpClient, HttpRequestBase method)
			throws IOException {
		HttpRequestBase currentMethod = method;
		HttpResponse response;
		try {
			response = httpClient.execute(currentMethod);
			Header location = response.getFirstHeader("Location");
			int redirectCount = 0;
			while (redirectCount++ < 10 && location != null
					&& isRedirect(response.getStatusLine().getStatusCode())) {
				currentMethod.abort();
				currentMethod = new HttpGet(location.getValue());
				HttpClientParams.setRedirecting(currentMethod.getParams(),
						false);
				response = httpClient.execute(currentMethod);
				location = response.getFirstHeader("Location");
			}
			if (location != null
					&& isRedirect(response.getStatusLine().getStatusCode())) {
				currentMethod.abort();
				throw new IOException("Maximum redirections reached");
			}
		} catch (IOException e) {
			currentMethod.abort();
			throw e;
		}
		// caller will need to release connection
		return response;
	}

	/**
	 * Execute webdav search method.
	 * 
	 * @param httpClient
	 *            http client instance
	 * @param path
	 *            <i>encoded</i> searched folder path
	 * @param searchRequest
	 *            (SQL like) search request
	 * @return Responses enumeration
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public static MultiStatusResponse[] executeSearchMethod(
			HttpClient httpClient, String path, String searchRequest)
			throws IOException, HttpException {
		String searchBody = "<?xml version=\"1.0\"?>\n"
				+ "<d:searchrequest xmlns:d=\"DAV:\">\n"
				+ "        <d:sql>"
				+ searchRequest.replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
				+ "</d:sql>\n" + "</d:searchrequest>";
		DavMethodBase searchMethod = new DavMethodBase(path) {

			@Override
			public String getName() {
				return "SEARCH";
			}

			@Override
			public String getMethod() {
				return getName();
			}
		};
		StringEntity entity = new StringEntity(searchBody, "UTF-8");
		entity.setContentType("text/xml");
		searchMethod.setEntity(entity);
		return executeMethod(httpClient, searchMethod);
	}

	/**
	 * Execute webdav propfind method.
	 * 
	 * @param httpClient
	 *            http client instance
	 * @param path
	 *            <i>encoded</i> searched folder path
	 * @param depth
	 *            propfind request depth
	 * @param properties
	 *            propfind requested properties
	 * @return Responses enumeration
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public static MultiStatusResponse[] executePropFindMethod(
			HttpClient httpClient, String path, int depth,
			DavPropertyNameSet properties) throws IOException, HttpException {
		PropFindMethod propFindMethod = new PropFindMethod(path, properties,
				depth);
		return executeMethod(httpClient, propFindMethod);
	}

	/**
	 * Execute a delete method on the given path with httpClient.
	 * 
	 * @param httpClient
	 *            Http client instance
	 * @param path
	 *            Path to be deleted
	 * @return Http status
	 * @throws IOException
	 *             on error
	 */
	public static int executeDeleteMethod(HttpClient httpClient, String path)
			throws IOException {
		DeleteMethod deleteMethod = new DeleteMethod(path);
		deleteMethod.setFollowRedirects(false);

		HttpResponse response = executeHttpMethod(httpClient, deleteMethod);
		int status = response.getStatusLine().getStatusCode();
		// do not throw error if already deleted
		if (status != HttpStatus.SC_OK && status != HttpStatus.SC_NOT_FOUND) {
			try {
				throw DavGatewayHttpClientFacade.buildHttpException(
						deleteMethod, response);
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return HttpStatus.SC_OK;
	}

	/**
	 * Execute webdav request.
	 * 
	 * @param httpClient
	 *            http client instance
	 * @param method
	 *            webdav method
	 * @return Responses enumeration
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public static MultiStatusResponse[] executeMethod(HttpClient httpClient,
			DavMethodBase method) throws IOException, HttpException {
		MultiStatusResponse[] responses = null;
		try {
			HttpResponse response = httpClient.execute(method);
			int status = response.getStatusLine().getStatusCode();

			if (status != HttpStatus.SC_MULTI_STATUS) {
				throw buildHttpException(method, response);
			}
			responses = DavResponseUtil.getResponseBodyAsMultiStatus(response)
					.getResponses();
		} catch (DavException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			method.abort();
		}
		return responses;
	}

	/**
	 * Execute method with httpClient.
	 * 
	 * @param httpClient
	 *            Http client instance
	 * @param method
	 *            Http method
	 * @return Http status
	 * @throws IOException
	 *             on error
	 */
	public static HttpResponse executeHttpMethod(HttpClient httpClient,
			HttpUriRequest method) throws IOException {

		return httpClient.execute(method);

	}
	// TODO Is this correct?
	private static boolean hasNTLM(HttpClient httpClient) {
		// Object authPrefs = httpClient.getParams().getParameter(
		// AuthPolicy.AUTH_SCHEME_PRIORITY);
		// return authPrefs instanceof List<?>
		// && ((Collection) authPrefs).contains(AuthPolicy.NTLM);
		return true;
	}

	// private static void addNTLM(DefaultHttpClient httpClient) {
	// ArrayList<String> authPrefs = new ArrayList<String>();
	// authPrefs.add(AuthPolicy.NTLM);
	// authPrefs.add(AuthPolicy.DIGEST);
	// authPrefs.add(AuthPolicy.BASIC);
	// httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY,
	// authPrefs);
	// }

	/**
	 * Execute Get method, do not follow redirects.
	 * 
	 * @param httpClient
	 *            Http client instance
	 * @param method
	 *            Http method
	 * @param followRedirects
	 *            Follow redirects flag
	 * @throws IOException
	 *             on error
	 * @throws DavMailException
	 */
	public static HttpResponse executeGetMethod(HttpClient httpClient,
			HttpGet method, boolean followRedirects) throws IOException,
			DavMailException {
		// do not follow redirects in expired sessions
		HttpClientParams.setRedirecting(method.getParams(), followRedirects);
		HttpResponse response = httpClient.execute(method);
		int status = response.getStatusLine().getStatusCode();
		if (status == HttpStatus.SC_UNAUTHORIZED & !hasNTLM(httpClient)) {
			method.abort();
			LOGGER.debug("Received unauthorized at " + method.getURI()
					+ ", retrying with NTLM");
			// addNTLM(httpClient);
			response = httpClient.execute(method);
			status = response.getStatusLine().getStatusCode();
		}
		if (status != HttpStatus.SC_OK) {
			LOGGER.warn("GET failed with status " + status + " at "
					+ method.getURI() + ": ");
			// + method.getResponseBodyAsString());
			throw new DavMailException("EXCEPTION_GET_FAILED", status, method
					.getURI());
		}
		// check for expired session
		if (followRedirects) {
			String queryString = method.getURI().getQuery();
			if (queryString != null && queryString.contains("reason=2")) {
				LOGGER.warn("GET failed, session expired  at "
						+ method.getURI() + ": ");
				// + method.getResponseBodyAsString());
				throw new DavMailException("EXCEPTION_GET_FAILED", status,
						method.getURI());
			}
		}
		return response;
	}

	/**
	 * Build Http Exception from methode status
	 * 
	 * @param method
	 *            Http Method
	 * @return Http Exception
	 */
	public static HttpException buildHttpException(HttpUriRequest method,
			HttpResponse response) {
		int status = response.getStatusLine().getStatusCode();
		// 440 means forbidden on Exchange
		if (status == 440) {
			status = HttpStatus.SC_FORBIDDEN;
		}
		StringBuilder message = new StringBuilder();
		message.append(status).append(' ').append(
				response.getStatusLine().getReasonPhrase());
		message.append(" at ").append(method.getURI());
		if (status == HttpStatus.SC_FORBIDDEN) {
			return new HttpForbiddenException(message.toString());
		} else if (status == HttpStatus.SC_NOT_FOUND) {
			return new HttpNotFoundException(message.toString());
		} else {
			return new HttpException(message.toString());
		}
	}
}
