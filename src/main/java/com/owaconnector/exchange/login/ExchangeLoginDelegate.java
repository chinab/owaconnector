package com.owaconnector.exchange.login;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.DavMailException;
import com.owaconnector.exception.UnknownHttpStatusException;
import com.owaconnector.exchange.ExchangeProperties;

public interface ExchangeLoginDelegate {

	ExchangeProperties doLogin(String url) throws IOException, DavMailException,
			com.owaconnector.exception.DavMailAuthenticationException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException;

}
