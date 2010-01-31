/**
 * 
 */
package davmail.exchange;

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
import org.mortbay.util.URIUtil;

import davmail.util.StringUtil;

final class VTimezone {
	/**
	 * 
	 */
	private final ExchangeSession exchangeSession;

	/**
	 * @param exchangeSession
	 */
	VTimezone(ExchangeSession exchangeSession) {
		this.exchangeSession = exchangeSession;
	}

	String timezoneBody;
	String timezoneId;

	/**
	 * create a fake event to get VTIMEZONE body
	 * 
	 * @throws HttpException
	 */
	void load() throws HttpException {
		try {
			// create temporary folder
			String folderPath = this.exchangeSession
					.getFolderPath("davmailtemp");
			this.exchangeSession.createCalendarFolder(folderPath);

			HttpPost postMethod = new HttpPost(URIUtil
					.encodePath(folderPath));

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("Cmd", "saveappt"));
			formparams
					.add(new BasicNameValuePair("FORMTYPE", "appointment"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
					formparams, "UTF-8");
			postMethod.setEntity(entity);
			String fakeEventUrl = null;
			try {
				// create fake event
				HttpResponse response = this.exchangeSession.httpClient
						.execute(postMethod);
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
				propertyList.add(new DefaultDavProperty(DavPropertyName
						.create("contentclass", Namespace
								.getNamespace("DAV:")),
						"urn:content-classes:appointment"));
				propertyList
						.add(new DefaultDavProperty(
								DavPropertyName
										.create(
												"outlookmessageclass",
												Namespace
														.getNamespace("http://schemas.microsoft.com/exchange/")),
								"IPM.Appointment"));
				propertyList.add(new DefaultDavProperty(DavPropertyName
						.create("instancetype", Namespace
								.getNamespace("urn:schemas:calendar:")),
						"0"));
				// get forced timezone id from settings
				// timezoneId = Settings.getProperty("davmail.timezoneId");
				if (timezoneId != null) {
					propertyList
							.add(new DefaultDavProperty(
									DavPropertyName
											.create(
													"timezoneid",
													Namespace
															.getNamespace("urn:schemas:calendar:")),
									timezoneId));
				}
				String patchMethodUrl = URIUtil.encodePath(folderPath)
						+ '/' + UUID.randomUUID().toString() + ".EML";
				PropPatchMethod patchMethod = new PropPatchMethod(URIUtil
						.encodePath(patchMethodUrl), propertyList);
				try {
					HttpResponse response = this.exchangeSession.httpClient.execute(patchMethod);
					int statusCode = response.getStatusLine()
							.getStatusCode();
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
					HttpResponse response = this.exchangeSession.httpClient
							.execute(getMethod);
					timezoneBody = "BEGIN:VTIMEZONE"
							+ StringUtil.getToken(EntityUtils
									.toString(response.getEntity()),
									"BEGIN:VTIMEZONE", "END:VTIMEZONE")
							+ "END:VTIMEZONE\r\n";
					timezoneId = StringUtil.getToken(timezoneBody, "TZID:",
							"\r\n");
				} finally {
					getMethod.abort();
				}
			}

			// delete temporary folder
			this.exchangeSession.deleteFolder("davmailtemp");
		} catch (IOException e) {
			ExchangeSession.LOGGER.warn("Unable to get VTIMEZONE info: " + e, e);
		}
	}
}