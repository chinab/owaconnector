package com.owaconnector.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.Calendars;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.exception.NoCalendarFoundException;
import com.owaconnector.service.CalendarService;

import davmail.exception.DavMailException;
import davmail.exchange.Event;
import davmail.exchange.ExchangeSession;
import davmail.exchange.ExchangeSessionFactory;

@Service
public class CalendarServiceImpl implements CalendarService {

	// @com.owaconnector.logger.Logger
	// private org.apache.log4j.Logger log;
	//	
	private final static Logger log = Logger
			.getLogger(CalendarServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.service.CalendarService#getCalendar(com.owaconnector
	 * .domain.CalendarConfiguration, java.lang.String)
	 */
	public StringBuilder getCalendar(CalendarConfiguration config,
			String decryptedPassword) throws NoCalendarFoundException {
		Assert.notNull(config, "CalendarConfiguration cannot be null");
		Assert.notNull(decryptedPassword, "DecryptedPassword cannot be null");

		StringBuilder calendar = null;
		try {
			List<Event> events = getEvents(config, decryptedPassword);
			if (events != null && events.size() > 0) {
				calendar = createCalendar(events);
			}
		} catch (IOException e) {
			log.error("Error in getCalendar: ", e);
			throw new NoCalendarFoundException(e);
		} catch (URISyntaxException e) {
			throw new NoCalendarFoundException(e);
		} catch (DavMailException e) {
			log.error("Error in getCalendar: ", e);
			throw new NoCalendarFoundException(e);
		} catch (HttpException e) {
			throw new NoCalendarFoundException(e);
		}
		return calendar;
	}

	/**
	 * Get all events for a specific CalendarConfiguration with supplied
	 * password.
	 * 
	 * @param config
	 *            The CalendarConfiguration to access the calendar.
	 * @param decryptedPassword
	 *            The decrypted password to access the calendar.
	 * @return List<ExchangeSession.Event> list of events in the calendar.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	private List<Event> getEvents(CalendarConfiguration config,
			String decryptedPassword) throws IOException, URISyntaxException,
			HttpException {
		Assert.notNull(config, "config cannot be null");
		Assert.notNull(decryptedPassword, "decryptedPassword cannot be null");

		String username = config.getDomainName() + "\\" + config.getUsername();
		ExchangeSession session = ExchangeSessionFactory.getInstance(config
				.getURL(), username, decryptedPassword);
		String folderPath = session.getFolderPath("calendar");
		return session.getAllEvents(folderPath, config.getMaxDaysInPast());
	}

	/**
	 * Create Calendar from a list of ExchangeSession.Events. <br/>
	 * Filter out VALARM because Google Calendar does not understand this. <br/>
	 * Remove VCALENDAR begin and end tag from each individual ICS, because
	 * these tags should be applied before and after the <b>entire</b> calendar,
	 * not the <b>individual</b> items.
	 * 
	 * @param events
	 *            List of ExchangeSession events from which the calendar should
	 *            be constructed.
	 * @return StringBuilder calendar as StringBuilder
	 * @throws IOException
	 * @throws HttpException
	 */
	private StringBuilder createCalendar(List<Event> events)
			throws IOException, HttpException {

		Assert.notNull(events, "Events cannot be null");

		// obtain ICS from Exchange and merge events into one ical4J calendar.
		Calendar finalCalendar = merge(events, new CalendarBuilder());

		// output ical4j calendar as ICS.
		return output(finalCalendar);

	}

	private StringBuilder output(Calendar finalCalendar) throws IOException,
			UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CalendarOutputter outputter = new CalendarOutputter();
		try {
			outputter.setValidating(false);
			outputter.output(finalCalendar, baos);
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new StringBuilder(baos.toString("UTF-8"));
	}

	private Calendar merge(List<Event> events, CalendarBuilder builder)
			throws IOException, HttpException {
		Calendar finalCalendar = new Calendar();
		for (Event event : events) {
			// first obtain the complete ics from the event
			String ics = event.getICS();
			try {
				Calendar calendar = builder.build(new StringReader(ics));
				finalCalendar = Calendars.merge(finalCalendar, calendar);
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return finalCalendar;
	}
}
