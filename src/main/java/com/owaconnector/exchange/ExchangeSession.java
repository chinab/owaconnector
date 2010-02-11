package com.owaconnector.exchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.DavMailAuthenticationException;
import com.owaconnector.exception.DavMailException;
import com.owaconnector.exception.UnknownHttpStatusException;
import com.owaconnector.exchange.entity.Event;
import com.owaconnector.exchange.login.BasicAuthenticationLoginDelegate;
import com.owaconnector.exchange.login.ExchangeLoginDelegate;
import com.owaconnector.exchange.login.FormLoginDelegate;
import com.owaconnector.exchange.util.DateUtil;

public class ExchangeSession {

	private final static Logger LOG = Logger.getLogger(ExchangeSession.class);

	private ClientFacade facade;

	private ExchangeProperties props;

	public ExchangeSession(ClientFacade facade, ExchangeProperties props) throws IOException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException {
		this.facade = facade;
		this.props = props;

	}

	public void login(String url, String username, String password)
			throws DavMailAuthenticationException, DavMailException, IOException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException {
		boolean isBasicAuthentication = isBasicAuthentication(url);
		ExchangeLoginDelegate loginDelegate;
		if (isBasicAuthentication) {
			// basic authentication delegate
			loginDelegate = new BasicAuthenticationLoginDelegate(facade, props, username, password);
			// loginDelegate = factory.createDelegate(DelegateType.Basic,
			// username, password);
		} else {
			// form login delegate
			loginDelegate = new FormLoginDelegate(facade, props, username, password);
			// loginDelegate = factory.createDelegate(DelegateType.Form,
			// username, password);

		}
		this.props = loginDelegate.doLogin(url);
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

	public List<Event> getAllEvents(String folderPath, int caldavPastDelay)
			throws URISyntaxException, HttpException, ClientProtocolException, IOException {
		String dateCondition = "";
		if (caldavPastDelay != Integer.MAX_VALUE) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -caldavPastDelay);
			dateCondition = "                AND \"urn:schemas:calendar:dtstart\" > '"
					+ DateUtil.formatSearchDate(cal.getTime()) + "'\n";
		}

		String searchQuery = "Select \"DAV:getetag\", \"http://schemas.microsoft.com/exchange/permanenturl\" "
				+ "FROM Scope('SHALLOW TRAVERSAL OF \""
				+ folderPath
				+ "\"')\n"
				+ "WHERE ("
				// VTODO events have a null instancetype
				+ "\"urn:schemas:calendar:instancetype\" is null OR "
				+ "\"urn:schemas:calendar:instancetype\" = 1\n"
				+ "OR (\"urn:schemas:calendar:instancetype\" = 0\n"
				+ dateCondition
				+ ")) AND \"DAV:contentclass\" = 'urn:content-classes:appointment'\n"
				+ "ORDER BY \"urn:schemas:calendar:dtstart\" DESC\n";
		return getEvents(folderPath, searchQuery);
	}

	private List<Event> getEvents(String folderPath, String searchQuery) throws URISyntaxException,
			HttpException, ClientProtocolException, IOException {
		List<Event> events = new ArrayList<Event>();

		MultiStatusResponse[] responses = facade.executeSearchMethod(
				URIUtil.encodePath(folderPath), searchQuery);
		for (MultiStatusResponse response : responses) {
			events.add(new Event(response));
		}
		return events;
	}

	// TODO rewrite
	public String getEvent(Event event) throws DavMailException {
		String result = null;
		LOG.debug("Get event: " + event.getPermanentUrl());
		HttpGet method = new HttpGet(event.getPermanentUrl());
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type", "text/xml; charset=utf-8"));
		headers.add(new BasicHeader("Translate", "f"));
		try {
			InputStream response = facade.executeGet(event.getPermanentUrl(), false, headers);
			// HttpResponse response = response;

			MimeMessage mimeMessage = new MimeMessage(null, response);
			Object mimeBody = mimeMessage.getContent();
			MimePart bodyPart;
			if (mimeBody instanceof MimeMultipart) {
				bodyPart = getCalendarMimePart((MimeMultipart) mimeBody);
			} else {
				// no multipart, single body
				bodyPart = mimeMessage;
			}

			if (bodyPart == null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				mimeMessage.getDataHandler().writeTo(baos);
				baos.close();
				throw new DavMailException("EXCEPTION_INVALID_MESSAGE_CONTENT", new String(baos
						.toByteArray(), "UTF-8"));
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bodyPart.getDataHandler().writeTo(baos);
			baos.close();
			result = new String(baos.toByteArray(), "UTF-8");
		} catch (IOException e) {
			LOG.warn("Unable to get event at " + event.getPermanentUrl() + ": " + e.getMessage());
		} catch (MessagingException e) {
			LOG.warn("Unable to get event at " + event.getPermanentUrl() + ": " + e.getMessage());
		} finally {
			method.abort();
		}
		return result;
	}

	// TODO Rewrite
	private MimePart getCalendarMimePart(MimeMultipart multiPart) throws IOException,
			MessagingException {
		MimePart bodyPart = null;
		for (int i = 0; i < multiPart.getCount(); i++) {
			String contentType = multiPart.getBodyPart(i).getContentType();
			if (contentType.startsWith("text/calendar")
					|| contentType.startsWith("application/ics")) {
				bodyPart = (MimePart) multiPart.getBodyPart(i);
				break;
			} else if (contentType.startsWith("multipart")) {
				Object content = multiPart.getBodyPart(i).getContent();
				if (content instanceof MimeMultipart) {
					bodyPart = getCalendarMimePart((MimeMultipart) content);
				}
			}
		}

		return bodyPart;
	}

}
