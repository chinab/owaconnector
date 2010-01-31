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
package davmail.exchange;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Stack;
import java.util.TimeZone;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.print.URIException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.log4j.Logger;
import org.htmlcleaner.CommentToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.mortbay.util.URIUtil;

import davmail.BundleMessage;
import davmail.exception.DavMailAuthenticationException;
import davmail.exception.DavMailException;
import davmail.http.DavGatewayHttpClientFacade;
import davmail.util.StringUtil;

/**
 * Exchange session through Outlook Web Access (DAV)
 */
public class ExchangeSession {
	protected static final Logger LOGGER = Logger
			.getLogger("davmail.exchange.ExchangeSession");

	/**
	 * Reference GMT timezone to format dates
	 */
	public static final SimpleTimeZone GMT_TIMEZONE = new SimpleTimeZone(0,
			"GMT");

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

	protected static final int FREE_BUSY_INTERVAL = 15;

	protected static final Namespace URN_SCHEMAS_HTTPMAIL = Namespace
			.getNamespace("urn:schemas:httpmail:");
	protected static final Namespace SCHEMAS_EXCHANGE = Namespace
			.getNamespace("http://schemas.microsoft.com/exchange/");
	protected static final Namespace SCHEMAS_MAPI_PROPTAG = Namespace
			.getNamespace("http://schemas.microsoft.com/mapi/proptag/");

	protected static final DavPropertyNameSet EVENT_REQUEST_PROPERTIES = new DavPropertyNameSet();

	static {
		EVENT_REQUEST_PROPERTIES.add(DavPropertyName.create("permanenturl",
				SCHEMAS_EXCHANGE));
		EVENT_REQUEST_PROPERTIES.add(DavPropertyName.GETETAG);
	}

	protected static final DavPropertyNameSet WELL_KNOWN_FOLDERS = new DavPropertyNameSet();

