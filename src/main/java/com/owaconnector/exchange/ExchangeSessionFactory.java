package com.owaconnector.exchange;

import java.io.IOException;

import org.springframework.stereotype.Component;

import davmail.exchange.ExchangeSession;

@Component
public class ExchangeSessionFactory {

	public ExchangeSession getInstance(String url, String username,
			String password) throws IOException {

		return davmail.exchange.ExchangeSessionFactory.getInstance(url,
				username, password);

	}
}
