/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.webdav.client.methods;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;

/**
 * <code>DavMethodBase</code>...
 */
public abstract class DavMethodBase extends HttpEntityEnclosingRequestBase
		implements DavMethod, DavConstants {

	static final DocumentBuilderFactory BUILDER_FACTORY = DomUtil.BUILDER_FACTORY;

	public DavMethodBase(String uri) {
		super();
		try {
			setURI(new URI(uri));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------< DavMethod
	// >---
	/**
	 * @see DavMethod#addRequestHeader(Header)
	 */
	public void addRequestHeader(org.apache.http.Header header) {
		addHeader(header.getName(), header.getValue());
	}

	// ---------------------------------------------------------< HttpMethod
	// >---
	/**
	 * Reset method to 'abstract' in order to force subclasses to change the
	 * name (inherited value is {@link GetMethod#getName()}).
	 * 
	 * @return Name of the method.
	 * @see HttpMethod#getName()
	 */
	public abstract String getName();

	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param requestBody
	 * @throws IOException
	 */
	public void setRequestBody(Document requestBody) throws IOException {
		setEntity(new XmlRequestEntity(requestBody));
	}

	/**
	 * @see DavMethod#getResponseBodyAsMultiStatus()
	 */
	// public MultiStatus getResponseBodyAsMultiStatus() throws IOException,
	// DavException {
	// //checkUsed();
	// // if (multiStatus != null) {
	// // return multiStatus;
	// // } else {
	// // DavException dx = getResponseException();
	// // if (dx != null) {
	// // throw dx;
	// // } else {
	// // throw new DavException(getStatusCode(), getName()
	// // + " resulted with unexpected status: "
	// // + getStatusLine());
	// // }
	// // }
	// return null;
	// }

	/**
	 * @see DavMethod#getResponseBodyAsDocument()
	 */
	// public Document getResponseBodyAsDocument() throws IOException {
	// if (responseDocument != null) {
	// // response has already been read
	// return responseDocument;
	// }
	//
	// InputStream in = getResponseBodyAsStream();
	// if (in != null) {
	// // read response and try to build a xml document
	// try {
	// DocumentBuilder docBuilder = BUILDER_FACTORY
	// .newDocumentBuilder();
	// docBuilder.setErrorHandler(new DefaultHandler());
	// responseDocument = docBuilder.parse(in);
	// return responseDocument;
	// } catch (ParserConfigurationException e) {
	// IOException exception = new IOException(
	// "XML parser configuration error");
	// exception.initCause(e);
	// throw exception;
	// } catch (SAXException e) {
	// IOException exception = new IOException("XML parsing error");
	// exception.initCause(e);
	// throw exception;
	// } finally {
	// in.close();
	// }
	// }
	// // no body or no parseable.
	// return null;
	// }

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	// Element getRootElement() throws IOException {
	// Document document = getResponseBodyAsDocument();
	// if (document != null) {
	// return document.getDocumentElement();
	// }
	// return null;
	// }

	/**
	 * @see DavMethod#getResponseException()
	 */
	// public DavException getResponseException() throws IOException {
	// //checkUsed();
	// if (success) {
	// String msg = "Cannot retrieve exception from successful response.";
	// log.warn(msg);
	// throw new IllegalStateException(msg);
	// }
	//
	// Element responseRoot = null;
	// try {
	// responseRoot = getRootElement();
	// } catch (IOException e) {
	// // unparsable body -> use null element
	// }
	// if (responseRoot != null) {
	// return new DavException(getStatusCode(), getStatusText(), null,
	// responseRoot);
	// } else {
	// // fallback: no or unparsable response body
	// return new DavException(getStatusCode(), getStatusText());
	// }
	// return null;
	// }

	/**
	 * @see DavMethod#checkSuccess()
	 */
	// public void checkSuccess() throws DavException, IOException {
	// if (!succeeded()) {
	// throw getResponseException();
	// }
	// }

	/**
	 * @see DavMethod#succeeded()
	 */
	// public boolean succeeded() {
	// //checkUsed();
	// return success;
	// }

	/**
	 * 
	 * @param requestBody
	 * @throws IOException
	 */
	public void setRequestBody(XmlSerializable requestBody) throws IOException {
		try {
			Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();
			doc.appendChild(requestBody.toXml(doc));
			setRequestBody(doc);
		} catch (ParserConfigurationException e) {
			IOException exception = new IOException(
					"XML parser configuration error");
			exception.initCause(e);
			throw exception;
		}
	}

	/**
	 * @see DavMethod#setRequestHeader(Header)
	 */
	public void setRequestHeader(org.apache.http.Header header) {
		setHeader(header.getName(), header.getValue());
	}

	// /**
	// *
	// * @param statusCode
	// * @return true if the specified status code corresponds to a successfully
	// * completed request.
	// */
	// abstract protected boolean isSuccess(int statusCode);
	//
	// /**
	// *
	// * @param success
	// */
	// protected void setSuccess(boolean success) {
	// this.success = success;
	// }
	//
	// /**
	// * @return true if this method was successfully executed; false otherwise.
	// */
	// protected boolean getSuccess() {
	// return success;
	// }
	//
	// /**
	// * This method is invoked during the
	// * {@link #processResponseBody(HttpState, HttpConnection)}, which in this
	// * implementation parses the response body into a <code>MultiStatus</code>
	// * object if the status code indicates 207 (MultiStatus).<br>
	// * Subclasses may want to override this method in order to apply specific
	// * validation of the multi-status.
	// * <p/>
	// * This implementation does nothing.
	// *
	// * @param multiStatus
	// * @param httpState
	// * @param httpConnection
	// * @see #processResponseBody(HttpState, HttpConnection)
	// */
	// protected void processMultiStatusBody(MultiStatus multiStatus,
	// HttpState httpState, HttpConnection httpConnection) {
	// // does nothing
	// }
	//
	// // -----------------------------------------------------< HttpMethodBase
	// // >---
	// /**
	// *
	// * @param httpState
	// * @param httpConnection
	// */
	// protected void processStatusLine(HttpState httpState,
	// HttpConnection httpConnection) {
	// super.processStatusLine(httpState, httpConnection);
	// int code = getStatusCode();
	// // sub classes define if status code indicates success.
	// success = isSuccess(code);
	// }
	//
	// /**
	// * In case of a MultiStatus response code, this method parses the response
	// * body and resets the 'success' flag depending on the multistatus
	// content,
	// * which could indicate method failure as well.
	// *
	// * @param httpState
	// * @param httpConnection
	// * @see HttpMethodBase#processResponseBody(HttpState, HttpConnection)
	// */
	// protected void processResponseBody(HttpState httpState,
	// HttpConnection httpConnection) {
	// // in case of multi-status response
	// if (getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
	// try {
	// multiStatus = MultiStatus.createFromXml(getRootElement());
	// // sub-class processing/validation of the multiStatus
	// processMultiStatusBody(multiStatus, httpState, httpConnection);
	// } catch (IOException e) {
	// log.error("Error while parsing multistatus response: " + e);
	// success = false;
	// }
	// }
	// }
}