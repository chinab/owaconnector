package com.owaconnector.exchange.original;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.htmlcleaner.CommentToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.owaconnector.exception.DavMailAuthenticationException;
import com.owaconnector.exception.DavMailException;

public class FormLoginDelegate {

	protected static final Logger LOGGER = Logger
			.getLogger(ExchangeSession.class);

	protected static final Set<String> USER_NAME_FIELDS = new HashSet<String>();

	static {
		USER_NAME_FIELDS.add("username");
		USER_NAME_FIELDS.add("txtUserName");
		USER_NAME_FIELDS.add("userid");
		USER_NAME_FIELDS.add("SafeWordUser");
	}

	protected static final Set<String> PASSWORD_FIELDS = new HashSet<String>();

	static {
		PASSWORD_FIELDS.add("password");
		PASSWORD_FIELDS.add("txtUserPass");
		PASSWORD_FIELDS.add("pw");
		PASSWORD_FIELDS.add("basicPassword");
	}

	protected static final Set<String> TOKEN_FIELDS = new HashSet<String>();

	static {
		TOKEN_FIELDS.add("SafeWordPassword");
	}

	/**
	 * Logon form user name field, default is username.
	 */
	private String userNameInput = "username";
	/**
	 * Logon form password field, default is password.
	 */
	private String passwordInput = "password";

	private String userName;
	private String password;

	private final DavGatewayHttpClientFacade facade;

	public FormLoginDelegate(String userName, String password,
			DavGatewayHttpClientFacade facade) {
		super();
		this.userName = userName;
		this.password = password;
		this.facade = facade;
	}

