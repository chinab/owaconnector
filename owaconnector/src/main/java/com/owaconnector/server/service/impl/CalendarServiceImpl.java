package com.owaconnector.server.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.springframework.stereotype.Service;

import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.server.service.CalendarService;

import davmail.exception.DavMailException;
import davmail.exchange.ExchangeSession;
import davmail.exchange.ExchangeSessionFactory;
import davmail.util.StringUtil;

@Service
public class CalendarServiceImpl implements CalendarService {
	
	public StringBuilder getCalendar(CalendarConfiguration config,
			String decryptedPassword) {
		if (config == null || decryptedPassword == null)
			throw new IllegalStateException();

		try {
			List<ExchangeSession.Event> events = getEvents(config,
					decryptedPassword);
			return createCalendar(events);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private List<ExchangeSession.Event> getEvents(CalendarConfiguration config,
			String decryptedPassword) throws IOException, DavMailException,
			URISyntaxException, HttpException {

		ExchangeSession session = ExchangeSessionFactory.getInstance(config
				.getUrl(), config.getUsername(), decryptedPassword);
		String folderPath = session.getFolderPath("CALENDAR");
		return session.getAllEvents(folderPath);
	}

	private StringBuilder createCalendar(List<ExchangeSession.Event> events)
			throws IOException, HttpException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("BEGIN:VCALENDAR");
		for (ExchangeSession.Event event : events) {
			String icsContent = StringUtil.getToken(event.getICS(),
					"BEGIN:VCALENDAR", "END:VCALENDAR");
			buffer.append(icsContent);
		}
		buffer.append("END:VCALENDAR");
		return buffer;
	}
}
