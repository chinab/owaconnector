package com.owaconnector.exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

import com.owaconnector.exception.DavMailAuthenticationException;
import com.owaconnector.exception.DavMailException;

public abstract class AbstractExchangeLoginDelegate implements ExchangeLoginDelegate {

	private static final Logger LOGGER = Logger.getLogger(AbstractExchangeLoginDelegate.class);

	private final HttpClientFacade facade;

	private String username;
	private String password;

	public AbstractExchangeLoginDelegate(HttpClientFacade facade, String username, String password) {
		this.facade = facade;
		this.username = username;
		this.password = password;
	}

	protected HttpClientFacade getFacade() {
		return facade;
	}

	protected String getUsername() {
		return username;
	}

	protected String getPassword() {
		return password;
	}

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

	private static final String BASE_HREF = "<base href=\"";

	// TODO implement HTML cleaner?
	protected String getMailPath(InputStream inboxPage) throws IOException {
		String line;

		BufferedReader mainPageReader = new BufferedReader(new InputStreamReader(inboxPage));
		while ((line = mainPageReader.readLine()) != null
				&& line.toLowerCase().indexOf(BASE_HREF) == -1) {
		}
		if (line != null) {
			return StringUtil.getToken(line, "\"", "\"");
		} else {
			throw new IllegalStateException("Cannot find base href");
		}

	}

	public ExchangeProperties getWellKnownFolders(InputStream inboxPage) throws HttpException,
			URISyntaxException, IOException {
		String mailPath = getMailPath(inboxPage);
		ExchangeProperties props = new ExchangeProperties();
		// Retrieve well known URLs
		MultiStatusResponse[] responses = null;
		try {
			responses = getFacade().executePropFindMethod(URIUtil.encodePath(mailPath), 0,
					WELL_KNOWN_FOLDERS);
			if (responses == null || responses.length == 0) {
				throw new DavMailException("EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", props
						.getMailPath());
			}
			DavPropertySet properties = responses[0].getProperties(HttpStatus.SC_OK);
			props.setInboxUrl(DavPropertyUtil.getURIPropertyIfExists(properties, "inbox",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			props.setDeleteditemsUrl(DavPropertyUtil.getURIPropertyIfExists(properties,
					"deleteditems", ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			props.setSentitemsUrl(DavPropertyUtil.getURIPropertyIfExists(properties, "sentitems",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			props.setSendmsgUrl(DavPropertyUtil.getURIPropertyIfExists(properties, "sendmsg",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			props.setDraftsUrl(DavPropertyUtil.getURIPropertyIfExists(properties, "drafts",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			props.setCalendarUrl(DavPropertyUtil.getURIPropertyIfExists(properties, "calendar",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			props.setContactsUrl(DavPropertyUtil.getURIPropertyIfExists(properties, "contacts",
					ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
			LOGGER.debug(props.toString());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new DavMailAuthenticationException("EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", props
					.getMailPath());
		}
		if (props.validate())
			return props;
		else {
			throw new IllegalStateException("ExchangeProperties invalid");
		}
	}

	// FIXME REWRITE

	private String getMailPath(HttpUriRequest method, HttpResponse response) throws HttpException {
		// find base url
		String line;
		String mailPath = null;
		// get user mail URL from html body (multi frame)
		BufferedReader mainPageReader = null;
		try {
			mainPageReader = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			// noinspection StatementWithEmptyBody
			while ((line = mainPageReader.readLine()) != null
					&& line.toLowerCase().indexOf(BASE_HREF) == -1) {
			}
			if (line != null) {
				int start = line.toLowerCase().indexOf(BASE_HREF) + BASE_HREF.length();
				int end = line.indexOf('\"', start);
				String mailBoxBaseHref = line.substring(start, end);
				URL baseURL = new URL(mailBoxBaseHref);
				mailPath = baseURL.getPath();
				LOGGER.debug("Base href found in body, mailPath is " + mailPath);
				// buildEmail(baseURL.getHost(), baseURL.getPath());
				// LOGGER.debug("Current user email is " + email);
			} else {
				// failover for Exchange 2007 : build standard mailbox link with
				// email
				// FIXME
				// buildEmail(method.getURI().getHost(),
				// method.getURI().getPath());
				// setMailPath("/exchange/" + email + '/');
				// LOGGER.debug("Current user email is " + email +
				// ", mailPath is " + getMailPath());
			}
		} catch (IOException e) {
			LOGGER.error("Error parsing main page at " + method.getURI().getPath(), e);
		} finally {
			if (mainPageReader != null) {
				try {
					mainPageReader.close();
				} catch (IOException e) {
					LOGGER.error("Error parsing main page at " + method.getURI().getPath());
				}
			}
		}

		if (mailPath == null) {
			throw new DavMailAuthenticationException(
					"EXCEPTION_AUTHENTICATION_FAILED_PASSWORD_EXPIRED");
		}
		return mailPath;
	}

}
