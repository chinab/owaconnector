package com.owaconnector.exchange;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.UnknownHttpStatusException;
import com.owaconnector.exchange.original.Event;

public class ExchangeSession {

	private ClientFacade facade;
	ExchangeProperties exchangeProperties;

	protected ClientFacade getFacade() {
		return facade;
	}

	private void setFacade(ClientFacade facade) {
		this.facade = facade;
	}

	protected ExchangeProperties getExchangeProperties() {
		return this.exchangeProperties;
	}

	public ExchangeSession(String url, String username, String password) throws IOException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException {
		HttpClientFacade facade = new HttpClientFacade();
		setFacade(facade);
		boolean isBasicAuthentication = isBasicAuthentication(url);
		ExchangeLoginDelegate loginDelegate;
		if (isBasicAuthentication) {
			// basic authentication delegate
			loginDelegate = new BasicAuthenticationLoginDelegate(facade, username, password);
		} else {
			// form login delegate
			// facade.removeAuthentication();
			loginDelegate = new FormLoginDelegate(facade, username, password);
		}
		loginDelegate.doLogin(url);

		// setExchangeProperties(exchangeProperties);

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
	private boolean isBasicAuthentication(String url) {
		return facade.getHttpStatus(url) == HttpStatus.SC_UNAUTHORIZED;
	}

	public String getCalendarUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Event> getAllEvents(String folderPath, Integer maxDaysInPast) {
		// TODO Auto-generated method stub
		return null;
	}

}