	static {
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("inbox",
				URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("deleteditems",
				URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("sentitems",
				URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("sendmsg",
				URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("drafts",
				URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("calendar",
				URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("contacts",
				URN_SCHEMAS_HTTPMAIL));
	}

	protected static final DavPropertyNameSet DISPLAY_NAME = new DavPropertyNameSet();

	static {
		DISPLAY_NAME.add(DavPropertyName.DISPLAYNAME);
	}

	protected static final DavPropertyNameSet FOLDER_PROPERTIES = new DavPropertyNameSet();

	static {
		FOLDER_PROPERTIES.add(DavPropertyName.create("hassubs"));
		FOLDER_PROPERTIES.add(DavPropertyName.create("nosubs"));
		FOLDER_PROPERTIES.add(DavPropertyName.create("unreadcount",
				URN_SCHEMAS_HTTPMAIL));
		FOLDER_PROPERTIES.add(DavPropertyName.create("contenttag", Namespace
				.getNamespace("http://schemas.microsoft.com/repl/")));
	}

	protected static final DavPropertyNameSet CONTENT_TAG = new DavPropertyNameSet();

	static {
		CONTENT_TAG.add(DavPropertyName.create("contenttag", Namespace
				.getNamespace("http://schemas.microsoft.com/repl/")));
	}

	protected static final DavPropertyNameSet RESOURCE_TAG = new DavPropertyNameSet();

	static {
		RESOURCE_TAG.add(DavPropertyName.create("resourcetag", Namespace
				.getNamespace("http://schemas.microsoft.com/repl/")));
	}

	protected static final DavPropertyName DEFAULT_SCHEDULE_STATE_PROPERTY = DavPropertyName
			.create("schedule-state", Namespace.getNamespace("CALDAV:"));
	protected DavPropertyName scheduleStateProperty = DEFAULT_SCHEDULE_STATE_PROPERTY;

	/**
	 * Various standard mail boxes Urls
	 */
	private String inboxUrl;
	private String deleteditemsUrl;
	private String sentitemsUrl;
	private String sendmsgUrl;
	private String draftsUrl;
	private String calendarUrl;
	private String contactsUrl;

	/**
	 * Base user mailboxes path (used to select folder)
	 */
	private String mailPath;
	private String email;
	private String alias;
	private final DefaultHttpClient httpClient;

	private final String userName;
	private final String password;

	private boolean disableGalLookup;
	private static final String YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
	private static final String YYYYMMDD_T_HHMMSS_Z = "yyyyMMdd'T'HHmmss'Z'";
	private static final String YYYY_MM_DD_T_HHMMSS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String YYYY_MM_DD_T_HHMMSS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/**
	 * Logon form user name field, default is username.
	 */
	private String userNameInput = "username";
	/**
	 * Logon form password field, default is password.
	 */
	private String passwordInput = "password";

	private String hostName;

	private String authScheme;

	/**
	 * Create an exchange session for the given URL. The session is established
	 * for given userName and password
	 * 
	 * @param url
	 *            Exchange url
	 * @param userName
	 *            user login name
	 * @param password
	 *            user password
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	ExchangeSession(String url, String userName, String password)
			throws IOException, HttpException, URISyntaxException {
		this.userName = userName;
		this.password = password;
		try {
			boolean isBasicAuthentication = isBasicAuthentication(url);

			httpClient = DavGatewayHttpClientFacade.getInstance(url, userName,
					password);

			// get webmail root url
			// providing credentials
			// manually follow redirect

			HttpGet method = new HttpGet(url);
			URI uri = method.getURI();
			this.hostName = uri.getHost();
			this.authScheme = uri.getScheme();
			HttpPost postMethod = null;
			HttpClientParams.setRedirecting(method.getParams(), false);
			HttpResponse response = DavGatewayHttpClientFacade
					.executeFollowRedirects(httpClient, method);

			if (isBasicAuthentication) {
				int status = response.getStatusLine().getStatusCode();

				if (status == HttpStatus.SC_UNAUTHORIZED) {
					// response.abort();
					throw new DavMailAuthenticationException(
							"EXCEPTION_AUTHENTICATION_FAILED");
				} else if (status != HttpStatus.SC_OK) {
					// response.abort();
					throw DavGatewayHttpClientFacade.buildHttpException(null,
							response);
				}
			} else {
				response = formLogin(httpClient, method, response, userName,
						password);
			}

			// avoid 401 roundtrips
			// httpClient.getParams().setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION,
			// true);

			buildMailPath(postMethod, response);

			// got base http mailbox http url
			getWellKnownFolders();

		} catch (DavMailAuthenticationException exc) {
			LOGGER.error(exc.getLogMessage());
			throw exc;
		} catch (UnknownHostException exc) {
			BundleMessage message = new BundleMessage("EXCEPTION_CONNECT", exc
					.getClass().getName(), exc.getMessage());
			ExchangeSession.LOGGER.error(message);
			throw new DavMailException("EXCEPTION_DAVMAIL_CONFIGURATION",
					message);
		} catch (IOException exc) {
			LOGGER.error(BundleMessage.formatLog(
					"EXCEPTION_EXCHANGE_LOGIN_FAILED", exc));
			throw new DavMailException("EXCEPTION_EXCHANGE_LOGIN_FAILED", exc);
		}
		LOGGER.debug("Session " + this + " created");
	}

	protected String formatSearchDate(Date date) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH);
		dateFormatter.setTimeZone(GMT_TIMEZONE);
		return dateFormatter.format(date);
	}

	protected SimpleDateFormat getZuluDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD_T_HHMMSS_Z,
				Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	protected SimpleDateFormat getExchangeZuluDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				YYYY_MM_DD_T_HHMMSS_Z, Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	protected SimpleDateFormat getExchangeZuluDateFormatMillisecond() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				YYYY_MM_DD_T_HHMMSS_SSS_Z, Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	protected Date parseDate(String dateString) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat.parse(dateString);
	}

	/**
	 * Test if the session expired.
	 * 
	 * @return true this session expired
	 * @throws NoRouteToHostException
	 *             on error
	 * @throws UnknownHostException
	 *             on error
	 * @throws HttpException
	 */
	public boolean isExpired() throws NoRouteToHostException,
			UnknownHostException, HttpException {
		boolean isExpired = false;
		try {
			DavGatewayHttpClientFacade.executePropFindMethod(httpClient,
					URIUtil.encodePath(inboxUrl), 0, DISPLAY_NAME);
		} catch (UnknownHostException exc) {
			throw exc;
		} catch (NoRouteToHostException exc) {
			throw exc;
		} catch (IOException e) {
			isExpired = true;
		}

		return isExpired;
	}

	/**
	 * Test authentication mode : form based or basic.
	 * 
	 * @param url
	 *            exchange base URL
	 * @return true if basic authentication detected
	 * @throws IOException
	 *             unable to connect to exchange
	 */
	protected boolean isBasicAuthentication(String url) throws IOException {
		return DavGatewayHttpClientFacade.getHttpStatus(url) == HttpStatus.SC_UNAUTHORIZED;
	}

	protected String getAbsoluteUri(HttpUriRequest method, String path)
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

	protected String getScriptBasedFormURL(HttpUriRequest initmethod,
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
	protected HttpPost buildLogonMethod(DefaultHttpClient httpClient,
			HttpUriRequest initmethod, HttpResponse initresponse)
			throws IOException, URISyntaxException {

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
						HttpResponse newInitResponse = DavGatewayHttpClientFacade
								.executeFollowRedirects(httpClient, logonMethod);
						logonMethod = buildLogonMethod(httpClient, logonMethod,
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
						HttpResponse newInitResponse = DavGatewayHttpClientFacade
								.executeFollowRedirects(httpClient,
										newInitMethod);
						logonMethod = buildLogonMethod(httpClient,
								newInitMethod, newInitResponse);
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
									HttpResponse response = DavGatewayHttpClientFacade
											.executeFollowRedirects(httpClient,
													newInitMethod);
									logonMethod = buildLogonMethod(httpClient,
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

	protected HttpResponse formLogin(DefaultHttpClient httpClient,
			HttpUriRequest initmethod, HttpResponse initresponse,
			String userName, String password) throws IOException,
			DavMailException {
		LOGGER.debug("Form based authentication detected");

		HttpPost logonMethod = null;
		try {
			logonMethod = buildLogonMethod(httpClient, initmethod, initresponse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (logonMethod == null) {
			throw new DavMailException(
					"EXCEPTION_AUTHENTICATION_FORM_NOT_FOUND", initmethod
							.getURI());
		}

		HttpResponse response = DavGatewayHttpClientFacade
				.executeFollowRedirects(httpClient, logonMethod);

		// test form based authentication
		checkFormLoginQueryString(logonMethod);

		// workaround for post logon script redirect
		if (!isAuthenticated()) {
			// try to get new method from script based redirection
			try {
				logonMethod = buildLogonMethod(httpClient, logonMethod,
						response);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (logonMethod != null) {
				// if logonMethod is not null, try to follow redirection
				checkFormLoginQueryString(logonMethod);
				// also check cookies
				if (!isAuthenticated()) {
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
	 * Look for session cookies.
	 * 
	 * @return true if session cookies are available
	 */
	protected boolean isAuthenticated() {
		boolean authenticated = false;

		for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
			// Exchange 2003 cookies
			if (cookie.getName().startsWith("cadata")
					|| "sessionid".equals(cookie.getName())
					// Exchange 2007 cookie
					|| "UserContext".equals(cookie.getName())) {
				authenticated = true;
				break;
			}
		}
		return authenticated;
	}

	protected void checkFormLoginQueryString(HttpPost logonMethod)
			throws DavMailAuthenticationException {
		String queryString = logonMethod.getURI().getQuery();
		if (queryString != null && queryString.contains("reason=2")) {
			logonMethod.abort();
			throwAuthenticationFailed();
		}
	}

	protected void throwAuthenticationFailed()
			throws DavMailAuthenticationException {
		if (this.userName != null && this.userName.contains("\\")) {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED");
		} else {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED_RETRY");
		}
	}

	static final String BASE_HREF = "<base href=\"";

	protected void buildMailPath(HttpUriRequest method, HttpResponse response)
			throws HttpException {
		// find base url
		String line;

		// get user mail URL from html body (multi frame)
		BufferedReader mainPageReader = null;
		try {
			mainPageReader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			// noinspection StatementWithEmptyBody
			while ((line = mainPageReader.readLine()) != null
					&& line.toLowerCase().indexOf(BASE_HREF) == -1) {
			}
			if (line != null) {
				int start = line.toLowerCase().indexOf(BASE_HREF)
						+ BASE_HREF.length();
				int end = line.indexOf('\"', start);
				String mailBoxBaseHref = line.substring(start, end);
				URL baseURL = new URL(mailBoxBaseHref);
				mailPath = baseURL.getPath();
				LOGGER
						.debug("Base href found in body, mailPath is "
								+ mailPath);
				buildEmail(baseURL.getHost(), baseURL.getPath());
				LOGGER.debug("Current user email is " + email);
			} else {
				// failover for Exchange 2007 : build standard mailbox link with
				// email
				buildEmail(method.getURI().getHost(), method.getURI().getPath());
				mailPath = "/exchange/" + email + '/';
				LOGGER.debug("Current user email is " + email
						+ ", mailPath is " + mailPath);
			}
		} catch (IOException e) {
			LOGGER.error("Error parsing main page at "
					+ method.getURI().getPath(), e);
		} finally {
			if (mainPageReader != null) {
				try {
					mainPageReader.close();
				} catch (IOException e) {
					LOGGER.error("Error parsing main page at "
							+ method.getURI().getPath());
				}
			}
		}

		if (mailPath == null || email == null) {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED_PASSWORD_EXPIRED");
		}
	}

	protected String getPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return null;
		} else {
			return (String) property.getValue();
		}
	}

	protected String getPropertyIfExists(DavPropertySet properties,
			DavPropertyName davPropertyName) {
		DavProperty property = properties.get(davPropertyName);
		if (property == null) {
			return null;
		} else {
			return (String) property.getValue();
		}
	}

	protected int getIntPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return 0;
		} else {
			return Integer.parseInt((String) property.getValue());
		}
	}

	protected long getLongPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return 0;
		} else {
			return Long.parseLong((String) property.getValue());
		}
	}

	protected String getURIPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) throws URISyntaxException {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return null;
		} else {
			return URIUtil.decodePath((String) property.getValue());
		}
	}

	protected void getWellKnownFolders() throws HttpException,
			URISyntaxException {
		// Retrieve well known URLs
		MultiStatusResponse[] responses = null;
		try {
			responses = DavGatewayHttpClientFacade.executePropFindMethod(
					httpClient, getBaseUrl() + URIUtil.encodePath(mailPath), 0,
					WELL_KNOWN_FOLDERS);
			if (responses == null || responses.length == 0) {
				throw new DavMailException(
						"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", mailPath);
			}
			DavPropertySet properties = responses[0]
					.getProperties(HttpStatus.SC_OK);
			inboxUrl = getURIPropertyIfExists(properties, "inbox",
					URN_SCHEMAS_HTTPMAIL);
			deleteditemsUrl = getURIPropertyIfExists(properties,
					"deleteditems", URN_SCHEMAS_HTTPMAIL);
			sentitemsUrl = getURIPropertyIfExists(properties, "sentitems",
					URN_SCHEMAS_HTTPMAIL);
			sendmsgUrl = getURIPropertyIfExists(properties, "sendmsg",
					URN_SCHEMAS_HTTPMAIL);
			draftsUrl = getURIPropertyIfExists(properties, "drafts",
					URN_SCHEMAS_HTTPMAIL);
			calendarUrl = getURIPropertyIfExists(properties, "calendar",
					URN_SCHEMAS_HTTPMAIL);
			contactsUrl = getURIPropertyIfExists(properties, "contacts",
					URN_SCHEMAS_HTTPMAIL);
			LOGGER.debug("Inbox URL : " + inboxUrl + " Trash URL : "
					+ deleteditemsUrl + " Sent URL : " + sentitemsUrl
					+ " Send URL : " + sendmsgUrl + " Drafts URL : "
					+ draftsUrl + " Calendar URL : " + calendarUrl
					+ " Contacts URL : " + contactsUrl);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new DavMailAuthenticationException(
					"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", mailPath);
		}
	}

	protected List<DavProperty> buildProperties(Map<String, String> properties) {
		ArrayList<DavProperty> list = new ArrayList<DavProperty>();
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if ("read".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create("read",
						URN_SCHEMAS_HTTPMAIL), entry.getValue()));
			} else if ("junk".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create(
						"x10830003", SCHEMAS_MAPI_PROPTAG), entry.getValue()));
			} else if ("flagged".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create(
						"x10900003", SCHEMAS_MAPI_PROPTAG), entry.getValue()));
			} else if ("answered".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create(
						"x10810003", SCHEMAS_MAPI_PROPTAG), entry.getValue()));
				if ("102".equals(entry.getValue())) {
					list.add(new DefaultDavProperty(DavPropertyName.create(
							"x10800003", SCHEMAS_MAPI_PROPTAG), "261"));
				}
			} else if ("forwarded".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create(
						"x10810003", SCHEMAS_MAPI_PROPTAG), entry.getValue()));
				if ("104".equals(entry.getValue())) {
					list.add(new DefaultDavProperty(DavPropertyName.create(
							"x10800003", SCHEMAS_MAPI_PROPTAG), "262"));
				}
			} else if ("bcc".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create("bcc",
						Namespace.getNamespace("urn:schemas:mailheader:")),
						entry.getValue()));
			} else if ("draft".equals(entry.getKey())) {
				list.add(new DefaultDavProperty(DavPropertyName.create(
						"x0E070003", SCHEMAS_MAPI_PROPTAG), entry.getValue()));
			} else if ("deleted".equals(entry.getKey())) {
				list
						.add(new DefaultDavProperty(
								DavPropertyName
										.create(
												"_x0030_x8570",
												Namespace
														.getNamespace("http://schemas.microsoft.com/mapi/id/{00062008-0000-0000-C000-000000000046}/")),
								entry.getValue()));
			} else if ("datereceived".equals(entry.getKey())) {
				list
						.add(new DefaultDavProperty(DavPropertyName.create(
								"datereceived", URN_SCHEMAS_HTTPMAIL), entry
								.getValue()));
			}
		}
		return list;
	}

	/**
	 * Search folders under given folder.
	 * 
	 * @param folderName
	 *            Exchange folder name
	 * @param recursive
	 *            deep search if true
	 * @return list of folders
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public List<Folder> getSubFolders(String folderName, boolean recursive)
			throws IOException, HttpException {
		return getSubFolders(
				folderName,
				"(\"DAV:contentclass\"='urn:content-classes:mailfolder' OR \"DAV:contentclass\"='urn:content-classes:folder')",
				recursive);
	}

	/**
	 * Search calendar folders under given folder.
	 * 
	 * @param folderName
	 *            Exchange folder name
	 * @param recursive
	 *            deep search if true
	 * @return list of folders
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public List<Folder> getSubCalendarFolders(String folderName,
			boolean recursive) throws IOException, HttpException {
		return getSubFolders(folderName,
				"\"DAV:contentclass\"='urn:content-classes:calendarfolder'",
				recursive);
	}

	/**
	 * Search folders under given folder matching filter.
	 * 
	 * @param folderName
	 *            Exchange folder name
	 * @param filter
	 *            search filter
	 * @param recursive
	 *            deep search if true
	 * @return list of folders
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public List<Folder> getSubFolders(String folderName, String filter,
			boolean recursive) throws IOException, HttpException {
		String mode = recursive ? "DEEP" : "SHALLOW";
		List<Folder> folders = new ArrayList<Folder>();
		StringBuilder searchRequest = new StringBuilder();
		searchRequest
				.append(
						"Select \"DAV:nosubs\", \"DAV:hassubs\", \"DAV:hassubs\","
								+ "\"urn:schemas:httpmail:unreadcount\" FROM Scope('")
				.append(mode)
				.append(" TRAVERSAL OF \"")
				.append(getFolderPath(folderName))
				.append(
						"\"')\n"
								+ " WHERE \"DAV:ishidden\" = False AND \"DAV:isfolder\" = True \n");
		if (filter != null && filter.length() > 0) {
			searchRequest.append("                      AND ").append(filter);
		}
		MultiStatusResponse[] responses = DavGatewayHttpClientFacade
				.executeSearchMethod(httpClient, URIUtil
						.encodePath(getFolderPath(folderName)), searchRequest
						.toString());

		for (MultiStatusResponse response : responses) {
			folders.add(buildFolder(response));
		}
		return folders;
	}

	protected Folder buildFolder(MultiStatusResponse entity)
			throws IOException, DavMailException {
		String href = URIUtil.decodePath(entity.getHref());
		Folder folder = new Folder();
		DavPropertySet properties = entity.getProperties(HttpStatus.SC_OK);
		folder.hasChildren = "1".equals(getPropertyIfExists(properties,
				"hassubs", Namespace.getNamespace("DAV:")));
		folder.noInferiors = "1".equals(getPropertyIfExists(properties,
				"nosubs", Namespace.getNamespace("DAV:")));
		folder.unreadCount = getIntPropertyIfExists(properties, "unreadcount",
				URN_SCHEMAS_HTTPMAIL);
		folder.contenttag = getPropertyIfExists(properties, "contenttag",
				Namespace.getNamespace("http://schemas.microsoft.com/repl/"));

		// replace well known folder names
		if (href.startsWith(inboxUrl)) {
			folder.folderPath = href.replaceFirst(inboxUrl, "INBOX");
		} else if (href.startsWith(sentitemsUrl)) {
			folder.folderPath = href.replaceFirst(sentitemsUrl, "Sent");
		} else if (href.startsWith(draftsUrl)) {
			folder.folderPath = href.replaceFirst(draftsUrl, "Drafts");
		} else if (href.startsWith(deleteditemsUrl)) {
			folder.folderPath = href.replaceFirst(deleteditemsUrl, "Trash");
		} else {
			int index = href.indexOf(mailPath.substring(0,
					mailPath.length() - 1));
			if (index >= 0) {
				if (index + mailPath.length() > href.length()) {
					folder.folderPath = "";
				} else {
					folder.folderPath = href.substring(index
							+ mailPath.length());
				}
			} else {
				try {
					URI folderURI = new URI(href);
					folder.folderPath = folderURI.getPath();
				} catch (URISyntaxException e) {
					throw new DavMailException("EXCEPTION_INVALID_FOLDER_URL",
							href);
				}
			}
		}
		if (folder.folderPath.endsWith("/")) {
			folder.folderPath = folder.folderPath.substring(0,
					folder.folderPath.length() - 1);
		}
		return folder;
	}

	/**
	 * Convert logical or relative folder path to absolute folder path.
	 * 
	 * @param folderName
	 *            folder name
	 * @return folder path
	 */
	public String getFolderPath(String folderName) {
		String folderPath;
		if (folderName.startsWith("INBOX")) {
			folderPath = folderName.replaceFirst("INBOX", inboxUrl);
		} else if (folderName.startsWith("Trash")) {
			folderPath = folderName.replaceFirst("Trash", deleteditemsUrl);
		} else if (folderName.startsWith("Drafts")) {
			folderPath = folderName.replaceFirst("Drafts", draftsUrl);
		} else if (folderName.startsWith("Sent")) {
			folderPath = folderName.replaceFirst("Sent", sentitemsUrl);
		} else if (folderName.startsWith("calendar")) {
			folderPath = folderName.replaceFirst("calendar", calendarUrl);
		} else if (folderName.startsWith("public")) {
			folderPath = '/' + folderName;
			// absolute folder path
		} else if (folderName.startsWith("/")) {
			folderPath = folderName;
		} else {
			folderPath = mailPath + folderName;
		}
		return folderPath;
	}

	/**
	 * Get folder object. Folder name can be logical names INBOX, Drafts, Trash
	 * or calendar, or a path relative to user base folder or absolute path.
	 * 
	 * @param folderName
	 *            folder name
	 * @return Folder object
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public Folder getFolder(String folderName) throws IOException,
			HttpException {
		MultiStatusResponse[] responses = DavGatewayHttpClientFacade
				.executePropFindMethod(httpClient, URIUtil
						.encodePath(getFolderPath(folderName)), 0,
						FOLDER_PROPERTIES);
		Folder folder = null;
		if (responses.length > 0) {
			folder = buildFolder(responses[0]);
			folder.folderName = folderName;
		}
		return folder;
	}

	/**
	 * Create Exchange message folder.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public void createMessageFolder(String folderName) throws IOException,
			HttpException {
		createFolder(folderName, "IPF.Note");
	}

	/**
	 * Create Exchange calendar folder.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public void createCalendarFolder(String folderName) throws IOException,
			HttpException {
		createFolder(folderName, "IPF.Appointment");
	}

	/**
	 * Create Exchange folder with given folder class.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @param folderClass
	 *            folder class
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public void createFolder(String folderName, String folderClass)
			throws IOException, HttpException {
		String folderPath = getFolderPath(folderName);
		ArrayList<DavProperty> list = new ArrayList<DavProperty>();
		list
				.add(new DefaultDavProperty(
						DavPropertyName
								.create(
										"outlookfolderclass",
										Namespace
												.getNamespace("http://schemas.microsoft.com/exchange/")),
						folderClass));
		// standard MkColMethod does not take properties, override
		// PropPatchMethod instead
		PropPatchMethod method = new PropPatchMethod(URIUtil
				.encodePath(folderPath), list) {
			@Override
			public String getName() {
				return "MKCOL";
			}
		};
		HttpResponse response = DavGatewayHttpClientFacade.executeHttpMethod(
				httpClient, method);
		int status = response.getStatusLine().getStatusCode();
		// ok or alredy exists
		if (status != HttpStatus.SC_MULTI_STATUS
				&& status != HttpStatus.SC_METHOD_NOT_ALLOWED) {
			throw DavGatewayHttpClientFacade.buildHttpException(method,
					response);
		}
	}

	/**
	 * Delete Exchange folder.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @throws IOException
	 *             on error
	 */
	public void deleteFolder(String folderName) throws IOException {
		DavGatewayHttpClientFacade.executeDeleteMethod(httpClient, URIUtil
				.encodePath(getFolderPath(folderName)));
	}

	/**
	 * Move folder to target name.
	 * 
	 * @param folderName
	 *            current folder name/path
	 * @param targetName
	 *            target folder name/path
	 * @throws IOException
	 *             on error
	 */
	public void moveFolder(String folderName, String targetName)
			throws HttpException {
		String folderPath = getFolderPath(folderName);
		String targetPath = getFolderPath(targetName);
		MoveMethod method = new MoveMethod(URIUtil.encodePath(folderPath),
				URIUtil.encodePath(targetPath), false);
		try {
			HttpResponse response = httpClient.execute(method);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_PRECONDITION_FAILED) {
				throw new DavMailException("EXCEPTION_UNABLE_TO_MOVE_FOLDER");
			} else if (statusCode != HttpStatus.SC_CREATED) {
				throw DavGatewayHttpClientFacade.buildHttpException(method,
						response);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Exchange folder with IMAP properties
	 */
	public class Folder {
		/**
		 * Logical (IMAP) folder path.
		 */
		public String folderPath;
		/**
		 * Folder unread message count.
		 */
		public int unreadCount;
		/**
		 * true if folder has subfolders (DAV:hassubs).
		 */
		public boolean hasChildren;
		/**
		 * true if folder has no subfolders (DAV:nosubs).
		 */
		public boolean noInferiors;
		/**
		 * Requested folder name
		 */
		public String folderName;
		/**
		 * Folder content tag (to detect folder content changes).
		 */
		public String contenttag;
		/**
		 * Folder message list, empty before loadMessages call.
		 */
		public ExchangeSession.MessageList messages;

		/**
		 * Get IMAP folder flags.
		 * 
		 * @return folder flags in IMAP format
		 */
		public String getFlags() {
			if (noInferiors) {
				return "\\NoInferiors";
			} else if (hasChildren) {
				return "\\HasChildren";
			} else {
				return "\\HasNoChildren";
			}
		}

		/**
		 * Folder message count.
		 * 
		 * @return message count
		 */
		public int count() {
			return messages.size();
		}

		/**
		 * Compute IMAP uidnext.
		 * 
		 * @return max(messageuids)+1
		 */
		public long getUidNext() {
			return messages.get(messages.size() - 1).getImapUid() + 1;
		}

		/**
		 * Get message uid at index.
		 * 
		 * @param index
		 *            message index
		 * @return message uid
		 */
		public long getImapUid(int index) {
			return messages.get(index).getImapUid();
		}

		/**
		 * Get message at index.
		 * 
		 * @param index
		 *            message index
		 * @return message
		 */
		public Message get(int index) {
			return messages.get(index);
		}
	}

	/**
	 * Exchange message.
	 */
	public class Message implements Comparable<Message> {
		protected String messageUrl;

		protected String permanentUrl;
		/**
		 * Message uid.
		 */
		protected String uid;
		/**
		 * Message IMAP uid, unique in folder (x0e230003).
		 */
		protected long imapUid;
		/**
		 * MAPI message size.
		 */
		public int size;
		/**
		 * Mail header message-id.
		 */
		protected String messageId;
		/**
		 * Message date (urn:schemas:mailheader:date).
		 */
		public String date;

		/**
		 * Message flag: read.
		 */
		public boolean read;
		/**
		 * Message flag: deleted.
		 */
		public boolean deleted;
		/**
		 * Message flag: junk.
		 */
		public boolean junk;
		/**
		 * Message flag: flagged.
		 */
		public boolean flagged;
		/**
		 * Message flag: draft.
		 */
		public boolean draft;
		/**
		 * Message flag: answered.
		 */
		public boolean answered;
		/**
		 * Message flag: fowarded.
		 */
		public boolean forwarded;

		/**
		 * Message content parsed in a MIME message.
		 */
		protected MimeMessage mimeMessage;

		/**
		 * IMAP uid , unique in folder (x0e230003)
		 * 
		 * @return IMAP uid
		 */
		public long getImapUid() {
			return imapUid;
		}

		/**
		 * Exchange uid.
		 * 
		 * @return uid
		 */
		public String getUid() {
			return uid;
		}

		/**
		 * Return permanent message url.
		 * 
		 * @return permanent message url
		 * @throws URIException
		 *             on error
		 */
		public String getPermanentUrl() throws URISyntaxException {
			return permanentUrl;
		}

		/**
		 * Return encoded message name.
		 * 
		 * @return encoded message name
		 * @throws IOException
		 *             on error
		 */
		public String getEncodedMessageName() throws IOException, HttpException {
			int index = messageUrl.lastIndexOf('/');
			if (index < 0) {
				throw new DavMailException("EXCEPTION_INVALID_MESSAGE_URL",
						messageUrl);
			}

			return URIUtil.encodePath(messageUrl.substring(index + 1));
		}

		/**
		 * Return message flags in IMAP format.
		 * 
		 * @return IMAP flags
		 */
		public String getImapFlags() {
			StringBuilder buffer = new StringBuilder();
			if (read) {
				buffer.append("\\Seen ");
			}
			if (deleted) {
				buffer.append("\\Deleted ");
			}
			if (flagged) {
				buffer.append("\\Flagged ");
			}
			if (junk) {
				buffer.append("Junk ");
			}
			if (draft) {
				buffer.append("\\Draft ");
			}
			if (answered) {
				buffer.append("\\Answered ");
			}
			if (forwarded) {
				buffer.append("$Forwarded ");
			}
			return buffer.toString().trim();
		}

		/**
		 * Write MIME message to os
		 * 
		 * @param os
		 *            output stream
		 * @throws IOException
		 *             on error
		 * @throws DavMailException
		 */
		public void write(OutputStream os) throws IOException, DavMailException {
			HttpGet method = new HttpGet(permanentUrl);
			method.setHeader("Content-Type", "text/xml; charset=utf-8");
			method.setHeader("Translate", "f");
			BufferedReader reader = null;
			try {
				HttpResponse response = DavGatewayHttpClientFacade
						.executeGetMethod(httpClient, method, false);
				reader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				OutputStreamWriter isoWriter = new OutputStreamWriter(os);
				String line;
				while ((line = reader.readLine()) != null) {
					if (".".equals(line)) {
						line = "..";
						// patch text/calendar to include utf-8 encoding
					} else if ("Content-Type: text/calendar;".equals(line)) {
						StringBuilder headerBuffer = new StringBuilder();
						headerBuffer.append(line);
						while ((line = reader.readLine()) != null
								&& line.startsWith("\t")) {
							headerBuffer.append((char) 13);
							headerBuffer.append((char) 10);
							headerBuffer.append(line);
						}
						if (headerBuffer.indexOf("charset") < 0) {
							headerBuffer.append(";charset=utf-8");
						}
						headerBuffer.append((char) 13);
						headerBuffer.append((char) 10);
						headerBuffer.append(line);
						line = headerBuffer.toString();
					}
					isoWriter.write(line);
					isoWriter.write((char) 13);
					isoWriter.write((char) 10);
				}
				isoWriter.flush();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						LOGGER.warn("Error closing message input stream", e);
					}
				}
				method.abort();
			}
		}

		/**
		 * Load message content in a Mime message
		 * 
		 * @return mime message
		 * @throws IOException
		 *             on error
		 * @throws MessagingException
		 *             on error
		 * @throws DavMailException
		 */
		public MimeMessage getMimeMessage() throws IOException,
				MessagingException, DavMailException {
			if (mimeMessage == null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				write(baos);
				mimeMessage = new MimeMessage(null, new ByteArrayInputStream(
						baos.toByteArray()));
			}
			return mimeMessage;
		}

		/**
		 * Drop mime message to avoid keeping message content in memory.
		 */
		public void dropMimeMessage() {
			mimeMessage = null;
		}

		/**
		 * Delete message.
		 * 
		 * @throws IOException
		 *             on error
		 */
		public void delete() throws IOException {
			DavGatewayHttpClientFacade.executeDeleteMethod(httpClient,
					permanentUrl);
		}

		/**
		 * Comparator to sort messages by IMAP uid
		 * 
		 * @param message
		 *            other message
		 * @return imapUid comparison result
		 */
		public int compareTo(Message message) {
			long compareValue = (imapUid - message.imapUid);
			if (compareValue > 0) {
				return 1;
			} else if (compareValue < 0) {
				return -1;
			} else {
				return 0;
			}
		}

		/**
		 * Override equals, compare IMAP uids
		 * 
		 * @param message
		 *            other message
		 * @return true if IMAP uids are equal
		 */
		@Override
		public boolean equals(Object message) {
			return message instanceof Message
					&& imapUid == ((Message) message).imapUid;
		}

		/**
		 * Override hashCode, return imapUid hashcode.
		 * 
		 * @return imapUid hashcode
		 */
		@Override
		public int hashCode() {
			return (int) (imapUid ^ (imapUid >>> 32));
		}
	}

	/**
	 * Message list
	 */
	public static class MessageList extends ArrayList<Message> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Calendar event object
	 */
	public class Event {
		protected String href;
		protected String permanentUrl;
		protected String etag;
		protected String contentClass;
		protected String noneMatch;
		/**
		 * ICS content
		 */
		protected String icsBody;

		protected MimePart getCalendarMimePart(MimeMultipart multiPart)
				throws IOException, MessagingException {
			MimePart bodyPart = null;
			for (int i = 0; i < multiPart.getCount(); i++) {
				String contentType = multiPart.getBodyPart(i).getContentType();
				if (contentType.startsWith("text/calendar")
						|| contentType.startsWith("application/ics")) {
					bodyPart = (MimePart) multiPart.getBodyPart(i);
					break;
				} else if (contentType.startsWith("multipart")) {
					Object content = multiPart.getBodyPart(i).getContent();
					if (content instanceof MimeMultipart) {
						bodyPart = getCalendarMimePart((MimeMultipart) content);
					}
				}
			}

			return bodyPart;
		}

		/**
		 * Load ICS content from Exchange server. User Translate: f header to
		 * get MIME event content and get ICS attachment from it
		 * 
		 * @return ICS (iCalendar) event
		 * @throws IOException
		 *             on error
		 * @throws HttpException
		 */
		public String getICS() throws IOException, HttpException {
			String result = null;
			LOGGER.debug("Get event: " + permanentUrl);
			HttpGet method = new HttpGet(permanentUrl);
			method.setHeader("Content-Type", "text/xml; charset=utf-8");
			method.setHeader("Translate", "f");
			try {
				HttpResponse response = DavGatewayHttpClientFacade
						.executeGetMethod(httpClient, method, false);

				MimeMessage mimeMessage = new MimeMessage(null, response
						.getEntity().getContent());
				Object mimeBody = mimeMessage.getContent();
				MimePart bodyPart;
				if (mimeBody instanceof MimeMultipart) {
					bodyPart = getCalendarMimePart((MimeMultipart) mimeBody);
				} else {
					// no multipart, single body
					bodyPart = mimeMessage;
				}

				if (bodyPart == null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					mimeMessage.getDataHandler().writeTo(baos);
					baos.close();
					throw new DavMailException(
							"EXCEPTION_INVALID_MESSAGE_CONTENT", new String(
									baos.toByteArray(), "UTF-8"));
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bodyPart.getDataHandler().writeTo(baos);
				baos.close();
				result = fixICS(new String(baos.toByteArray(), "UTF-8"), true);
			} catch (IOException e) {
				LOGGER.warn("Unable to get event at " + permanentUrl + ": "
						+ e.getMessage());
			} catch (MessagingException e) {
				LOGGER.warn("Unable to get event at " + permanentUrl + ": "
						+ e.getMessage());
			} finally {
				method.abort();
			}
			return result;
		}

		/**
		 * Get event name (file name part in URL).
		 * 
		 * @return event name
		 */
		public String getName() {
			int index = href.lastIndexOf('/');
			if (index >= 0) {
				return href.substring(index + 1);
			} else {
				return href;
			}
		}

		/**
		 * Get event etag (last change tag).
		 * 
		 * @return event etag
		 */
		public String getEtag() {
			return etag;
		}

		protected String fixTimezoneId(String line, String validTimezoneId) {
			return StringUtil.replaceToken(line, "TZID=", ":", validTimezoneId);
		}

		protected void splitExDate(ICSBufferedWriter result, String line) {
			int cur = line.lastIndexOf(':') + 1;
			String start = line.substring(0, cur);

			for (int next = line.indexOf(',', cur); next != -1; next = line
					.indexOf(',', cur)) {
				String val = line.substring(cur, next);
				result.writeLine(start + val);

				cur = next + 1;
			}

			result.writeLine(start + line.substring(cur));
		}

		protected String getAllDayLine(String line) throws IOException,
				HttpException {
			int keyIndex = line.indexOf(';');
			int valueIndex = line.lastIndexOf(':');
			int valueEndIndex = line.lastIndexOf('T');
			if (valueIndex < 0 || valueEndIndex < 0) {
				throw new DavMailException("EXCEPTION_INVALID_ICS_LINE", line);
			}
			String dateValue = line.substring(valueIndex + 1, valueEndIndex);
			String key = line.substring(0, Math.max(keyIndex, valueIndex));
			return key + ";VALUE=DATE:" + dateValue;
		}

		protected String fixICS(String icsBody, boolean fromServer)
				throws IOException, HttpException {
			// first pass : detect
			class AllDayState {
				boolean isAllDay;
				boolean hasCdoAllDay;
				boolean isCdoAllDay;
			}

			dumpIndex++;

			// Convert event class from and to iCal
			// See
			// https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-privateevents.txt
			boolean isAppleiCal = false;
			boolean hasAttendee = false;
			boolean hasCdoBusyStatus = false;
			// detect ics event with empty timezone (all day from Lightning)
			boolean hasTimezone = false;
			String transp = null;
			String validTimezoneId = null;
			String eventClass = null;
			String organizer = null;
			String action = null;
			boolean sound = false;

			List<AllDayState> allDayStates = new ArrayList<AllDayState>();
			AllDayState currentAllDayState = new AllDayState();
			BufferedReader reader = null;
			try {
				reader = new ICSBufferedReader(new StringReader(icsBody));
				String line;
				while ((line = reader.readLine()) != null) {
					int index = line.indexOf(':');
					if (index >= 0) {
						String key = line.substring(0, index);
						String value = line.substring(index + 1);
						if ("DTSTART;VALUE=DATE".equals(key)) {
							currentAllDayState.isAllDay = true;
						} else if ("X-MICROSOFT-CDO-ALLDAYEVENT".equals(key)) {
							currentAllDayState.hasCdoAllDay = true;
							currentAllDayState.isCdoAllDay = "TRUE"
									.equals(value);
						} else if ("END:VEVENT".equals(line)) {
							allDayStates.add(currentAllDayState);
							currentAllDayState = new AllDayState();
						} else if ("PRODID".equals(key)
								&& line.contains("iCal")) {
							// detect iCal created events
							isAppleiCal = true;
						} else if (isAppleiCal
								&& "X-CALENDARSERVER-ACCESS".equals(key)) {
							eventClass = value;
						} else if (!isAppleiCal && "CLASS".equals(key)) {
							eventClass = value;
						} else if ("ACTION".equals(key)) {
							action = value;
						} else if ("ATTACH;VALUES=URI".equals(key)) {
							// This is a marker that this event has an alarm
							// with sound
							sound = true;
							// Set the default sound to whatever this event
							// contains
							// (under assumption that the user has the same
							// sound set
							// for all events)
							defaultSound = value;
						} else if (key.startsWith("ORGANIZER")) {
							if (value.startsWith("MAILTO:")) {
								organizer = value.substring(7);
							} else {
								organizer = value;
							}
						} else if (key.startsWith("ATTENDEE")) {
							hasAttendee = true;
						} else if ("TRANSP".equals(key)) {
							transp = value;
						} else if (line.startsWith("TZID:(GMT") ||
						// additional test for Outlook created recurring events
								line.startsWith("TZID:GMT ")) {
							try {
								validTimezoneId = ResourceBundle.getBundle(
										"timezones").getString(value);
							} catch (MissingResourceException mre) {
								LOGGER.warn(new BundleMessage(
										"LOG_INVALID_TIMEZONE", value));
							}
						} else if ("X-MICROSOFT-CDO-BUSYSTATUS".equals(key)) {
							hasCdoBusyStatus = true;
						} else if ("BEGIN:VTIMEZONE".equals(line)) {
							hasTimezone = true;
						}
					}
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
			// second pass : fix
			int count = 0;
			ICSBufferedWriter result = new ICSBufferedWriter();
			try {
				reader = new ICSBufferedReader(new StringReader(icsBody));
				String line;

				while ((line = reader.readLine()) != null) {
					// remove empty properties
					if ("CLASS:".equals(line) || "LOCATION:".equals(line)) {
						continue;
					}
					// fix invalid exchange timezoneid
					if (validTimezoneId != null && line.indexOf(";TZID=") >= 0) {
						line = fixTimezoneId(line, validTimezoneId);
					}
					if (!fromServer && "BEGIN:VEVENT".equals(line)
							&& !hasTimezone) {
						result
								.write(ExchangeSession.this.getVTimezone().timezoneBody);
						hasTimezone = true;
					}
					if (!fromServer && currentAllDayState.isAllDay
							&& "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE".equals(line)) {
						line = "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE";
					} else if (!fromServer && "END:VEVENT".equals(line)) {
						if (!hasCdoBusyStatus) {
							result.writeLine("X-MICROSOFT-CDO-BUSYSTATUS:"
									+ (!"TRANSPARENT".equals(transp) ? "BUSY"
											: "FREE"));
						}
						if (currentAllDayState.isAllDay
								&& !currentAllDayState.hasCdoAllDay) {
							result
									.writeLine("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE");
						}
						// add organizer line to all events created in Exchange
						// for active sync
						if (organizer == null) {
							result.writeLine("ORGANIZER:MAILTO:" + email);
						}
					} else if (!fromServer
							&& line.startsWith("X-MICROSOFT-CDO-BUSYSTATUS:")) {
						line = "X-MICROSOFT-CDO-BUSYSTATUS:"
								+ (!"TRANSPARENT".equals(transp) ? "BUSY"
										: "FREE");
					} else if (!fromServer && !currentAllDayState.isAllDay
							&& "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE".equals(line)) {
						line = "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE";
					} else if (fromServer && currentAllDayState.isCdoAllDay
							&& line.startsWith("DTSTART")
							&& !line.startsWith("DTSTART;VALUE=DATE")) {
						line = getAllDayLine(line);
					} else if (fromServer && currentAllDayState.isCdoAllDay
							&& line.startsWith("DTEND")
							&& !line.startsWith("DTEND;VALUE=DATE")) {
						line = getAllDayLine(line);
					} else if (!fromServer && currentAllDayState.isAllDay
							&& line.startsWith("DTSTART")
							&& line.startsWith("DTSTART;VALUE=DATE")) {
						line = "DTSTART;TZID=\""
								+ ExchangeSession.this.getVTimezone().timezoneId
								+ "\":" + line.substring(19) + "T000000";
					} else if (!fromServer && currentAllDayState.isAllDay
							&& line.startsWith("DTEND")
							&& line.startsWith("DTEND;VALUE=DATE")) {
						line = "DTEND;TZID=\""
								+ ExchangeSession.this.getVTimezone().timezoneId
								+ "\":" + line.substring(17) + "T000000";
					} else if (line.startsWith("TZID:")
							&& validTimezoneId != null) {
						line = "TZID:" + validTimezoneId;
					} else if ("BEGIN:VEVENT".equals(line)) {
						currentAllDayState = allDayStates.get(count++);
					} else if (line.startsWith("X-CALENDARSERVER-ACCESS:")) {
						if (!isAppleiCal) {
							continue;
						} else {
							if ("CONFIDENTIAL".equalsIgnoreCase(eventClass)) {
								result.writeLine("CLASS:PRIVATE");
							} else if ("PRIVATE".equalsIgnoreCase(eventClass)) {
								result.writeLine("CLASS:CONFIDENTIAL");
							} else {
								result.writeLine("CLASS:" + eventClass);
							}
						}
					} else if (line.startsWith("EXDATE;TZID=")
							|| line.startsWith("EXDATE:")) {
						// Apple iCal doesn't support EXDATE with multiple
						// exceptions
						// on one line. Split into multiple EXDATE entries
						// (which is
						// also legal according to the caldav standard).
						splitExDate(result, line);
						continue;
					} else if (line.startsWith("X-ENTOURAGE_UUID:")) {
						// Apple iCal doesn't understand this key, and it's
						// entourage
						// specific (i.e. not needed by any caldav client):
						// strip it out
						continue;
					} else if (fromServer && line.startsWith("ATTENDEE;")
							&& (line.indexOf(email) >= 0)) {
						// If this is coming from the server, strip out RSVP for
						// this
						// user as an attendee where the partstat is something
						// other
						// than PARTSTAT=NEEDS-ACTION since the RSVP confuses
						// iCal4 into
						// thinking the attendee has not replied

						int rsvpSuffix = line.indexOf("RSVP=TRUE;");
						int rsvpPrefix = line.indexOf(";RSVP=TRUE");

						if (((rsvpSuffix >= 0) || (rsvpPrefix >= 0))
								&& (line.indexOf("PARTSTAT=") >= 0)
								&& (line.indexOf("PARTSTAT=NEEDS-ACTION") < 0)) {

							// Strip out the "RSVP" line from the calendar entry
							if (rsvpSuffix >= 0) {
								line = line.substring(0, rsvpSuffix)
										+ line.substring(rsvpSuffix + 10);
							} else {
								line = line.substring(0, rsvpPrefix)
										+ line.substring(rsvpPrefix + 10);
							}

						}
					} else if (line.startsWith("ACTION:")) {
						if (fromServer && "DISPLAY".equals(action)) {
							// Use the default iCal alarm action instead
							// of the alarm Action exchange (and blackberry)
							// understand.
							// This is a bit of a hack because we don't know
							// what type
							// of alarm an iCal user really wants - but we know
							// what the
							// default is, and can setup the default action type

							result.writeLine("ACTION:AUDIO");

							if (!sound) {
								// Add default sound into the audio alarm
								result.writeLine("ATTACH;VALUE=URI:"
										+ defaultSound);
							}

							continue;
						} else if (!fromServer && "AUDIO".equals(action)) {
							// Use the alarm action that exchange (and
							// blackberry) understand
							// (exchange and blackberry don't understand audio
							// actions)

							result.writeLine("ACTION:DISPLAY");
							continue;
						}

						// Don't recognize this type of action: pass it through

					} else if (line.startsWith("CLASS:")) {
						if (isAppleiCal) {
							continue;
						} else {
							if ("PRIVATE".equalsIgnoreCase(eventClass)) {
								result
										.writeLine("X-CALENDARSERVER-ACCESS:CONFIDENTIAL");
							} else if ("CONFIDENTIAL"
									.equalsIgnoreCase(eventClass)) {
								result
										.writeLine("X-CALENDARSERVER-ACCESS:PRIVATE");
							} else {
								result.writeLine("X-CALENDARSERVER-ACCESS:"
										+ eventClass);
							}
						}
						// remove organizer line if user is organizer for iPhone
					} else if (fromServer && line.startsWith("ORGANIZER")
							&& !hasAttendee) {
						continue;
					} else if (organizer != null && line.startsWith("ATTENDEE")
							&& line.contains(organizer)) {
						// Ignore organizer as attendee
						continue;
					} else if (!fromServer && line.startsWith("ATTENDEE")) {
						line = replaceIcal4Principal(line);
					}

					result.writeLine(line);
				}
			} finally {
				reader.close();
			}

			return result.toString();
		}

		protected String getICSValue(String icsBody, String prefix,
				String defval) throws IOException {
			// only return values in VEVENT section, not VALARM
			Stack<String> sectionStack = new Stack<String>();
			BufferedReader reader = null;

			try {
				reader = new ICSBufferedReader(new StringReader(icsBody));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("BEGIN:")) {
						sectionStack.push(line);
					} else if (line.startsWith("END:")
							&& !sectionStack.isEmpty()) {
						sectionStack.pop();
					} else if (!sectionStack.isEmpty()
							&& "BEGIN:VEVENT".equals(sectionStack.peek())
							&& line.startsWith(prefix)) {
						return line.substring(prefix.length());
					}
				}

			} finally {
				if (reader != null) {
					reader.close();
				}
			}

			return defval;
		}

		protected String getICSSummary(String icsBody) throws IOException {
			return getICSValue(icsBody, "SUMMARY:", BundleMessage
					.format("MEETING_REQUEST"));
		}

		protected String getICSDescription(String icsBody) throws IOException {
			return getICSValue(icsBody, "DESCRIPTION:", "");
		}

		class Participants {
			String attendees;
			String organizer;
		}

		/**
		 * Parse ics event for attendees and organizer. For notifications, only
		 * include attendees with RSVP=TRUE or PARTSTAT=NEEDS-ACTION
		 * 
		 * @param isNotification
		 *            get only notified attendees
		 * @return participants
		 * @throws IOException
		 *             on error
		 */
		protected Participants getParticipants(boolean isNotification)
				throws IOException {
			HashSet<String> attendees = new HashSet<String>();
			String organizer = null;
			BufferedReader reader = null;
			try {
				reader = new ICSBufferedReader(new StringReader(icsBody));
				String line;
				while ((line = reader.readLine()) != null) {
					int index = line.indexOf(':');
					if (index >= 0) {
						String key = line.substring(0, index);
						String value = line.substring(index + 1);
						int semiColon = key.indexOf(';');
						if (semiColon >= 0) {
							key = key.substring(0, semiColon);
						}
						if ("ORGANIZER".equals(key) || "ATTENDEE".equals(key)) {
							int colonIndex = value.indexOf(':');
							if (colonIndex >= 0) {
								value = value.substring(colonIndex + 1);
							}
							value = replaceIcal4Principal(value);
							if ("ORGANIZER".equals(key)) {
								organizer = value;
								// exclude current user and invalid values from
								// recipients
								// also exclude no action attendees
							} else if (!email.equalsIgnoreCase(value)
									&& value.indexOf('@') >= 0
									&& (!isNotification
											|| line.indexOf("RSVP=TRUE") >= 0 || line
											.indexOf("PARTSTAT=NEEDS-ACTION") >= 0)) {
								attendees.add(value);
							}
						}
					}
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
			Participants participants = new Participants();
			if (!attendees.isEmpty()) {
				StringBuilder result = new StringBuilder();
				for (String recipient : attendees) {
					if (result.length() > 0) {
						result.append(", ");
					}
					result.append(recipient);
				}
				participants.attendees = result.toString();
			}
			participants.organizer = organizer;
			return participants;
		}

		protected String getICSMethod(String icsBody) {
			String icsMethod = StringUtil.getToken(icsBody, "METHOD:", "\r");
			if (icsMethod == null) {
				// default method is REQUEST
				icsMethod = "REQUEST";
			}
			return icsMethod;
		}

	}

	/**
	 * Search calendar messages in provided folder.
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @return list of calendar messages as Event objects
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public List<Event> getEventMessages(String folderPath) throws IOException,
			URISyntaxException, HttpException {
		List<Event> result;
		String searchQuery = "Select \"DAV:getetag\", \"http://schemas.microsoft.com/exchange/permanenturl\""
				+ "                FROM Scope('SHALLOW TRAVERSAL OF \""
				+ folderPath
				+ "\"')\n"
				+ "                WHERE \"DAV:contentclass\" = 'urn:content-classes:calendarmessage'\n"
				+ "                AND (NOT \""
				+ scheduleStateProperty.getNamespace().getURI()
				+ scheduleStateProperty.getName()
				+ "\" = 'CALDAV:schedule-processed')\n"
				+ "                ORDER BY \"urn:schemas:calendar:dtstart\" DESC\n";
		result = getEvents(folderPath, searchQuery);
		return result;
	}

	/**
	 * Search calendar events in provided folder.
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @return list of calendar events
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public List<Event> getAllEvents(String folderPath, int caldavPastDelay)
			throws IOException, URISyntaxException, HttpException {
		String dateCondition = "";
		if (caldavPastDelay != Integer.MAX_VALUE) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -caldavPastDelay);
			dateCondition = "                AND \"urn:schemas:calendar:dtstart\" > '"
					+ formatSearchDate(cal.getTime()) + "'\n";
		}

		String searchQuery = "Select \"DAV:getetag\", \"http://schemas.microsoft.com/exchange/permanenturl\""
				+ "                FROM Scope('SHALLOW TRAVERSAL OF \""
				+ folderPath
				+ "\"')\n"
				+ "                WHERE ("
				+
				// VTODO events have a null instancetype
				"                       \"urn:schemas:calendar:instancetype\" is null OR"
				+ "                       \"urn:schemas:calendar:instancetype\" = 1\n"
				+ "                OR (\"urn:schemas:calendar:instancetype\" = 0\n"
				+ dateCondition
				+ "                )) AND \"DAV:contentclass\" = 'urn:content-classes:appointment'\n"
				+ "                ORDER BY \"urn:schemas:calendar:dtstart\" DESC\n";
		return getEvents(folderPath, searchQuery);
	}

	/**
	 * Search calendar events or messages in provided folder matching the search
	 * query.
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @param searchQuery
	 *            Exchange search query
	 * @return list of calendar messages as Event objects
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	protected List<Event> getEvents(String folderPath, String searchQuery)
			throws IOException, URISyntaxException, HttpException {
		List<Event> events = new ArrayList<Event>();
		MultiStatusResponse[] responses = DavGatewayHttpClientFacade
				.executeSearchMethod(httpClient,
						URIUtil.encodePath(folderPath), searchQuery);
		for (MultiStatusResponse response : responses) {
			events.add(buildEvent(response));
		}
		return events;
	}

	/**
	 * Get event named eventName in folder
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @param eventName
	 *            event name
	 * @return event object
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public Event getEvent(String folderPath, String eventName)
			throws IOException, URISyntaxException, HttpException {
		String eventPath = folderPath + '/' + eventName;
		return getEvent(eventPath);
	}

	/**
	 * Get event by url
	 * 
	 * @param eventPath
	 *            Event path
	 * @return event object
	 * @throws IOException
	 *             on error
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public Event getEvent(String eventPath) throws IOException,
			URISyntaxException, HttpException {
		MultiStatusResponse[] responses = DavGatewayHttpClientFacade
				.executePropFindMethod(httpClient, URIUtil
						.encodePath(eventPath), 0, EVENT_REQUEST_PROPERTIES);
		if (responses.length == 0) {
			throw new DavMailException("EXCEPTION_EVENT_NOT_FOUND");
		}
		return buildEvent(responses[0]);
	}

	/**
	 * Delete event named eventName in folder
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @param eventName
	 *            event name
	 * @return HTTP status
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public int deleteEvent(String folderPath, String eventName)
			throws IOException, HttpException {
		String eventPath = URIUtil.encodePath(folderPath + '/' + eventName);
		int status;
		if (inboxUrl.endsWith(folderPath)) {
			// do not delete calendar messages, mark read and processed
			ArrayList<DavProperty> list = new ArrayList<DavProperty>();
			list.add(new DefaultDavProperty(scheduleStateProperty,
					"CALDAV:schedule-processed"));
			list.add(new DefaultDavProperty(DavPropertyName.create("read",
					URN_SCHEMAS_HTTPMAIL), "1"));
			PropPatchMethod patchMethod = new PropPatchMethod(eventPath, list);
			DavGatewayHttpClientFacade.executeMethod(httpClient, patchMethod);
			status = HttpStatus.SC_OK;
		} else {
			status = DavGatewayHttpClientFacade.executeDeleteMethod(httpClient,
					eventPath);
		}
		return status;
	}

	protected Event buildEvent(MultiStatusResponse calendarResponse)
			throws URISyntaxException {
		Event event = new Event();
		event.href = URIUtil.decodePath(calendarResponse.getHref());
		event.permanentUrl = getPropertyIfExists(calendarResponse
				.getProperties(HttpStatus.SC_OK), "permanenturl",
				SCHEMAS_EXCHANGE);
		event.etag = getPropertyIfExists(calendarResponse
				.getProperties(HttpStatus.SC_OK), "getetag", Namespace
				.getNamespace("DAV:"));
		return event;
	}

	private static int dumpIndex;
	private String defaultSound = "Basso";

	/**
	 * Replace iCal4 (Snow Leopard) principal paths with mailto expression
	 * 
	 * @param value
	 *            attendee value or ics line
	 * @return fixed value
	 */
	protected String replaceIcal4Principal(String value) {
		if (value.contains("/principals/__uuids__/")) {
			return value.replaceAll(
					"/principals/__uuids__/([^/]*)__AT__([^/]*)/",
					"mailto:$1@$2");
		} else {
			return value;
		}
	}

	/**
	 * Event result object to hold HTTP status and event etag from an event
	 * creation/update.
	 */
	public static class EventResult {
		/**
		 * HTTP status
		 */
		public int status;
		/**
		 * Event etag from response HTTP header
		 */
		public String etag;
	}

	/**
	 * Get folder ctag (change tag). This flag changes whenever folder or folder
	 * content changes
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @return folder ctag
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public String getFolderCtag(String folderPath) throws IOException,
			HttpException {
		return getFolderProperty(folderPath, CONTENT_TAG);
	}

	/**
	 * Get folder resource tag. Same as etag for folders, changes when folder
	 * (not content) changes
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @return folder resource tag
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public String getFolderResourceTag(String folderPath) throws IOException,
			HttpException {
		return getFolderProperty(folderPath, RESOURCE_TAG);
	}

	protected String getFolderProperty(String folderPath,
			DavPropertyNameSet davPropertyNameSet) throws IOException,
			HttpException {
		String result;
		MultiStatusResponse[] responses = DavGatewayHttpClientFacade
				.executePropFindMethod(httpClient, URIUtil
						.encodePath(folderPath), 0, davPropertyNameSet);
		if (responses.length == 0) {
			throw new DavMailException("EXCEPTION_UNABLE_TO_GET_FOLDER",
					folderPath);
		}
		DavPropertySet properties = responses[0]
				.getProperties(HttpStatus.SC_OK);
		DavPropertyName davPropertyName = davPropertyNameSet.iterator()
				.nextPropertyName();
		result = getPropertyIfExists(properties, davPropertyName);
		if (result == null) {
			throw new DavMailException("EXCEPTION_UNABLE_TO_GET_PROPERTY",
					davPropertyName);
		}
		return result;
	}

	/**
	 * Get current Exchange alias name from login name
	 * 
	 * @return user name
	 */
	protected String getAliasFromLogin() {
		// Exchange 2007 : userName is login without domain
		String result = this.userName;
		int index = result.indexOf('\\');
		if (index >= 0) {
			result = result.substring(index + 1);
		}
		return result;
	}

	/**
	 * Get current Exchange alias name from mailbox name
	 * 
	 * @return user name
	 */
	protected String getAliasFromMailPath() {
		if (mailPath == null) {
			return null;
		}
		int index = mailPath.lastIndexOf('/', mailPath.length() - 2);
		if (index >= 0 && mailPath.endsWith("/")) {
			return mailPath.substring(index + 1, mailPath.length() - 1);
		} else {
			LOGGER.warn(new BundleMessage("EXCEPTION_INVALID_MAIL_PATH",
					mailPath));
			return null;
		}
	}

	/**
	 * Get user alias from mailbox display name over Webdav.
	 * 
	 * @return user alias
	 * @throws HttpException
	 */
	public String getAliasFromMailboxDisplayName() throws HttpException {
		if (mailPath == null) {
			return null;
		}
		String displayName = null;
		try {
			MultiStatusResponse[] responses = DavGatewayHttpClientFacade
					.executePropFindMethod(httpClient, URIUtil
							.encodePath(mailPath), 0, DISPLAY_NAME);
			if (responses.length == 0) {
				LOGGER.warn(new BundleMessage(
						"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", mailPath));
			} else {
				displayName = getPropertyIfExists(responses[0]
						.getProperties(HttpStatus.SC_OK), "displayname",
						Namespace.getNamespace("DAV:"));
			}
		} catch (IOException e) {
			LOGGER.warn(new BundleMessage(
					"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", mailPath));
		}
		return displayName;
	}

	/**
	 * Build Caldav calendar path for principal and folder name. - prefix is
	 * current user mailbox path if principal is current user, else prefix is
	 * parent folder of current user mailbox path followed by principal - suffix
	 * according to well known folder names (internationalized on Exchange)
	 * 
	 * @param principal
	 *            calendar principal
	 * @param folderName
	 *            requested folder name
	 * @return Exchange folder path
	 * @throws IOException
	 *             on error
	 * @throws DavMailException
	 */
	public String buildCalendarPath(String principal, String folderName)
			throws IOException, DavMailException {
		StringBuilder buffer = new StringBuilder();
		// other user calendar => replace principal folder name in mailPath
		if (principal != null && !alias.equals(principal)
				&& !email.equals(principal)) {
			LOGGER.debug("Detected shared calendar path for principal "
					+ principal + ", user principal is " + email);
			int index = mailPath.lastIndexOf('/', mailPath.length() - 2);
			if (index >= 0 && mailPath.endsWith("/")) {
				buffer.append(mailPath.substring(0, index + 1)).append(
						principal).append('/');
			} else {
				throw new DavMailException("EXCEPTION_INVALID_MAIL_PATH",
						mailPath);
			}
		} else if (principal != null) {
			buffer.append(mailPath);
		}

		if (folderName != null && folderName.startsWith("calendar")) {
			// replace 'calendar' folder name with i18n name
			buffer.append(calendarUrl
					.substring(calendarUrl.lastIndexOf('/') + 1));

			// sub calendar folder => append sub folder name
			int index = folderName.indexOf('/');
			if (index >= 0) {
				buffer.append(folderName.substring(index));
			}
			// replace 'inbox' folder name with i18n name
		} else if ("inbox".equals(folderName)) {
			buffer.append(inboxUrl.substring(inboxUrl.lastIndexOf('/') + 1));
			// append folder name without replace (public folder)
		} else if (folderName != null && folderName.length() > 0) {
			buffer.append(folderName);
		}
		return buffer.toString();
	}

	/**
	 * Build base path for cmd commands (galfind, gallookup). This does not work
	 * with freebusy, which requires /public/
	 * 
	 * @return cmd base path
	 */
	public String getCmdBasePath() {
		if (mailPath == null) {
			return "/public/";
		} else {
			return mailPath;
		}
	}

	/**
	 * Get user email from global address list (galfind).
	 * 
	 * @param alias
	 *            user alias
	 * @return user email
	 * @throws DavMailException
	 */
	public String getEmail(String alias) throws DavMailException {
		String emailResult = null;
		if (alias != null) {
			HttpGet getMethod = null;
			String path = null;
			try {
				path = getBaseUrl() + getCmdBasePath() + "?Cmd=galfind&AN="
						+ URIUtil.encodePath(alias);
				getMethod = new HttpGet(path);
				HttpResponse response = DavGatewayHttpClientFacade
						.executeGetMethod(httpClient, getMethod, true);
				Map<String, Map<String, String>> results = XMLStreamUtil
						.getElementContentsAsMap(response.getEntity()
								.getContent(), "item", "AN");
				Map<String, String> result = results.get(alias.toLowerCase());
				if (result != null) {
					emailResult = result.get("EM");
				}
			} catch (IOException e) {
				LOGGER.debug("GET " + path + " failed: " + e + ' '
						+ e.getMessage());
			}
		}
		return emailResult;
	}

	private String getBaseUrl() {
		return this.authScheme + "://" + this.hostName;
	}

	/**
	 * Determine user email through various means.
	 * 
	 * @param hostName
	 *            Exchange server host name for last failover
	 * @param methodPath
	 *            current httpclient method path
	 * @throws HttpException
	 */
	public void buildEmail(String hostName, String methodPath)
			throws HttpException {
		// first try to get email from login name
		alias = getAliasFromLogin();
		email = getEmail(alias);
		// failover: use mailbox name as alias
		if (email == null) {
			alias = getAliasFromMailPath();
			email = getEmail(alias);
		}
		// another failover : get alias from mailPath display name
		if (email == null) {
			alias = getAliasFromMailboxDisplayName();
			email = getEmail(alias);
		}
		if (email == null) {
			// failover : get email from Exchange 2007 Options page
			alias = getAliasFromOptions(methodPath);
			email = getEmail(alias);
			// failover: get email from options
			if (alias != null && email == null) {
				email = getEmailFromOptions(methodPath);
			}
		}
		if (email == null) {
			LOGGER.debug("Unable to get user email with alias "
					+ getAliasFromLogin() + " or " + getAliasFromMailPath()
					+ " or " + getAliasFromOptions(methodPath));
			// last failover: build email from domain name and mailbox display
			// name
			StringBuilder buffer = new StringBuilder();
			// most reliable alias
			alias = getAliasFromMailboxDisplayName();
			if (alias == null) {
				alias = getAliasFromLogin();
			}
			if (alias != null) {
				buffer.append(alias);
				buffer.append('@');
				int dotIndex = hostName.indexOf('.');
				if (dotIndex >= 0) {
					buffer.append(hostName.substring(dotIndex + 1));
				}
			}
			email = buffer.toString();
		}
	}

	static final String MAILBOX_BASE = "cn=recipients/cn=";

	protected String getAliasFromOptions(String path) throws DavMailException {
		String result = null;
		// get user mail URL from html body
		BufferedReader optionsPageReader = null;
		HttpGet optionsMethod = new HttpGet(path + "?ae=Options&t=About");
		try {
			HttpResponse response = DavGatewayHttpClientFacade
					.executeGetMethod(httpClient, optionsMethod, false);
			optionsPageReader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line;
			// find mailbox full name
			// noinspection StatementWithEmptyBody
			while ((line = optionsPageReader.readLine()) != null
					&& line.toLowerCase().indexOf(MAILBOX_BASE) == -1) {
			}
			if (line != null) {
				int start = line.toLowerCase().indexOf(MAILBOX_BASE)
						+ MAILBOX_BASE.length();
				int end = line.indexOf('<', start);
				result = line.substring(start, end);
			}
		} catch (IOException e) {
			LOGGER.error("Error parsing options page at "
					+ optionsMethod.getURI().getPath());
		} finally {
			if (optionsPageReader != null) {
				try {
					optionsPageReader.close();
				} catch (IOException e) {
					LOGGER.error("Error parsing options page at "
							+ optionsMethod.getURI().getPath());
				}
			}
		}

		return result;
	}

	protected String getEmailFromOptions(String path) throws DavMailException {
		String result = null;
		// get user mail URL from html body
		BufferedReader optionsPageReader = null;
		HttpGet optionsMethod = new HttpGet(path + "?ae=Options&t=About");
		try {
			HttpResponse response = DavGatewayHttpClientFacade
					.executeGetMethod(httpClient, optionsMethod, false);
			optionsPageReader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line;
			// find email
			// noinspection StatementWithEmptyBody
			while ((line = optionsPageReader.readLine()) != null
					&& (line.indexOf('[') == -1 || line.indexOf('@') == -1 || line
							.indexOf(']') == -1)) {
			}
			if (line != null) {
				int start = line.toLowerCase().indexOf('[') + 1;
				int end = line.indexOf(']', start);
				result = line.substring(start, end);
			}
		} catch (IOException e) {
			LOGGER.error("Error parsing options page at "
					+ optionsMethod.getURI().getPath());
		} finally {
			if (optionsPageReader != null) {
				try {
					optionsPageReader.close();
				} catch (IOException e) {
					LOGGER.error("Error parsing options page at "
							+ optionsMethod.getURI().getPath());
				}
			}
		}

		return result;
	}

	/**
	 * Get current user email
	 * 
	 * @return user email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Get current user alias
	 * 
	 * @return user email
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Search users in global address book
	 * 
	 * @param searchAttribute
	 *            exchange search attribute
	 * @param searchValue
	 *            search value
	 * @return List of users
	 * @throws IOException
	 *             on error
	 */
	public Map<String, Map<String, String>> galFind(String searchAttribute,
			String searchValue) throws IOException {
		Map<String, Map<String, String>> results = null;
		HttpGet getMethod = new HttpGet(URIUtil.encodePath(getCmdBasePath()
				+ "?Cmd=galfind&" + searchAttribute + '=' + searchValue));
		HttpResponse response;
		try {
			response = DavGatewayHttpClientFacade.executeGetMethod(httpClient,
					getMethod, true);
			results = XMLStreamUtil.getElementContentsAsMap(response
					.getEntity().getContent(), "item", "AN");
			LOGGER.debug("galfind " + searchAttribute + '=' + searchValue
					+ ": " + results.size() + " result(s)");
		} catch (DavMailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//      
		return results;
	}

	static final String DAV_UID_FILTER = "\"DAV:uid\"='";

	/**
	 * Get extended address book information for person with gallookup. Does not
	 * work with Exchange 2007
	 * 
	 * @param person
	 *            person attributes map
	 * @throws DavMailException
	 */
	public void galLookup(Map<String, String> person) throws DavMailException {
		if (!disableGalLookup) {
			HttpGet getMethod = null;
			try {
				getMethod = new HttpGet(URIUtil.encodePath(getCmdBasePath()
						+ "?Cmd=gallookup&ADDR=" + person.get("EM")));
				HttpResponse response = DavGatewayHttpClientFacade
						.executeGetMethod(httpClient, getMethod, true);
				Map<String, Map<String, String>> results = XMLStreamUtil
						.getElementContentsAsMap(response.getEntity()
								.getContent(), "person", "alias");
				// add detailed information
				if (!results.isEmpty()) {
					Map<String, String> fullperson = results.get(person.get(
							"AN").toLowerCase());
					if (fullperson != null) {
						for (Map.Entry<String, String> entry : fullperson
								.entrySet()) {
							person.put(entry.getKey(), entry.getValue());
						}
					}
				}
			} catch (IOException e) {
				LOGGER.warn("Unable to gallookup person: " + person
						+ ", disable GalLookup");
				disableGalLookup = true;
			} finally {
				if (getMethod != null) {
					getMethod.abort();
				}
			}
		}
	}

	/**
	 * Get freebusy info for attendee between start and end date.
	 * 
	 * @param attendee
	 *            attendee email
	 * @param startDateValue
	 *            start date
	 * @param endDateValue
	 *            end date
	 * @return FreeBusy info
	 * @throws IOException
	 *             on error
	 * @throws DavMailException
	 */
	public FreeBusy getFreebusy(String attendee, String startDateValue,
			String endDateValue) throws IOException, DavMailException {
		attendee = replaceIcal4Principal(attendee);
		if (attendee.startsWith("mailto:") || attendee.startsWith("MAILTO:")) {
			attendee = attendee.substring("mailto:".length());
		}

		SimpleDateFormat exchangeZuluDateFormat = getExchangeZuluDateFormat();
		SimpleDateFormat icalDateFormat = getZuluDateFormat();

		String freebusyUrl;
		Date startDate;
		Date endDate;
		try {
			if (startDateValue.length() == 8) {
				startDate = parseDate(startDateValue);
			} else {
				startDate = icalDateFormat.parse(startDateValue);
			}
			if (endDateValue.length() == 8) {
				endDate = parseDate(endDateValue);
			} else {
				endDate = icalDateFormat.parse(endDateValue);
			}
			freebusyUrl = "/public/?cmd=freebusy" + "&start="
					+ exchangeZuluDateFormat.format(startDate) + "&end="
					+ exchangeZuluDateFormat.format(endDate) + "&interval="
					+ FREE_BUSY_INTERVAL + "&u=SMTP:" + attendee;
		} catch (ParseException e) {
			throw new DavMailException("EXCEPTION_INVALID_DATES", e
					.getMessage());
		}

		FreeBusy freeBusy = null;
		HttpGet getMethod = new HttpGet(freebusyUrl);
		getMethod.setHeader("Content-Type", "text/xml");

		try {
			HttpResponse response = DavGatewayHttpClientFacade
					.executeGetMethod(httpClient, getMethod, true);

			String fbdata = StringUtil.getLastToken(EntityUtils
					.toString(response.getEntity()), "<a:fbdata>",
					"</a:fbdata>");
			if (fbdata != null) {
				freeBusy = new FreeBusy(icalDateFormat, startDate, fbdata);
			}
		} finally {
			getMethod.abort();
		}

		if (freeBusy != null && freeBusy.knownAttendee) {
			return freeBusy;
		} else {
			return null;
		}
	}

	/**
	 * Exchange to iCalendar Free/Busy parser. Free time returns 0, Tentative
	 * returns 1, Busy returns 2, and Out of Office (OOF) returns 3
	 */
	public static final class FreeBusy {
		final SimpleDateFormat icalParser;
		boolean knownAttendee = true;
		static final HashMap<Character, String> FBTYPES = new HashMap<Character, String>();

		static {
			FBTYPES.put('1', "BUSY-TENTATIVE");
			FBTYPES.put('2', "BUSY");
			FBTYPES.put('3', "BUSY-UNAVAILABLE");
		}

		final HashMap<String, StringBuilder> busyMap = new HashMap<String, StringBuilder>();

		StringBuilder getBusyBuffer(char type) {
			String fbType = FBTYPES.get(Character.valueOf(type));
			StringBuilder buffer = busyMap.get(fbType);
			if (buffer == null) {
				buffer = new StringBuilder();
				busyMap.put(fbType, buffer);
			}
			return buffer;
		}

		void startBusy(char type, Calendar currentCal) {
			if (type == '4') {
				knownAttendee = false;
			} else if (type != '0') {
				StringBuilder busyBuffer = getBusyBuffer(type);
				if (busyBuffer.length() > 0) {
					busyBuffer.append(',');
				}
				busyBuffer.append(icalParser.format(currentCal.getTime()));
			}
		}

		void endBusy(char type, Calendar currentCal) {
			if (type != '0' && type != '4') {
				getBusyBuffer(type).append('/').append(
						icalParser.format(currentCal.getTime()));
			}
		}

		FreeBusy(SimpleDateFormat icalParser, Date startDate, String fbdata) {
			this.icalParser = icalParser;
			if (fbdata.length() > 0) {
				Calendar currentCal = Calendar.getInstance(TimeZone
						.getTimeZone("UTC"));
				currentCal.setTime(startDate);

				startBusy(fbdata.charAt(0), currentCal);
				for (int i = 1; i < fbdata.length() && knownAttendee; i++) {
					currentCal.add(Calendar.MINUTE, FREE_BUSY_INTERVAL);
					char previousState = fbdata.charAt(i - 1);
					char currentState = fbdata.charAt(i);
					if (previousState != currentState) {
						endBusy(previousState, currentCal);
						startBusy(currentState, currentCal);
					}
				}
				currentCal.add(Calendar.MINUTE, FREE_BUSY_INTERVAL);
				endBusy(fbdata.charAt(fbdata.length() - 1), currentCal);
			}
		}

		/**
		 * Append freebusy information to buffer.
		 * 
		 * @param buffer
		 *            String buffer
		 */
		public void appendTo(StringBuilder buffer) {
			for (Map.Entry<String, StringBuilder> entry : busyMap.entrySet()) {
				buffer.append("FREEBUSY;FBTYPE=").append(entry.getKey())
						.append(':').append(entry.getValue()).append((char) 13)
						.append((char) 10);
			}
		}
	}

	protected final class VTimezone {
		private String timezoneBody;
		private String timezoneId;

		/**
		 * create a fake event to get VTIMEZONE body
		 * 
		 * @throws HttpException
		 */
		private void load() throws HttpException {
			try {
				// create temporary folder
				String folderPath = ExchangeSession.this
						.getFolderPath("davmailtemp");
				ExchangeSession.this.createCalendarFolder(folderPath);

				HttpPost postMethod = new HttpPost(URIUtil
						.encodePath(folderPath));

				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				formparams.add(new BasicNameValuePair("Cmd", "saveappt"));
				formparams
						.add(new BasicNameValuePair("FORMTYPE", "appointment"));
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						formparams, "UTF-8");
				postMethod.setEntity(entity);
				String fakeEventUrl = null;
				try {
					// create fake event
					HttpResponse response = ExchangeSession.this.httpClient
							.execute(postMethod);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						fakeEventUrl = StringUtil.getToken(EntityUtils
								.toString(response.getEntity()),
								"<span id=\"itemHREF\">", "</span>");
					}
				} finally {
					postMethod.abort();
				}
				// failover for Exchange 2007, use PROPPATCH with forced
				// timezone
				if (fakeEventUrl == null) {
					ArrayList<DavProperty> propertyList = new ArrayList<DavProperty>();
					propertyList.add(new DefaultDavProperty(DavPropertyName
							.create("contentclass", Namespace
									.getNamespace("DAV:")),
							"urn:content-classes:appointment"));
					propertyList
							.add(new DefaultDavProperty(
									DavPropertyName
											.create(
													"outlookmessageclass",
													Namespace
															.getNamespace("http://schemas.microsoft.com/exchange/")),
									"IPM.Appointment"));
					propertyList.add(new DefaultDavProperty(DavPropertyName
							.create("instancetype", Namespace
									.getNamespace("urn:schemas:calendar:")),
							"0"));
					// get forced timezone id from settings
					// timezoneId = Settings.getProperty("davmail.timezoneId");
					if (timezoneId != null) {
						propertyList
								.add(new DefaultDavProperty(
										DavPropertyName
												.create(
														"timezoneid",
														Namespace
																.getNamespace("urn:schemas:calendar:")),
										timezoneId));
					}
					String patchMethodUrl = URIUtil.encodePath(folderPath)
							+ '/' + UUID.randomUUID().toString() + ".EML";
					PropPatchMethod patchMethod = new PropPatchMethod(URIUtil
							.encodePath(patchMethodUrl), propertyList);
					try {
						HttpResponse response = httpClient.execute(patchMethod);
						int statusCode = response.getStatusLine()
								.getStatusCode();
						if (statusCode == HttpStatus.SC_MULTI_STATUS) {
							fakeEventUrl = patchMethodUrl;
						}
					} finally {
						patchMethod.abort();
					}
				}
				if (fakeEventUrl != null) {
					// get fake event body
					HttpGet getMethod = new HttpGet(URIUtil
							.encodePath(fakeEventUrl));
					getMethod.addHeader("Translate", "f");
					try {
						HttpResponse response = ExchangeSession.this.httpClient
								.execute(getMethod);
						timezoneBody = "BEGIN:VTIMEZONE"
								+ StringUtil.getToken(EntityUtils
										.toString(response.getEntity()),
										"BEGIN:VTIMEZONE", "END:VTIMEZONE")
								+ "END:VTIMEZONE\r\n";
						timezoneId = StringUtil.getToken(timezoneBody, "TZID:",
								"\r\n");
					} finally {
						getMethod.abort();
					}
				}

				// delete temporary folder
				ExchangeSession.this.deleteFolder("davmailtemp");
			} catch (IOException e) {
				LOGGER.warn("Unable to get VTIMEZONE info: " + e, e);
			}
		}
	}

	protected VTimezone vTimezone;

	protected VTimezone getVTimezone() throws HttpException {
		if (vTimezone == null) {
			// need to load Timezone info from OWA
			vTimezone = new VTimezone();
			vTimezone.load();
		}
		return vTimezone;
	}

	/**
	 * Return internal HttpClient instance
	 * 
	 * @return http client
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}

}