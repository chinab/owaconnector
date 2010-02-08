package com.owaconnector.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

public class HttpClientFacade implements ClientFacade {

	private final static Logger LOG = Logger.getLogger(HttpClientFacade.class);
	/*
	 * User agent that this facade should use to communicate.
	 */
	private static final String IE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";

	/*
	 * HttpClient this facade should use.
	 */
	private DefaultHttpClient httpClient;

	private CredentialsProvider credsProvider;

	public HttpClientFacade() {

		// set Http parameters
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, IE_USER_AGENT);

		// Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		// create and set connection manager
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
		// Increase max total connection to 200
		cm.setMaxTotalConnections(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		this.httpClient = new DefaultHttpClient(cm, params);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.exchange.ClientFacade#getHttpStatus(java.lang.String)
	 */
	public int getHttpStatus(String url) {
		HttpGet method = new HttpGet(url);
		HttpClientParams.setAuthenticating(method.getParams(), false);
		HttpResponse response = execute(method);
		return response.getStatusLine().getStatusCode();
	}

	/**
	 * execute a HTTP request.
	 * 
	 * @param request
	 *            the request to execute.
	 * @return response obtained from the request.
	 */
	private HttpResponse execute(HttpRequestBase request) {
		HttpResponse response = null;
		try {
			response = this.httpClient.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.owaconnector.exchange.ClientFacade#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		boolean authenticated = false;

		for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
			// Exchange 2003 cookies
			if (cookie.getName().startsWith("cadata") || "sessionid".equals(cookie.getName())
			// Exchange 2007 cookie
					|| "UserContext".equals(cookie.getName())) {
				authenticated = true;
				break;
			}
		}
		return authenticated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.exchange.ClientFacade#executePropFindMethod(java.lang
	 * .String, int, org.apache.jackrabbit.webdav.property.DavPropertyNameSet)
	 */
	public MultiStatusResponse[] executePropFindMethod(String path, int depth,
			DavPropertyNameSet properties) throws IOException {
		Assert.notNull(path, "Path is required");
		Assert.notNull(depth, "depth is required");
		Assert.notNull(properties, "properties is required");

		PropFindMethod propFindMethod = new PropFindMethod(path, properties, depth);
		HttpResponse response = execute(propFindMethod);
		return DavResponseUtil.getResponseBodyAsMultiStatus(response).getResponses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.owaconnector.exchange.ClientFacade#executeGet(java.lang.String,
	 * boolean)
	 */
	public InputStream executeGet(String url, boolean redirecting) throws IOException {
		Assert.notNull(url, "URL is required");
		Assert.notNull(redirecting, "redirecting is required");

		LOG.debug("[executeGet] URL: " + url + " Redirecting: " + redirecting);

		HttpRequestBase currentMethod = new HttpGet(url);
		HttpClientParams.setRedirecting(currentMethod.getParams(), redirecting);
		HttpResponse response = execute(currentMethod);
		return response.getEntity().getContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.owaconnector.exchange.ClientFacade#executePost(java.lang.String,
	 * java.util.List)
	 */
	public InputStream executePost(String url, List<NameValuePair> formparams, boolean redirecting) {
		Assert.notNull(url, "URL is required");
		HttpPost currentMethod = new HttpPost(url);
		// HttpClientParams.setRedirecting(currentMethod.getParams(),
		// redirecting);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			currentMethod.setEntity(entity);
			HttpResponse response = execute(currentMethod);
			int statusCode = response.getStatusLine().getStatusCode();
			switch (statusCode) {
			case HttpStatus.SC_OK:
				return response.getEntity().getContent();
			case HttpStatus.SC_MOVED_TEMPORARILY:
				currentMethod.abort();
				return executeGet(response.getFirstHeader("Location").getValue(), false);
			case HttpStatus.SC_MOVED_PERMANENTLY:
				currentMethod.abort();
				return executeGet(response.getFirstHeader("Location").getValue(), false);
			default:
				throw new IllegalStateException("[executePost] invalid status:" + statusCode);
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public void setAuthentication(String username, String password) {
		credsProvider = new BasicCredentialsProvider();
		AuthScope authScope = new AuthScope(null, -1);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username,
				password);
		credsProvider.setCredentials(authScope, credentials);
	}

	public void removeAuthentication() {
		if (credsProvider != null) {
			credsProvider.clear();
		}
	}
}
