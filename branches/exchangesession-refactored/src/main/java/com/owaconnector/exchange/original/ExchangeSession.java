package com.owaconnector.exchange.original;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

import com.owaconnector.exception.DavMailAuthenticationException;
import com.owaconnector.exception.DavMailException;
import com.owaconnector.exchange.DavPropertyUtil;
import com.owaconnector.exchange.ExchangeNamespace;

public class ExchangeSession {

	protected static final Logger LOGGER = Logger
			.getLogger(ExchangeSession.class);

	static final String MAILBOX_BASE = "cn=recipients/cn=";

	static final String DAV_UID_FILTER = "\"DAV:uid\"='";

	protected static final DavPropertyNameSet EVENT_REQUEST_PROPERTIES = new DavPropertyNameSet();

	static {
		EVENT_REQUEST_PROPERTIES.add(DavPropertyName.create("permanenturl",
				ExchangeNamespace.SCHEMAS_EXCHANGE));
		EVENT_REQUEST_PROPERTIES.add(DavPropertyName.GETETAG);
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
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		FOLDER_PROPERTIES.add(DavPropertyName.create("contenttag", Namespace
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

	private static final DavPropertyNameSet WELL_KNOWN_FOLDERS = new DavPropertyNameSet();

	static {
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("inbox",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("deleteditems",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("sentitems",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("sendmsg",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("drafts",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("calendar",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		WELL_KNOWN_FOLDERS.add(DavPropertyName.create("contacts",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
	}
	/**
	 * Base user mailboxes path (used to select folder)
	 */
	private String mailPath;
	private String email;
	private String alias;

	private final String userName;
	private final String password;

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

	private String hostName;
	private String authScheme;

	// private static int dumpIndex;
	private String defaultSound = "Basso";

	private final DavGatewayHttpClientFacade facade;

	public void setDefaultSound(String defaultSound) {
		this.defaultSound = defaultSound;
	}

	public String getDefaultSound() {
		return defaultSound;
	}

	private void setMailPath(String mailPath) {
		this.mailPath = mailPath;
	}

	public String getMailPath() {
		return mailPath;
	}

	public DavGatewayHttpClientFacade getFacade() {
		return facade;
	}

	public String getInboxUrl() {
		return inboxUrl;
	}

	private void setInboxUrl(String inboxUrl) {
		this.inboxUrl = inboxUrl;
	}

	public String getDeleteditemsUrl() {
		return deleteditemsUrl;
	}

	private void setDeleteditemsUrl(String deleteditemsUrl) {
		this.deleteditemsUrl = deleteditemsUrl;
	}

	public String getSentitemsUrl() {
		return sentitemsUrl;
	}

	private void setSentitemsUrl(String sentitemsUrl) {
		this.sentitemsUrl = sentitemsUrl;
	}

	public String getSendmsgUrl() {
		return sendmsgUrl;
	}

	private void setSendmsgUrl(String sendmsgUrl) {
		this.sendmsgUrl = sendmsgUrl;
	}

	public String getDraftsUrl() {
		return draftsUrl;
	}

	private void setDraftsUrl(String draftsUrl) {
		this.draftsUrl = draftsUrl;
	}

	public String getCalendarUrl() {
		return calendarUrl;
	}

	private void setCalendarUrl(String calendarUrl) {
		this.calendarUrl = calendarUrl;
	}

	public String getContactsUrl() {
		return contactsUrl;
	}

	private void setContactsUrl(String contactsUrl) {
		this.contactsUrl = contactsUrl;
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
			this.facade = new DavGatewayHttpClientFacade(url, this.userName,
					this.password);

			boolean isBasicAuthentication = isBasicAuthentication(url);
			// get webmail root url
			// providing credentials
			// manually follow redirect

			HttpGet method = new HttpGet(url);
			HttpPost postMethod = null;
			URI uri = method.getURI();
			this.hostName = uri.getHost();
			this.authScheme = uri.getScheme();
			HttpClientParams.setRedirecting(method.getParams(), false);
			HttpResponse response = facade.executeFollowRedirects(method);

			if (isBasicAuthentication) {
				int status = response.getStatusLine().getStatusCode();

				if (status == HttpStatus.SC_UNAUTHORIZED) {
					// response.abort();
					throw new DavMailAuthenticationException(
							"EXCEPTION_AUTHENTICATION_FAILED");
				} else if (status != HttpStatus.SC_OK) {
					// response.abort();
					throw facade.buildHttpException(null, response);
				}
			} else {
				FormLoginDelegate delegate = new FormLoginDelegate(
						this.userName, this.password, facade);
				response = delegate.doLogin(method, response);
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
			facade.executePropFindMethod(URIUtil.encodePath(inboxUrl), 0,
					DISPLAY_NAME);
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
	private boolean isBasicAuthentication(String url) throws IOException {
		return facade.getHttpStatus(url) == HttpStatus.SC_UNAUTHORIZED;
	}

	private static final String BASE_HREF = "<base href=\"";

	private void buildMailPath(HttpUriRequest method, HttpResponse response)
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
				setMailPath(baseURL.getPath());
				LOGGER.debug("Base href found in body, mailPath is "
						+ getMailPath());
				buildEmail(baseURL.getHost(), baseURL.getPath());
				LOGGER.debug("Current user email is " + email);
			} else {
				// failover for Exchange 2007 : build standard mailbox link with
				// email
				buildEmail(method.getURI().getHost(), method.getURI().getPath());
				setMailPath("/exchange/" + email + '/');
				LOGGER.debug("Current user email is " + email
						+ ", mailPath is " + getMailPath());
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

		if (getMailPath() == null || email == null) {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED_PASSWORD_EXPIRED");
		}
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
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public List<Event> getAllEvents(String folderPath, int caldavPastDelay)
			throws URISyntaxException, HttpException, ClientProtocolException,
			IOException {
		String dateCondition = "";
		if (caldavPastDelay != Integer.MAX_VALUE) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -caldavPastDelay);
			dateCondition = "                AND \"urn:schemas:calendar:dtstart\" > '"
					+ DateUtil.formatSearchDate(cal.getTime()) + "'\n";
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
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private List<Event> getEvents(String folderPath, String searchQuery)
			throws URISyntaxException, HttpException, ClientProtocolException,
			IOException {
		List<Event> events = new ArrayList<Event>();
		MultiStatusResponse[] responses = facade.executeSearchMethod(URIUtil
				.encodePath(folderPath), searchQuery);
		for (MultiStatusResponse response : responses) {
			events.add(new Event(response, this));
		}
		return events;
	}

	/**
	 * Get current Exchange alias name from login name
	 * 
	 * @return user name
	 */
	private String getAliasFromLogin() {
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
	private String getAliasFromMailPath() {
		if (getMailPath() == null) {
			return null;
		}
		int index = getMailPath().lastIndexOf('/', getMailPath().length() - 2);
		if (index >= 0 && getMailPath().endsWith("/")) {
			return getMailPath().substring(index + 1,
					getMailPath().length() - 1);
		} else {
			LOGGER.warn(new BundleMessage("EXCEPTION_INVALID_MAIL_PATH",
					getMailPath()));
			return null;
		}
	}

	/**
	 * Get user alias from mailbox display name over Webdav.
	 * 
	 * @return user alias
	 * @throws HttpException
	 */
	private String getAliasFromMailboxDisplayName() throws HttpException {
		if (getMailPath() == null) {
			return null;
		}
		String displayName = null;
		try {
			MultiStatusResponse[] responses = facade.executePropFindMethod(
					URIUtil.encodePath(getMailPath()), 0, DISPLAY_NAME);
			if (responses.length == 0) {
				LOGGER.warn(new BundleMessage(
						"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", getMailPath()));
			} else {
				displayName = DavPropertyUtil.getPropertyIfExists(responses[0]
						.getProperties(HttpStatus.SC_OK), "displayname",
						Namespace.getNamespace("DAV:"));
			}
		} catch (IOException e) {
			LOGGER.warn(new BundleMessage(
					"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", getMailPath()));
		}
		return displayName;
	}

	/**
	 * Build base path for cmd commands (galfind, gallookup). This does not work
	 * with freebusy, which requires /public/
	 * 
	 * @return cmd base path
	 */
	public String getCmdBasePath() {
		if (getMailPath() == null) {
			return "/public/";
		} else {
			return getMailPath();
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
	private String getEmailFromGlobalAddressList(String alias)
			throws DavMailException {
		String emailResult = null;
		if (alias != null) {
			HttpGet getMethod = null;
			String path = null;
			try {
				path = getBaseUrl() + getCmdBasePath() + "?Cmd=galfind&AN="
						+ URIUtil.encodePath(alias);
				getMethod = new HttpGet(path);
				HttpResponse response = facade
						.executeGetMethod(getMethod, true);
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

	public String getBaseUrl() {
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
		email = getEmailFromGlobalAddressList(alias);
		// failover: use mailbox name as alias
		if (email == null) {
			alias = getAliasFromMailPath();
			email = getEmailFromGlobalAddressList(alias);
		}
		// another failover : get alias from mailPath display name
		if (email == null) {
			alias = getAliasFromMailboxDisplayName();
			email = getEmailFromGlobalAddressList(alias);
		}
		if (email == null) {
			// failover : get email from Exchange 2007 Options page
			alias = getAliasFromOptions(methodPath);
			email = getEmailFromGlobalAddressList(alias);
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

	private String getAliasFromOptions(String path) throws DavMailException {
		String result = null;
		// get user mail URL from html body
		BufferedReader optionsPageReader = null;
		HttpGet optionsMethod = new HttpGet(path + "?ae=Options&t=About");
		try {
			HttpResponse response = facade.executeGetMethod(optionsMethod,
					false);
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

	private String getEmailFromOptions(String path) throws DavMailException {
		String result = null;
		// get user mail URL from html body
		BufferedReader optionsPageReader = null;
		HttpGet optionsMethod = new HttpGet(path + "?ae=Options&t=About");
		try {
			HttpResponse response = facade.executeGetMethod(optionsMethod,
					false);
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

	public void getWellKnownFolders() throws HttpException, URISyntaxException {
		// Retrieve well known URLs
		MultiStatusResponse[] responses = null;
		try {
			responses = facade.executePropFindMethod(getBaseUrl()
					+ URIUtil.encodePath(getMailPath()), 0, WELL_KNOWN_FOLDERS);
			if (responses == null || responses.length == 0) {
				throw new DavMailException(
						"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", getMailPath());
			}
			DavPropertySet properties = responses[0]
					.getProperties(HttpStatus.SC_OK);
			setInboxUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"inbox", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			setDeleteditemsUrl(DavPropertyUtil.getURIPropertyIfExists(
					properties, "deleteditems",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			setSentitemsUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"sentitems", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			setSendmsgUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"sendmsg", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			setDraftsUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"drafts", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			setCalendarUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"calendar", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			setContactsUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"contacts", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			LOGGER.debug("Inbox URL : " + getInboxUrl() + " Trash URL : "
					+ getDeleteditemsUrl() + " Sent URL : " + getSentitemsUrl()
					+ " Send URL : " + getSendmsgUrl() + " Drafts URL : "
					+ getDraftsUrl() + " Calendar URL : " + getCalendarUrl()
					+ " Contacts URL : " + getContactsUrl());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new DavMailAuthenticationException(
					"EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", getMailPath());
		}
	}

	private VTimezone vTimezone;

	public VTimezone getVTimezone() throws HttpException {
		if (vTimezone == null) {
			// need to load Timezone info from OWA
			vTimezone = new VTimezone(this);
			vTimezone.load();
		}
		return vTimezone;
	}
}
