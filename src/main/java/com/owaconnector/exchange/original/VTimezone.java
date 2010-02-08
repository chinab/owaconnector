/**
 * 
 */
package com.owaconnector.exchange.original;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

public final class VTimezone {

	protected static final Logger LOGGER = Logger.getLogger(VTimezone.class);

	/**
	 * 
	 */
	private final ExchangeSession exchangeSession;
	private final DavGatewayHttpClientFacade facade;

	/**
	 * @param exchangeSession
	 */
	public VTimezone(ExchangeSession exchangeSession) {
		this.exchangeSession = exchangeSession;
		this.facade = exchangeSession.getFacade();
	}

	private String timezoneBody;
	private String timezoneId;

	/**
	 * create a fake event to get VTIMEZONE body
	 * 
	 * @throws HttpException
	 */
	public void load() throws HttpException {
		try {
			// create temporary folder
			Folder folder = new Folder("davmailtemp", this.exchangeSession);
			String folderPath = folder.getPath();
			folder.create(Folder.FolderTypes.IPF_APPOINTMENT);

			HttpPost postMethod = new HttpPost(URIUtil.encodePath(folderPath));

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("Cmd", "saveappt"));
			formparams.add(new BasicNameValuePair("FORMTYPE", "appointment"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
					"UTF-8");
			postMethod.setEntity(entity);
			String fakeEventUrl = null;
			try {
				// create fake event
				HttpResponse response = facade.execute(postMethod);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					fakeEventUrl = StringUtil.getToken(EntityUtils
							.toString(response.getEntity()),
							"<span id=\"itemHREF\">", "</span>");
				}
			} finally {
				postMethod.abort();
			}
			// failover for Exchange 2007, use PROPPATCH with forced
			// timezone
			if (fakeEventUrl == null) {
				ArrayList<DavProperty> propertyList = new ArrayList<DavProperty>();
				propertyList.add(new DefaultDavProperty(DavPropertyName.create(
						"contentclass", Namespace.getNamespace("DAV:")),
						"urn:content-classes:appointment"));
				propertyList
						.add(new DefaultDavProperty(
								DavPropertyName
										.create(
												"outlookmessageclass",
												Namespace
														.getNamespace("http://schemas.microsoft.com/exchange/")),
								"IPM.Appointment"));
				propertyList.add(new DefaultDavProperty(DavPropertyName.create(
						"instancetype", Namespace
								.getNamespace("urn:schemas:calendar:")), "0"));
				// get forced timezone id from settings
				// timezoneId = Settings.getProperty("davmail.timezoneId");
				if (getTimezoneId() != null) {
					propertyList.add(new DefaultDavProperty(DavPropertyName
							.create("timezoneid", Namespace
									.getNamespace("urn:schemas:calendar:")),
							getTimezoneId()));
				}
				String patchMethodUrl = URIUtil.encodePath(folderPath) + '/'
						+ UUID.randomUUID().toString() + ".EML";
				PropPatchMethod patchMethod = new PropPatchMethod(URIUtil
						.encodePath(patchMethodUrl), propertyList);
				try {
					HttpResponse response = this.facade.execute(patchMethod);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_MULTI_STATUS) {
						fakeEventUrl = patchMethodUrl;
					}
				} finally {
					patchMethod.abort();
				}
			}
			if (fakeEventUrl != null) {
				// get fake event body
				HttpGet getMethod = new HttpGet(URIUtil
						.encodePath(fakeEventUrl));
				getMethod.addHeader("Translate", "f");
				try {
					HttpResponse response = facade.execute(getMethod);
					setTimezoneBody("BEGIN:VTIMEZONE"
							+ StringUtil.getToken(EntityUtils.toString(response
									.getEntity()), "BEGIN:VTIMEZONE",
									"END:VTIMEZONE") + "END:VTIMEZONE\r\n");
					setTimezoneId(StringUtil.getToken(getTimezoneBody(),
							"TZID:", "\r\n"));
				} finally {
					getMethod.abort();
				}
			}

			// delete temporary folder
			folder.delete();
		} catch (IOException e) {
			LOGGER.warn("Unable to get VTIMEZONE info: " + e, e);
		}
	}

	public void setTimezoneId(String timezoneId) {
		this.timezoneId = timezoneId;
	}

	public String getTimezoneId() {
		return timezoneId;
	}

	public void setTimezoneBody(String timezoneBody) {
		this.timezoneBody = timezoneBody;
	}

	public String getTimezoneBody() {
		return timezoneBody;
	}
}