package com.owaconnector.exchange;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class DavResponseUtil {

	static final DocumentBuilderFactory BUILDER_FACTORY = DomUtil.BUILDER_FACTORY;

	/**
	 * In case of a MultiStatus response code, this method parses the response
	 * body and resets the 'success' flag depending on the multistatus content,
	 * which could indicate method failure as well.
	 * 
	 * @param httpState
	 * @param httpConnection
	 * @see HttpMethodBase#processResponseBody(HttpState, HttpConnection)
	 */
	protected static MultiStatus processResponseBody(HttpResponse response) {
		// in case of multi-status response
		MultiStatus multiStatus = null;
		if (getStatusCode(response) == DavServletResponse.SC_MULTI_STATUS) {
			try {
				multiStatus = MultiStatus
						.createFromXml(getRootElement(response));
				// sub-class processing/validation of the multiStatus
			} catch (IOException e) {

			}
		}
		return multiStatus;
	}

	private static Element getRootElement(HttpResponse response)
			throws IOException {
		Document document = getResponseBodyAsDocument(response);
		if (document != null) {
			return document.getDocumentElement();
		} else {
			return null;
		}
	}

	private static int getStatusCode(HttpResponse response) {
		// TODO Auto-generated method stub
		return response.getStatusLine().getStatusCode();
	}

	public static Document getResponseBodyAsDocument(HttpResponse response)
			throws IOException {

		InputStream in = response.getEntity().getContent();
		if (in != null) {
			// read response and try to build a xml document
			try {
				DocumentBuilder docBuilder = BUILDER_FACTORY
						.newDocumentBuilder();
				docBuilder.setErrorHandler(new DefaultHandler());
				Document responseDocument = docBuilder.parse(in);
				return responseDocument;
			} catch (ParserConfigurationException e) {
				IOException exception = new IOException(
						"XML parser configuration error");
				exception.initCause(e);
				throw exception;
			} catch (SAXException e) {
				IOException exception = new IOException("XML parsing error");
				exception.initCause(e);
				throw exception;
			} finally {
				in.close();
			}
		}
		// no body or no parseable.
		return null;
	}

	public static MultiStatus getResponseBodyAsMultiStatus(HttpResponse response) {
		return processResponseBody(response);
	}

}
