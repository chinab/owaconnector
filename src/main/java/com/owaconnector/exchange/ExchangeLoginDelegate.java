package com.owaconnector.exchange;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.DavMailException;
import com.owaconnector.exception.UnknownHttpStatusException;

public interface ExchangeLoginDelegate {

	ExchangeProperties doLogin(String url) throws IOException, DavMailException,
			com.owaconnector.exception.DavMailAuthenticationException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException;

}
