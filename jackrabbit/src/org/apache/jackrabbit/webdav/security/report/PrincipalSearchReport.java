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
package org.apache.jackrabbit.webdav.security.report;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;

/**
 * The <code>PrincipalSearchReport</code> performs a search for all principals
 * that match the search criteria specified in the request.
 * <p/>
 * The following XML structure is required in the request body:
 * 
 * <pre>
 * &lt;!ELEMENT principal-property-search ((property-search+), prop?, apply-to-principal-collection-set?) &gt;
 * &lt;!ELEMENT property-search (prop, match) >
 *  prop: see RFC 2518, Section 12.11
 * &lt;!ELEMENT match #PCDATA &gt;
 * &lt;!ELEMENT apply-to-principal-collection-set #EMPTY &gt;
 * </pre>
 * 
 * <strong>DAV:property-search</strong> contains lists the properties to be
 * searched inside the DAV:prop element and the query string inside the
 * DAV:match element. Multiple DAV:property-search elements or multiple elements
 * within the DAV:prop element will be interpreted with a logical AND.
 * <p/>
 * <strong>DAV:prop</strong> lists the property names to be reported in the
 * response for each of the principle resources found.
 * <p/>
 * <strong>DAV:apply-to-principal-collection-set</strong>: Optional empty
 * element. If present in the request body the search will be executed over all
 * members of the collections that are listed as values in the
 * DAV:principal-collection-set property present on the resource the report has
 * been requested for. Otherwise the search is limited to all members (at any
 * depth) of that resource itself.
 * <p/>
 * The response body must contain a single DAV:multistatus XML element.
 */
public class PrincipalSearchReport extends AbstractSecurityReport {

	// --------------------------------< implementation specific inner class
	// >---
	/**
	 * Inner utility class preparing the query arguments present in the
	 * DAV:property-search element(s).
	 */
	protected class SearchArgument {

		private final DavPropertyNameSet searchProps;
		private final String searchString;

		private SearchArgument(Element propSearch) {
			searchProps = new DavPropertyNameSet(DomUtil.getChildElement(
					propSearch, DavConstants.XML_PROP, DavConstants.NAMESPACE));
			searchString = DomUtil.getChildText(propSearch, XML_MATCH,
					SecurityConstants.NAMESPACE);
		}

		/**
		 * @return property name set used to restrict the search to a limited
		 *         amount of properties.
		 */
		protected DavPropertyNameSet getSearchProperties() {
			return searchProps;
		}

		/**
		 * @return query string as present in the DAV:match element.
		 */
		protected String getSearchString() {
			return searchString;
		}
	}

	public static final String XML_APPLY_TO_PRINCIPAL_COLLECTION_SET = "apply-to-principal-collection-set";
	public static final String XML_PROPERTY_SEARCH = "property-search";

	public static final String XML_MATCH = "match";

	/**
	 * The report name
	 */
	public static final String REPORT_NAME = "principal-property-search";

	/**
	 * The report type
	 */
	public static final ReportType REPORT_TYPE = ReportType.register(
			REPORT_NAME, SecurityConstants.NAMESPACE,
			PrincipalSearchReport.class);
	private String[] searchRoots;

	private SearchArgument[] searchArguments;

	/**
	 * Retrive the search arguments used to run the search for principals.<br>
	 * Note, that the search result must be converted to
	 * {@link MultiStatusResponse}s that must be returned back to this report.
	 * 
	 * @return array of <code>SearchArgument</code> used to run the principal
	 *         search.
	 * @see #setResponses(MultiStatusResponse[])
	 */
	public SearchArgument[] getSearchArguments() {
		return searchArguments;
	}

	// ------------------------------------< implementation specific methods
	// >---
	/**
	 * Retrieve the the locations where the search should be performed.<br>
	 * Note, that the search result must be converted to
	 * {@link MultiStatusResponse}s that must be returned back to this report.
	 * 
	 * @return href of collections that act as start for the search.
	 * @see #setResponses(MultiStatusResponse[])
	 */
	public String[] getSearchRoots() {
		return searchRoots;
	}

	/**
	 * @see Report#getType()
	 */
	public ReportType getType() {
		return REPORT_TYPE;
	}

	/**
	 * @see Report#init(DavResource, ReportInfo)
	 */
	@Override
	public void init(DavResource resource, ReportInfo info) throws DavException {
		super.init(resource, info);
		// make sure the request body contains all mandator elements
		if (!info.containsContentElement(XML_PROPERTY_SEARCH,
				SecurityConstants.NAMESPACE)) {
			throw new DavException(HttpServletResponse.SC_BAD_REQUEST,
					"Request body must contain at least a single DAV:property-search element.");
		}
		List<?> psElements = info.getContentElements(XML_PROPERTY_SEARCH,
				SecurityConstants.NAMESPACE);
		searchArguments = new SearchArgument[psElements.size()];
		Iterator<?> it = psElements.iterator();
		int i = 0;
		while (it.hasNext()) {
			searchArguments[i++] = new SearchArgument((Element) it.next());
		}

		if (info.containsContentElement(XML_APPLY_TO_PRINCIPAL_COLLECTION_SET,
				SecurityConstants.NAMESPACE)) {
			HrefProperty p = new HrefProperty(resource
					.getProperty(SecurityConstants.PRINCIPAL_COLLECTION_SET));
			searchRoots = p.getHrefs().toArray(new String[0]);
		} else {
			searchRoots = new String[] { resource.getHref() };
		}
	}

	/**
	 * Write the search result back to the report.
	 * 
	 * @param responses
	 */
	public void setResponses(MultiStatusResponse[] responses) {
		this.responses = responses;
	}
}