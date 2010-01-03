package com.owaconnector.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.junit.Assert;
import org.springframework.stereotype.Service;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.service.CalendarService;

import davmail.exception.DavMailException;
import davmail.exchange.ExchangeSession;
import davmail.exchange.ExchangeSessionFactory;
import davmail.util.StringUtil;

@Service
public class CalendarServiceImpl implements CalendarService {

	public StringBuilder getCalendar(CalendarConfiguration config,
			String decryptedPassword) {
		Assert.assertNotNull("CalendarConfiguration cannot be null", config);
		Assert.assertNotNull("Password cannot be null", decryptedPassword);

		StringBuilder calendar = null;

		try {
			List<ExchangeSession.Event> events = getEvents(config,
					decryptedPassword);
			if (events != null && events.size() > 0) {
				calendar = createCalendar(events);
			}
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
		return calendar;

	}

	private List<ExchangeSession.Event> getEvents(CalendarConfiguration config,
			String decryptedPassword) throws IOException, DavMailException,
			URISyntaxException, HttpException {
		Assert.assertNotNull("CalendarConfiguration cannot be null", config);
		Assert.assertNotNull("Password cannot be null", decryptedPassword);

		String username = config.getDomainName() + "\\" + config.getUsername();
		ExchangeSession session = ExchangeSessionFactory.getInstance(config
				.getURL(), username, decryptedPassword);
		String folderPath = session.getFolderPath("CALENDAR");
		return session.getAllEvents(folderPath, config.getMaxDaysInPast());
	}

	private StringBuilder createCalendar(List<ExchangeSession.Event> events)
			throws IOException, HttpException {
		Assert.assertNotNull("Events cannot be null", events);

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
