package com.owaconnector.exchange.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

import com.owaconnector.exception.DavMailAuthenticationException;
import com.owaconnector.exception.DavMailException;
import com.owaconnector.exchange.ClientFacade;
import com.owaconnector.exchange.ExchangeNamespace;
import com.owaconnector.exchange.ExchangeProperties;
import com.owaconnector.exchange.util.DavPropertyUtil;
import com.owaconnector.exchange.util.StringUtil;

public abstract class AbstractExchangeLoginDelegate implements ExchangeLoginDelegate {

	private static final Logger LOGGER = Logger.getLogger(AbstractExchangeLoginDelegate.class);

	private String username;
	private String password;
	private ClientFacade facade;
	private ExchangeProperties props;

	public AbstractExchangeLoginDelegate(ClientFacade facade, ExchangeProperties props,
			String username, String password) {
		this.username = username;
		this.password = password;
		this.facade = facade;
		this.props = props;
	}

	protected ClientFacade getFacade() {
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

}
