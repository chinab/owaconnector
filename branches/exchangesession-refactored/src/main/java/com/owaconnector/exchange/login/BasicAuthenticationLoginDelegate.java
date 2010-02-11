package com.owaconnector.exchange.login;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.UnknownHttpStatusException;
import com.owaconnector.exchange.ClientFacade;
import com.owaconnector.exchange.ExchangeProperties;

public class BasicAuthenticationLoginDelegate extends AbstractExchangeLoginDelegate {

	public BasicAuthenticationLoginDelegate(ClientFacade facade, ExchangeProperties props,
			String username, String password) {
		super(facade, props, username, password);
	}

	public ExchangeProperties doLogin(String url) throws IOException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException {
		getFacade().setAuthentication(getUsername(), getPassword());
		InputStream inboxPage = getFacade().executeGet(url, true);
		return getWellKnownFolders(inboxPage);
	}
}
