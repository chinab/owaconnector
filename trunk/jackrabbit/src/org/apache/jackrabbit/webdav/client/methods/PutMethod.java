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

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavServletResponse;

/**
 * <code>PutMethod</code>...
 */
public class PutMethod extends DavMethodBase {

	public PutMethod(String uri) {
		super(uri);
	}

	@Override
	public String getMethod() {
		return getName();
	}

	// ---------------------------------------------------------< HttpMethod
	// >---
	/**
	 * @see org.apache.commons.httpclient.HttpMethod#getName()
	 */
	@Override
	public String getName() {
		return DavMethods.METHOD_PUT;
	}

	// ------------------------------------------------------< DavMethodBase
	// >---
	/**
	 * @param statusCode
	 * @return true if the status code is either
	 *         {@link DavServletResponse#SC_CREATED 200 (OK)} or
	 *         {@link DavServletResponse#SC_NO_CONTENT 204 (No Content)}.
	 * @see DavMethodBase#isSuccess(int)
	 */
	protected boolean isSuccess(int statusCode) {
		return statusCode == HttpServletResponse.SC_CREATED
				|| statusCode == HttpServletResponse.SC_NO_CONTENT;
	}
}