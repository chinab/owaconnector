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
package org.apache.jackrabbit.webdav.version.report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The <code>ReportInfo</code> class encapsulates the body of a REPORT request.
 * <a href="http://www.ietf.org/rfc/rfc3253.txt">RFC 3253</a> the top Xml
 * element being the name of the requested report. In addition a Depth header
 * may be present (default value: {@link DavConstants#DEPTH_0}).
 */
public class ReportInfo implements XmlSerializable {

	private static Logger log = LoggerFactory.getLogger(ReportInfo.class);

	private final String typeLocalName;
	private final Namespace typeNamespace;
	private final int depth;
	private final DavPropertyNameSet propertyNames;
	private final List<Element> content = new ArrayList<Element>();

	/**
	 * Create a new <code>ReportInfo</code> object from the given Xml element.
	 * 
	 * @param reportElement
	 * @param depth
	 *            Depth value as retrieved from the
	 *            {@link DavConstants#HEADER_DEPTH}.
	 * @throws DavException
	 *             if the report element is <code>null</code>.
	 */
	public ReportInfo(Element reportElement, int depth) throws DavException {
		if (reportElement == null) {
			log.warn("Report request body must not be null.");
			throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
		}

		this.typeLocalName = reportElement.getLocalName();
		this.typeNamespace = DomUtil.getNamespace(reportElement);
		this.depth = depth;
		Element propElement = DomUtil.getChildElement(reportElement,
				DavConstants.XML_PROP, DavConstants.NAMESPACE);
		if (propElement != null) {
			propertyNames = new DavPropertyNameSet(propElement);
			reportElement.removeChild(propElement);
		} else {
			propertyNames = new DavPropertyNameSet();
		}

		ElementIterator it = DomUtil.getChildren(reportElement);
		while (it.hasNext()) {
			Element el = it.nextElement();
			if (!DavConstants.XML_PROP.equals(el.getLocalName())) {
				content.add(el);
			}
		}
	}

	/**
	 * Create a new <code>ReportInfo</code>
	 * 
	 * @param type
	 */
	public ReportInfo(ReportType type) {
		this(type, DavConstants.DEPTH_0, null);
	}

	/**
	 * Create a new <code>ReportInfo</code>
	 * 
	 * @param type
	 * @param depth
	 */
	public ReportInfo(ReportType type, int depth) {
		this(type, depth, null);
	}

	/**
	 * Create a new <code>ReportInfo</code>
	 * 
	 * @param type
	 * @param depth
	 * @param propertyNames
	 */
	public ReportInfo(ReportType type, int depth,
			DavPropertyNameSet propertyNames) {
		this.typeLocalName = type.getLocalName();
		this.typeNamespace = type.getNamespace();
		this.depth = depth;
		if (propertyNames != null) {
			this.propertyNames = new DavPropertyNameSet(propertyNames);
		} else {
			this.propertyNames = new DavPropertyNameSet();
		}
	}

	/**
	 * Indicates whether this info contains an element with the given
	 * name/namespace.
	 * 
	 * @param localName
	 * @param namespace
	 * @return true if an element with the given name/namespace is present in
	 *         the body of the request info.
	 */
	public boolean containsContentElement(String localName, Namespace namespace) {
		if (content.isEmpty()) {
			return false;
		}
		Iterator<Element> it = content.iterator();
		while (it.hasNext()) {
			Element elem = it.next();
			boolean sameNamespace = (namespace == null) ? elem
					.getNamespaceURI() == null : namespace.isSame(elem
					.getNamespaceURI());
			if (sameNamespace && elem.getLocalName().equals(localName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the Xml element with the given name/namespace that is a child
	 * of this info. If no such child exists <code>null</code> is returned. If
	 * multiple elements with the same name exist, the first one is returned.
	 * 
	 * @param localName
	 * @param namespace
	 * @return Xml element with the given name/namespace or <code>null</code>
	 */
	public Element getContentElement(String localName, Namespace namespace) {
		List<Element> values = getContentElements(localName, namespace);
		if (values.isEmpty()) {
			return null;
		} else {
			return values.get(0);
		}
	}

	/**
	 * Returns a list containing all child Xml elements of this info that have
	 * the specified name/namespace. If this info contains no such element, an
	 * empty list is returned.
	 * 
	 * @param localName
	 * @param namespace
	 * @return List contain all child elements with the given name/namespace or
	 *         an empty list.
	 */
	public List<Element> getContentElements(String localName,
			Namespace namespace) {
		List<Element> l = new ArrayList<Element>();
		Iterator<Element> it = content.iterator();
		while (it.hasNext()) {
			Element elem = it.next();
			if (DomUtil.matches(elem, localName, namespace)) {
				l.add(elem);
			}
		}
		return l;
	}

	/**
	 * Returns the depth field. The request must be applied separately to the
	 * collection itself and to all members of the collection that satisfy the
	 * depth value.
	 * 
	 * @return depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Returns a <code>DavPropertyNameSet</code> providing the property names
	 * present in an eventual {@link DavConstants#XML_PROP} child element. If no
	 * such child element is present an empty set is returned.
	 * 
	 * @return {@link DavPropertyNameSet} providing the property names present
	 *         in an eventual {@link DavConstants#XML_PROP DAV:prop} child
	 *         element or an empty set.
	 */
	public DavPropertyNameSet getPropertyNameSet() {
		return propertyNames;
	}

	/**
	 * Name of the report type that will be / has been requested.
	 * 
	 * @return Name of the report type
	 */
	public String getReportName() {
		return DomUtil.getExpandedName(typeLocalName, typeNamespace);
	}

	/**
	 * Add the specified Xml element as child of this info.
	 * 
	 * @param contentElement
	 */
	public void setContentElement(Element contentElement) {
		content.add(contentElement);
	}

	/**
	 * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
	 * @param document
	 */
	public Element toXml(Document document) {
		Element reportElement = DomUtil.createElement(document, typeLocalName,
				typeNamespace);
		if (!content.isEmpty()) {
			Iterator<Element> it = content.iterator();
			while (it.hasNext()) {
				Object contentEntry = it.next();
				if (contentEntry instanceof Node) {
					Node n = document.importNode((Node) contentEntry, true);
					reportElement.appendChild(n);
				}
				// else: another object. this should never occure and is
				// therefore ignored.
			}
		}
		if (!propertyNames.isEmpty()) {
			reportElement.appendChild(propertyNames.toXml(document));
		}
		return reportElement;
	}

}