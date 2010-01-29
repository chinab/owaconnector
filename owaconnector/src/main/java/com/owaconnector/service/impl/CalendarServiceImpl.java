package com.owaconnector.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.exception.NoCalendarFoundException;
import com.owaconnector.service.CalendarService;

import davmail.exception.DavMailException;
import davmail.exchange.ExchangeSession;
import davmail.exchange.ExchangeSessionFactory;
import davmail.util.StringUtil;

@Service
public class CalendarServiceImpl implements CalendarService {

	@com.owaconnector.logger.Logger 
	private org.apache.log4j.Logger log;
	
    public StringBuilder getCalendar(CalendarConfiguration config, String decryptedPassword) throws NoCalendarFoundException {
        Assert.notNull(config,"CalendarConfiguration cannot be null");
        Assert.notNull(decryptedPassword,"DecryptedPassword cannot be null");
        
        StringBuilder calendar = null;
        try {
            List<ExchangeSession.Event> events = getEvents(config, decryptedPassword);
            if (events != null && events.size() > 0) {
                calendar = createCalendar(events);
            }
        } catch (IOException e) {
        	log.error("Error in getCalendar: ",e);
        	throw new NoCalendarFoundException(e);
        } catch (URISyntaxException e) {
            throw new NoCalendarFoundException(e);
        } catch (DavMailException e) {
        	log.error("Error in getCalendar: ",e);
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
    private List<ExchangeSession.Event> getEvents(CalendarConfiguration config, String decryptedPassword) throws IOException, URISyntaxException, HttpException {
        String username = config.getDomainName() + "\\" + config.getUsername();
        ExchangeSession session = ExchangeSessionFactory.getInstance(config.getURL(), username, decryptedPassword);
        String folderPath = session.getFolderPath("calendar");
        return session.getAllEvents(folderPath, config.getMaxDaysInPast());
    }

    private StringBuilder createCalendar(List<ExchangeSession.Event> events) throws IOException, HttpException {
        if (events == null) {
            throw new IllegalArgumentException("Events cannot be null");
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("BEGIN:VCALENDAR");
        for (ExchangeSession.Event event : events) {
            String icsContent = StringUtil.getToken(event.getICS(), "BEGIN:VCALENDAR", "END:VCALENDAR");
            buffer.append(icsContent);
        }
        buffer.append("END:VCALENDAR");
        return buffer;
    }
}