	public HttpResponse doLogin(HttpUriRequest initmethod,
			HttpResponse initresponse) throws IOException, DavMailException {

		LOGGER.debug("Form based authentication detected");

		HttpPost logonMethod = null;
		try {
			logonMethod = buildLogonMethod(initmethod, initresponse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (logonMethod == null) {
			throw new DavMailException(
					"EXCEPTION_AUTHENTICATION_FORM_NOT_FOUND", initmethod
							.getURI());
		}

		HttpResponse response = facade.executeFollowRedirects(logonMethod);

		// test form based authentication
		checkFormLoginQueryString(logonMethod);

		// workaround for post logon script redirect
		if (!facade.isAuthenticated()) {
			// try to get new method from script based redirection
			try {
				logonMethod = buildLogonMethod(logonMethod, response);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (logonMethod != null) {
				// if logonMethod is not null, try to follow redirection
				checkFormLoginQueryString(logonMethod);
				// also check cookies
				if (!facade.isAuthenticated()) {
					throwAuthenticationFailed();
				}
			} else {
				// authentication failed
				throwAuthenticationFailed();
			}
		}

		return response;
	}

	/**
	 * Try to find logon method path from logon form body.
	 * 
	 * @param httpClient
	 *            httpClient instance
	 * @param initresponse
	 *            form body http method
	 * @return logon method
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 */
	private HttpPost buildLogonMethod(HttpUriRequest initmethod,
			HttpResponse initresponse) throws IOException, URISyntaxException {

		HttpPost logonMethod = null;

		// create an instance of HtmlCleaner
		HtmlCleaner cleaner = new HtmlCleaner();
		try {
			TagNode node = cleaner.clean(initresponse.getEntity().getContent());
			List<?> forms = node.getElementListByName("form", true);
			if (forms.size() == 1) {
				TagNode form = (TagNode) forms.get(0);
				String logonMethodPath = form.getAttributeByName("action");

				logonMethod = new HttpPost(getAbsoluteUri(initmethod,
						logonMethodPath));
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();

				List<?> inputList = form.getElementListByName("input", true);
				for (Object input : inputList) {
					String type = ((TagNode) input).getAttributeByName("type");
					String name = ((TagNode) input).getAttributeByName("name");
					String value = ((TagNode) input)
							.getAttributeByName("value");
					if ("hidden".equalsIgnoreCase(type) && name != null
							&& value != null) {
						formparams.add(new BasicNameValuePair(name, value));

					}
					// custom login form
					if (USER_NAME_FIELDS.contains(name)) {
						userNameInput = name;
					} else if (PASSWORD_FIELDS.contains(name)) {
						passwordInput = name;
					} else if ("addr".equals(name)) {
						// this is not a logon form but a redirect form
						HttpResponse newInitResponse = facade
								.executeFollowRedirects(logonMethod);
						logonMethod = buildLogonMethod(logonMethod,
								newInitResponse);
					} else if (TOKEN_FIELDS.contains(name)) {
						throw new IOException("Unsupported");
					}
				}
				formparams.add(new BasicNameValuePair(userNameInput, userName));
				formparams.add(new BasicNameValuePair(passwordInput, password));
				formparams.add(new BasicNameValuePair("trusted", "4"));
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						formparams, "UTF-8");
				logonMethod.setEntity(entity);
			} else {
				List<?> frameList = node.getElementListByName("frame", true);
				if (frameList.size() == 1) {
					String src = ((TagNode) frameList.get(0))
							.getAttributeByName("src");
					if (src != null) {
						LOGGER
								.debug("Frames detected in form page, try frame content");
						HttpRequestBase newInitMethod = new HttpGet(src);
						HttpClientParams.setRedirecting(newInitMethod
								.getParams(), true);
						HttpResponse newInitResponse = facade
								.executeFollowRedirects(newInitMethod);
						logonMethod = buildLogonMethod(newInitMethod,
								newInitResponse);
					}
				} else {
					// another failover for script based logon forms (Exchange
					// 2007)
					List<?> scriptList = node.getElementListByName("script",
							true);
					for (Object script : scriptList) {
						List<?> contents = ((TagNode) script).getChildren();
						for (Object content : contents) {
							if (content instanceof CommentToken) {
								String scriptValue = ((CommentToken) content)
										.getCommentedContent();
								String sUrl = StringUtil.getToken(scriptValue,
										"var a_sUrl = \"", "\"");
								String sLgn = StringUtil.getToken(scriptValue,
										"var a_sLgn = \"", "\"");
								if (sLgn == null) {
									sLgn = StringUtil.getToken(scriptValue,
											"var a_sLgnQS = \"", "\"");
								}
								if (sUrl != null && sLgn != null) {
									String src = getScriptBasedFormURL(
											initmethod, sLgn + sUrl);
									LOGGER
											.debug("Detected script based logon, redirect to form at "
													+ src);
									HttpRequestBase newInitMethod = new HttpGet(
											src);
									HttpClientParams.setRedirecting(
											newInitMethod.getParams(), false);
									HttpResponse response = facade
											.executeFollowRedirects(newInitMethod);
									logonMethod = buildLogonMethod(
											newInitMethod, response);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error parsing login form at " + initmethod.getURI());
		}

		return logonMethod;
	}

	private void checkFormLoginQueryString(HttpPost logonMethod)
			throws DavMailAuthenticationException {
		String queryString = logonMethod.getURI().getQuery();
		if (queryString != null && queryString.contains("reason=2")) {
			logonMethod.abort();
			throwAuthenticationFailed();
		}
	}

	private void throwAuthenticationFailed()
			throws DavMailAuthenticationException {
		if (this.userName != null && this.userName.contains("\\")) {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED");
		} else {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED_RETRY");
		}
	}

	private String getAbsoluteUri(HttpUriRequest method, String path)
			throws URISyntaxException {
		URI uri = method.getURI();
		String scheme = uri.getScheme();
		String authority = uri.getAuthority();
		String newPath = null;
		String fragment = uri.getFragment();
		if (path != null) {
			// reset query string
			// uri.setQuery(null);
			if (path.startsWith("/")) {
				// path is absolute, replace method path
				newPath = path;
			} else if (path.startsWith("http://")
					|| path.startsWith("https://")) {
				return path;
			} else {
				// relative path, build new path
				String currentPath = method.getURI().getPath();
				int end = currentPath.lastIndexOf('/');
				if (end >= 0) {
					newPath = currentPath.substring(0, end + 1) + path;
				}
			}
		}
		return new URI(scheme, authority, newPath, null, fragment).toString();
	}

	private String getScriptBasedFormURL(HttpUriRequest initmethod,
			String pathQuery) throws URISyntaxException {
		URI initmethodURI = initmethod.getURI();
		String scheme = initmethodURI.getScheme();
		String authority = initmethodURI.getAuthority();
		String path = null;
		String query = null;
		String fragment = initmethodURI.getFragment();
		int queryIndex = pathQuery.indexOf('?');
		if (queryIndex >= 0) {
			if (queryIndex > 0) {
				// update path
				String newPath = pathQuery.substring(0, queryIndex);
				if (newPath.startsWith("/")) {
					// absolute path
					path = newPath;
				} else {
					String currentPath = initmethodURI.getPath();
					int folderIndex = currentPath.lastIndexOf('/');
					if (folderIndex >= 0) {
						// replace relative path
						path = currentPath.substring(0, folderIndex + 1)
								+ newPath;
					} else {
						// should not happen
						path = '/' + newPath;
					}
				}
			}
			query = pathQuery.substring(queryIndex + 1);
		}
		return new URI(scheme, authority, path, query, fragment).toString();
		// initmethodURI.getURI();
	}

}
