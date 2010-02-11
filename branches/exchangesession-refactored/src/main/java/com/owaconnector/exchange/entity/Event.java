package com.owaconnector.exchange.entity;

import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.mortbay.util.URIUtil;

import com.owaconnector.exchange.ExchangeNamespace;
import com.owaconnector.exchange.util.DavPropertyUtil;

/**
 * Calendar event object
 */
public class Event {

	// private static final Logger LOGGER = Logger.getLogger(Event.class);

	public Event(MultiStatusResponse response) {

		setHref(URIUtil.decodePath(response.getHref()));
		setPermanentUrl(DavPropertyUtil.getPropertyIfExists(response
				.getProperties(HttpStatus.SC_OK), "permanenturl",
				ExchangeNamespace.SCHEMAS_EXCHANGE));
		setEtag(DavPropertyUtil.getPropertyIfExists(response.getProperties(HttpStatus.SC_OK),
				"getetag", Namespace.getNamespace("DAV:")));

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

	class Participants {
		String attendees;
		String organizer;
	}

}