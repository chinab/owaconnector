package com.owaconnector.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.UnknownHttpStatusException;

public class BasicAuthenticationLoginDelegate extends AbstractExchangeLoginDelegate {

	public BasicAuthenticationLoginDelegate(HttpClientFacade facade, String username,
			String password) {
		super(facade, username, password);

	}

	public ExchangeProperties doLogin(String url) throws IOException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException {
		// TODO set authentication
		getFacade().setAuthentication(getUsername(), getPassword());
		InputStream inboxPage = getFacade().executeGet(url, true);
		return getWellKnownFolders(inboxPage);
	}
}
