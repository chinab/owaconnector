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
package org.apache.jackrabbit.webdav.bind;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BindInfo implements XmlSerializable {

	private static Logger log = LoggerFactory.getLogger(BindInfo.class);

	/**
	 * Build an <code>BindInfo</code> object from the root element present in
	 * the request body.
	 * 
	 * @param root
	 *            the root element of the request body
	 * @return a BindInfo object containing segment and href
	 * @throws org.apache.jackrabbit.webdav.DavException
	 *             if the BIND request is malformed
	 */
	public static BindInfo createFromXml(Element root) throws DavException {
		if (!DomUtil.matches(root, BindConstants.XML_BIND,
				BindConstants.NAMESPACE)) {
			log.warn("DAV:bind element expected");
			throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
		}
		String href = null;
		String segment = null;
		ElementIterator it = DomUtil.getChildren(root);
		while (it.hasNext()) {
			Element elt = it.nextElement();
			if (DomUtil.matches(elt, BindConstants.XML_SEGMENT,
					BindConstants.NAMESPACE)) {
				if (segment == null) {
					segment = DomUtil.getText(elt);
				} else {
					log
							.warn("unexpected multiple occurence of DAV:segment element");
					throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else if (DomUtil.matches(elt, BindConstants.XML_HREF,
					BindConstants.NAMESPACE)) {
				if (href == null) {
					href = DomUtil.getText(elt);
				} else {
					log
							.warn("unexpected multiple occurence of DAV:href element");
					throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else {
				log.warn("unexpected element " + elt.getLocalName());
				throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		if (href == null) {
			log.warn("DAV:href element expected");
			throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
		}
		if (segment == null) {
			log.warn("DAV:segment element expected");
			throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
		}
		return new BindInfo(href, segment);
	}

	private String segment;

	private String href;

	public BindInfo(String href, String segment) {
		this.href = href;
		this.segment = segment;
	}

	public String getHref() {
		return this.href;
	}

	public String getSegment() {
		return this.segment;
	}

	/**
	 * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
	 */
	public Element toXml(Document document) {
		Element bindElt = DomUtil.createElement(document,
				BindConstants.XML_BIND, BindConstants.NAMESPACE);
		Element hrefElt = DomUtil.createElement(document,
				BindConstants.XML_HREF, BindConstants.NAMESPACE, this.href);
		Element segElt = DomUtil.createElement(document,
				BindConstants.XML_SEGMENT, BindConstants.NAMESPACE,
				this.segment);
		bindElt.appendChild(hrefElt);
		bindElt.appendChild(segElt);
		return bindElt;
	}
}
