package com.owaconnector.exchange.original;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

import com.owaconnector.exception.DavMailException;
import com.owaconnector.exchange.DavPropertyUtil;
import com.owaconnector.exchange.ExchangeNamespace;

/**
 * Calendar event object
 */
public class Event {

	private static final Logger LOGGER = Logger.getLogger(Event.class);

	/**
	 * 
	 */
	private final ExchangeSession exchangeSession;
	private final DavGatewayHttpClientFacade facade;

	public Event(MultiStatusResponse response, ExchangeSession exchangeSession) {

		this.exchangeSession = exchangeSession;
		this.facade = exchangeSession.getFacade();
		setHref(URIUtil.decodePath(response.getHref()));
		setPermanentUrl(DavPropertyUtil.getPropertyIfExists(response
				.getProperties(HttpStatus.SC_OK), "permanenturl",
				ExchangeNamespace.SCHEMAS_EXCHANGE));
		setEtag(DavPropertyUtil.getPropertyIfExists(response
				.getProperties(HttpStatus.SC_OK), "getetag", Namespace
				.getNamespace("DAV:")));

	}

	public String getHref() {
		return href;
	}

	private void setHref(String href) {
		this.href = href;
	}

	public String getPermanentUrl() {
		return permanentUrl;
	}

	private void setPermanentUrl(String permanentUrl) {
		this.permanentUrl = permanentUrl;
	}

	public String getContentClass() {
		return contentClass;
	}

	public String getNoneMatch() {
		return noneMatch;
	}

	private void setEtag(String etag) {
		this.etag = etag;
	}

	private String href;
	private String permanentUrl;
	private String etag;
	private String contentClass;
	private String noneMatch;

	public static final int FREE_BUSY_INTERVAL = 15;

