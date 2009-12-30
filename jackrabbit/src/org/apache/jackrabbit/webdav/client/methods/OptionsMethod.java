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
import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.OptionsInfo;

/**
 * <code>OptionsMethod</code>...
 */
public class OptionsMethod extends DavMethodBase {

	private final Set<?> allowedMethods = new HashSet<Object>();
	private final Set<?> complianceClasses = new HashSet<Object>();

	public OptionsMethod(String uri) {
		super(uri);
	}

	public OptionsMethod(String uri, OptionsInfo optionsInfo)
			throws IOException {
		super(uri);
		if (optionsInfo != null) {
			setRequestBody(optionsInfo);
		}
	}

	public OptionsMethod(String uri, String[] optionsEntries)
			throws IOException {
		this(uri, new OptionsInfo(optionsEntries));
	}

	/**
	 * @throws IOException
	 */
	// public OptionsResponse getResponseAsOptionsResponse() throws IOException
	// {
	// checkUsed();
	// OptionsResponse or = null;
	// Element rBody = getRootElement();
	// if (rBody != null) {
	// or = OptionsResponse.createFromXml(rBody);
	// }
	// return or;
	// }

	/**
	 * Checks if the specified method is a supported method by the resource
	 * identified by the original URI.
	 * 
	 * @param method
	 * @return true if the given method is contained in the 'Allow' response
	 *         header.
	 */
	// public boolean isAllowed(String method) {
	// checkUsed();
	// return allowedMethods.contains(method.toUpperCase());
	// }

	/**
	 * Returns an array of String listing the allowed methods.
	 * 
	 * @return all methods allowed on the resource specified by the original
	 *         URI.
	 */
	// public String[] getAllowedMethods() {
	// checkUsed();
	// return (String[]) allowedMethods.toArray(new
	// String[allowedMethods.size()]);
	// }

	/**
	 * Checks if the specified compliance class is supported by the resource
	 * identified by the original URI.
	 * 
	 * @param complianceClass
	 *            WebDAV compliance class
	 * @return true if the given compliance class is contained in the 'DAV'
	 *         response header.
	 */
	// public boolean hasComplianceClass(String complianceClass) {
	// checkUsed();
	// return complianceClasses.contains(complianceClass);
	// }

	/**
	 * Returns an array of String listing the WebDAV compliance classes.
	 * 
	 * @return all compliance classes supported by the resource specified by the
	 *         original URI.
	 */
	// public String[] getComplianceClasses() {
	// checkUsed();
	// return (String[]) complianceClasses.toArray(new
	// String[complianceClasses.size()]);
	// }

	// ------------------------------------------------------< DavMethodBase
	// >---
	/**
	 * 
	 * @param statusCode
	 * @return true if status code is {@link DavServletResponse#SC_OK 200 (OK)}.
	 */
	// protected boolean isSuccess(int statusCode) {
	// return statusCode == DavServletResponse.SC_OK;
	// }
	@Override
	public String getMethod() {
		return getName();
	}

	// -----------------------------------------------------< HttpMethodBase
	// >---
	/**
	 * <p>
	 * This implementation will parse the <tt>Allow</tt> and <tt>DAV</tt>
	 * headers to obtain the set of HTTP methods and WebDAV compliance classes
	 * supported by the resource identified by the Request-URI.
	 * </p>
	 * 
	 * @param state
	 *            the {@link HttpState state} information associated with this
	 *            method
	 * @param conn
	 *            the {@link HttpConnection connection} used to execute this
	 *            HTTP method
	 * @see HttpMethodBase#processResponseHeaders(HttpState, HttpConnection)
	 */
	// protected void processResponseHeaders(HttpState state, HttpConnection
	// conn) {
	// Header allow = getFirstHeader("Allow");
	// if (allow != null) {
	// String[] methods = allow.getValue().split(",");
	// for (int i = 0; i < methods.length; i++) {
	// allowedMethods.add(methods[i].trim().toUpperCase());
	// }
	// }
	// Header dav = getFirstHeader("DAV");
	// if (dav != null) {
	// String[] classes = dav.getValue().split(",");
	// for (int i = 0; i < classes.length; i++) {
	// complianceClasses.add(classes[i].trim());
	// }
	// }
	// }

	// ---------------------------------------------------------< HttpMethod
	// >---
	/**
	 * @see org.apache.commons.httpclient.HttpMethod#getName()
	 */
	@Override
	public String getName() {
		return DavMethods.METHOD_OPTIONS;
	}

	public Set<?> getAllowedMethods() {
		return allowedMethods;
	}

	public Set<?> getComplianceClasses() {
		return complianceClasses;
	}
}