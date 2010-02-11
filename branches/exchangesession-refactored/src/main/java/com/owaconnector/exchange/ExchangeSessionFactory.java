package com.owaconnector.exchange;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.UnknownHttpStatusException;

@Component
public class ExchangeSessionFactory {

	@Autowired
	private ClientFacade facade;

	@Autowired
	private ExchangeProperties props;

	public ExchangeSession getInstance(String url, String username, String password)
			throws IOException, AuthenticationFailedException, UnknownHttpStatusException,
			HttpException, URISyntaxException {
		ExchangeSession session = new ExchangeSession(facade, props);
		session.login(url, username, password);
		return session;
	}
}