	private MimePart getCalendarMimePart(MimeMultipart multiPart)
			throws IOException, MessagingException {
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

	/**
	 * Load ICS content from Exchange server. User Translate: f header to get
	 * MIME event content and get ICS attachment from it
	 * 
	 * @return ICS (iCalendar) event
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public String getICS() throws IOException, HttpException {
		String result = null;
		LOGGER.debug("Get event: " + permanentUrl);
		HttpGet method = new HttpGet(permanentUrl);
		method.setHeader("Content-Type", "text/xml; charset=utf-8");
		method.setHeader("Translate", "f");
		try {
			HttpResponse response = facade.executeGetMethod(method, false);

			MimeMessage mimeMessage = new MimeMessage(null, response
					.getEntity().getContent());
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
				throw new DavMailException("EXCEPTION_INVALID_MESSAGE_CONTENT",
						new String(baos.toByteArray(), "UTF-8"));
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bodyPart.getDataHandler().writeTo(baos);
			baos.close();
			result = fixICS(new String(baos.toByteArray(), "UTF-8"), true);
		} catch (IOException e) {
			LOGGER.warn("Unable to get event at " + permanentUrl + ": "
					+ e.getMessage());
		} catch (MessagingException e) {
			LOGGER.warn("Unable to get event at " + permanentUrl + ": "
					+ e.getMessage());
		} finally {
			method.abort();
		}
		return result;
	}

	/**
	 * Get event name (file name part in URL).
	 * 
	 * @return event name
	 */
	public String getName() {
		int index = href.lastIndexOf('/');
		if (index >= 0) {
			return href.substring(index + 1);
		} else {
			return href;
		}
	}

	/**
	 * Get event etag (last change tag).
	 * 
	 * @return event etag
	 */
	public String getEtag() {
		return etag;
	}

	private String fixTimezoneId(String line, String validTimezoneId) {
		return StringUtil.replaceToken(line, "TZID=", ":", validTimezoneId);
	}

	private void splitExDate(ICSBufferedWriter result, String line) {
		int cur = line.lastIndexOf(':') + 1;
		String start = line.substring(0, cur);

		for (int next = line.indexOf(',', cur); next != -1; next = line
				.indexOf(',', cur)) {
			String val = line.substring(cur, next);
			result.writeLine(start + val);

			cur = next + 1;
		}

		result.writeLine(start + line.substring(cur));
	}

	private String getAllDayLine(String line) throws IOException, HttpException {
		int keyIndex = line.indexOf(';');
		int valueIndex = line.lastIndexOf(':');
		int valueEndIndex = line.lastIndexOf('T');
		if (valueIndex < 0 || valueEndIndex < 0) {
			throw new DavMailException("EXCEPTION_INVALID_ICS_LINE", line);
		}
		String dateValue = line.substring(valueIndex + 1, valueEndIndex);
		String key = line.substring(0, Math.max(keyIndex, valueIndex));
		return key + ";VALUE=DATE:" + dateValue;
	}

	private String fixICS(String icsBody, boolean fromServer)
			throws IOException, HttpException {
		// first pass : detect
		class AllDayState {
			boolean isAllDay;
			boolean hasCdoAllDay;
			boolean isCdoAllDay;
		}

		// ExchangeSession.dumpIndex++;

		// Convert event class from and to iCal
		// See
		// https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-privateevents.txt
		boolean isAppleiCal = false;
		boolean hasAttendee = false;
		boolean hasCdoBusyStatus = false;
		// detect ics event with empty timezone (all day from Lightning)
		boolean hasTimezone = false;
		String transp = null;
		String validTimezoneId = null;
		String eventClass = null;
		String organizer = null;
		String action = null;
		boolean sound = false;

		List<AllDayState> allDayStates = new ArrayList<AllDayState>();
		AllDayState currentAllDayState = new AllDayState();
		BufferedReader reader = null;
		reader = new ICSBufferedReader(new StringReader(icsBody));
		try {

			String line;
			while ((line = reader.readLine()) != null) {
				int index = line.indexOf(':');
				if (index >= 0) {
					String key = line.substring(0, index);
					String value = line.substring(index + 1);
					if ("DTSTART;VALUE=DATE".equals(key)) {
						currentAllDayState.isAllDay = true;
					} else if ("X-MICROSOFT-CDO-ALLDAYEVENT".equals(key)) {
						currentAllDayState.hasCdoAllDay = true;
						currentAllDayState.isCdoAllDay = "TRUE".equals(value);
					} else if ("END:VEVENT".equals(line)) {
						allDayStates.add(currentAllDayState);
						currentAllDayState = new AllDayState();
					} else if ("PRODID".equals(key) && line.contains("iCal")) {
						// detect iCal created events
						isAppleiCal = true;
					} else if (isAppleiCal
							&& "X-CALENDARSERVER-ACCESS".equals(key)) {
						eventClass = value;
					} else if (!isAppleiCal && "CLASS".equals(key)) {
						eventClass = value;
					} else if ("ACTION".equals(key)) {
						action = value;
					} else if ("ATTACH;VALUES=URI".equals(key)) {
						// This is a marker that this event has an alarm
						// with sound
						sound = true;
						// Set the default sound to whatever this event
						// contains
						// (under assumption that the user has the same
						// sound set
						// for all events)
						this.exchangeSession.setDefaultSound(value);
					} else if (key.startsWith("ORGANIZER")) {
						if (value.startsWith("MAILTO:")) {
							organizer = value.substring(7);
						} else {
							organizer = value;
						}
					} else if (key.startsWith("ATTENDEE")) {
						hasAttendee = true;
					} else if ("TRANSP".equals(key)) {
						transp = value;
					} else if (line.startsWith("TZID:(GMT") ||
					// additional test for Outlook created recurring events
							line.startsWith("TZID:GMT ")) {
						try {
							validTimezoneId = ResourceBundle.getBundle(
									"timezones").getString(value);
						} catch (MissingResourceException mre) {
							LOGGER.warn(new BundleMessage(
									"LOG_INVALID_TIMEZONE", value));
						}
					} else if ("X-MICROSOFT-CDO-BUSYSTATUS".equals(key)) {
						hasCdoBusyStatus = true;
					} else if ("BEGIN:VTIMEZONE".equals(line)) {
						hasTimezone = true;
					}
				}
			}
		} finally {
			reader.close();
		}
		// second pass : fix
		int count = 0;
		ICSBufferedWriter result = new ICSBufferedWriter();
		try {
			reader = new ICSBufferedReader(new StringReader(icsBody));
			String line;

			while ((line = reader.readLine()) != null) {
				// remove empty properties
				if ("CLASS:".equals(line) || "LOCATION:".equals(line)) {
					continue;
				}
				// fix invalid exchange timezoneid
				if (validTimezoneId != null && line.indexOf(";TZID=") >= 0) {
					line = fixTimezoneId(line, validTimezoneId);
				}
				if (!fromServer && "BEGIN:VEVENT".equals(line) && !hasTimezone) {
					result.write(exchangeSession.getVTimezone()
							.getTimezoneBody());
					hasTimezone = true;
				}
				if (!fromServer && currentAllDayState.isAllDay
						&& "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE".equals(line)) {
					line = "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE";
				} else if (!fromServer && "END:VEVENT".equals(line)) {
					if (!hasCdoBusyStatus) {
						result.writeLine("X-MICROSOFT-CDO-BUSYSTATUS:"
								+ (!"TRANSPARENT".equals(transp) ? "BUSY"
										: "FREE"));
					}
					if (currentAllDayState.isAllDay
							&& !currentAllDayState.hasCdoAllDay) {
						result.writeLine("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE");
					}
					// add organizer line to all events created in Exchange
					// for active sync
					if (organizer == null) {
						result.writeLine("ORGANIZER:MAILTO:"
								+ this.exchangeSession.getEmail());
					}
				} else if (!fromServer
						&& line.startsWith("X-MICROSOFT-CDO-BUSYSTATUS:")) {
					line = "X-MICROSOFT-CDO-BUSYSTATUS:"
							+ (!"TRANSPARENT".equals(transp) ? "BUSY" : "FREE");
				} else if (!fromServer && !currentAllDayState.isAllDay
						&& "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE".equals(line)) {
					line = "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE";
				} else if (fromServer && currentAllDayState.isCdoAllDay
						&& line.startsWith("DTSTART")
						&& !line.startsWith("DTSTART;VALUE=DATE")) {
					line = getAllDayLine(line);
				} else if (fromServer && currentAllDayState.isCdoAllDay
						&& line.startsWith("DTEND")
						&& !line.startsWith("DTEND;VALUE=DATE")) {
					line = getAllDayLine(line);
				} else if (!fromServer && currentAllDayState.isAllDay
						&& line.startsWith("DTSTART")
						&& line.startsWith("DTSTART;VALUE=DATE")) {
					line = "DTSTART;TZID=\""
							+ this.exchangeSession.getVTimezone()
									.getTimezoneId() + "\":"
							+ line.substring(19) + "T000000";
				} else if (!fromServer && currentAllDayState.isAllDay
						&& line.startsWith("DTEND")
						&& line.startsWith("DTEND;VALUE=DATE")) {
					line = "DTEND;TZID=\""
							+ this.exchangeSession.getVTimezone()
									.getTimezoneId() + "\":"
							+ line.substring(17) + "T000000";
				} else if (line.startsWith("TZID:") && validTimezoneId != null) {
					line = "TZID:" + validTimezoneId;
				} else if ("BEGIN:VEVENT".equals(line)) {
					currentAllDayState = allDayStates.get(count++);
				} else if (line.startsWith("X-CALENDARSERVER-ACCESS:")) {
					if (!isAppleiCal) {
						continue;
					} else {
						if ("CONFIDENTIAL".equalsIgnoreCase(eventClass)) {
							result.writeLine("CLASS:PRIVATE");
						} else if ("PRIVATE".equalsIgnoreCase(eventClass)) {
							result.writeLine("CLASS:CONFIDENTIAL");
						} else {
							result.writeLine("CLASS:" + eventClass);
						}
					}
				} else if (line.startsWith("EXDATE;TZID=")
						|| line.startsWith("EXDATE:")) {
					// Apple iCal doesn't support EXDATE with multiple
					// exceptions
					// on one line. Split into multiple EXDATE entries
					// (which is
					// also legal according to the caldav standard).
					splitExDate(result, line);
					continue;
				} else if (line.startsWith("X-ENTOURAGE_UUID:")) {
					// Apple iCal doesn't understand this key, and it's
					// entourage
					// specific (i.e. not needed by any caldav client):
					// strip it out
					continue;
				} else if (fromServer && line.startsWith("ATTENDEE;")
						&& (line.indexOf(this.exchangeSession.getEmail()) >= 0)) {
					// If this is coming from the server, strip out RSVP for
					// this
					// user as an attendee where the partstat is something
					// other
					// than PARTSTAT=NEEDS-ACTION since the RSVP confuses
					// iCal4 into
					// thinking the attendee has not replied

					int rsvpSuffix = line.indexOf("RSVP=TRUE;");
					int rsvpPrefix = line.indexOf(";RSVP=TRUE");

					if (((rsvpSuffix >= 0) || (rsvpPrefix >= 0))
							&& (line.indexOf("PARTSTAT=") >= 0)
							&& (line.indexOf("PARTSTAT=NEEDS-ACTION") < 0)) {

						// Strip out the "RSVP" line from the calendar entry
						if (rsvpSuffix >= 0) {
							line = line.substring(0, rsvpSuffix)
									+ line.substring(rsvpSuffix + 10);
						} else {
							line = line.substring(0, rsvpPrefix)
									+ line.substring(rsvpPrefix + 10);
						}

					}
				} else if (line.startsWith("ACTION:")) {
					if (fromServer && "DISPLAY".equals(action)) {
						// Use the default iCal alarm action instead
						// of the alarm Action exchange (and blackberry)
						// understand.
						// This is a bit of a hack because we don't know
						// what type
						// of alarm an iCal user really wants - but we know
						// what the
						// default is, and can setup the default action type

						result.writeLine("ACTION:AUDIO");

						if (!sound) {
							// Add default sound into the audio alarm
							result.writeLine("ATTACH;VALUE=URI:"
									+ this.exchangeSession.getDefaultSound());
						}

						continue;
					} else if (!fromServer && "AUDIO".equals(action)) {
						// Use the alarm action that exchange (and
						// blackberry) understand
						// (exchange and blackberry don't understand audio
						// actions)

						result.writeLine("ACTION:DISPLAY");
						continue;
					}

					// Don't recognize this type of action: pass it through

				} else if (line.startsWith("CLASS:")) {
					if (isAppleiCal) {
						continue;
					} else {
						if ("PRIVATE".equalsIgnoreCase(eventClass)) {
							result
									.writeLine("X-CALENDARSERVER-ACCESS:CONFIDENTIAL");
						} else if ("CONFIDENTIAL".equalsIgnoreCase(eventClass)) {
							result.writeLine("X-CALENDARSERVER-ACCESS:PRIVATE");
						} else {
							result.writeLine("X-CALENDARSERVER-ACCESS:"
									+ eventClass);
						}
					}
					// remove organizer line if user is organizer for iPhone
				} else if (fromServer && line.startsWith("ORGANIZER")
						&& !hasAttendee) {
					continue;
				} else if (organizer != null && line.startsWith("ATTENDEE")
						&& line.contains(organizer)) {
					// Ignore organizer as attendee
					continue;
				} else if (!fromServer && line.startsWith("ATTENDEE")) {
					line = replaceIcal4Principal(line);
				}

				result.writeLine(line);
			}
		} finally {
			reader.close();
		}

		return result.toString();
	}

	class Participants {
		String attendees;
		String organizer;
	}

	/**
	 * Replace iCal4 (Snow Leopard) principal paths with mailto expression
	 * 
	 * @param value
	 *            attendee value or ics line
	 * @return fixed value
	 */
	private String replaceIcal4Principal(String value) {
		if (value.contains("/principals/__uuids__/")) {
			return value.replaceAll(
					"/principals/__uuids__/([^/]*)__AT__([^/]*)/",
					"mailto:$1@$2");
		} else {
			return value;
		}
	}

}