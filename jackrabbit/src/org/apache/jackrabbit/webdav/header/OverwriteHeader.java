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
package org.apache.jackrabbit.webdav.header;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.jackrabbit.webdav.DavConstants;

/**
 * <code>OverwriteHeader</code>...
 */
public class OverwriteHeader implements org.apache.http.Header {

	public static final String OVERWRITE_TRUE = "T";
	public static final String OVERWRITE_FALSE = "F";

	/**
	 * Set 'doOverwrite' to <code>true</code> by default. See RFC 2518: "If the
	 * overwrite header is not included in a COPY or MOVE request then the
	 * resource MUST treat the request as if it has an overwrite header of value
	 * {@link #OVERWRITE_TRUE}".
	 */
	private final boolean doOverwrite;

	public OverwriteHeader(boolean doOverwrite) {
		this.doOverwrite = doOverwrite;
	}

	/**
	 * Create a new <code>OverwriteHeader</code> for the given request object.
	 * If the latter does not contain an "Overwrite" header field, the default
	 * applies, which is {@link #OVERWRITE_TRUE} according to RFC 2518.
	 * 
	 * @param request
	 */
	public OverwriteHeader(HttpServletRequest request) {
		String overwriteHeader = request
				.getHeader(DavConstants.HEADER_OVERWRITE);
		if (overwriteHeader != null) {
			doOverwrite = overwriteHeader.equalsIgnoreCase(OVERWRITE_TRUE);
		} else {
			// no Overwrite header -> default is 'true'
			doOverwrite = true;
		}
	}

	public HeaderElement[] getElements() throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return DavConstants.HEADER_OVERWRITE;
	}

	public String getValue() {
		return (doOverwrite) ? OVERWRITE_TRUE : OVERWRITE_FALSE;
	}

	public boolean isOverwrite() {
		return doOverwrite;
	}

}