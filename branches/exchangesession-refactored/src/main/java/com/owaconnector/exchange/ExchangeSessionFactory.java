package com.owaconnector.exchange;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.UnknownHttpStatusException;

public class ExchangeSessionFactory {

	public static ExchangeSession getInstance(String url, String username, String password)
			throws IOException, AuthenticationFailedException, UnknownHttpStatusException,
			HttpException, URISyntaxException {

		return new ExchangeSession(url, username, password);
	}

}
