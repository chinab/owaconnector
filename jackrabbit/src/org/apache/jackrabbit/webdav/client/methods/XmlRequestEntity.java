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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.jackrabbit.commons.xml.SerializingContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * <code>XmlRequestEntity</code>...
 */
public class XmlRequestEntity implements HttpEntity {

	private static Logger log = LoggerFactory.getLogger(XmlRequestEntity.class);

	private final ByteArrayOutputStream xml = new ByteArrayOutputStream();

	public XmlRequestEntity(Document xmlDocument) throws IOException {
		try {
			ContentHandler handler = SerializingContentHandler
					.getSerializer(xml);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(new DOMSource(xmlDocument), new SAXResult(
					handler));
		} catch (TransformerException e) {
			log.error(e.getMessage());
			throw new IOException(e.getMessage());
		} catch (SAXException e) {
			log.error(e.getMessage());
			throw new IOException(e.getMessage());
		}
	}

	public void consumeContent() throws IOException {
	}

	public InputStream getContent() throws IOException, IllegalStateException {
		return new ByteArrayInputStream(xml.toByteArray());
	}

	public Header getContentEncoding() {
		return new BasicHeader("Content-Encoding",
				SerializingContentHandler.ENCODING);
	}

	public long getContentLength() {
		return xml.size();
	}

	public Header getContentType() {
		// TODO: Shouldn't this be application/xml? See JCR-1621
		return new BasicHeader("Content-Type", "text/xml; charset="
				+ SerializingContentHandler.ENCODING);
	}

	public boolean isChunked() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRepeatable() {
		return true;
	}

	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return false;
	}

	public void writeRequest(OutputStream out) throws IOException {
		xml.writeTo(out);
	}

	public void writeTo(OutputStream arg0) throws IOException {
		xml.writeTo(arg0);

	}

}