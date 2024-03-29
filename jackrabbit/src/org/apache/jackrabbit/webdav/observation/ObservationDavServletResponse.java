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
package org.apache.jackrabbit.webdav.observation;

import java.io.IOException;

import org.apache.jackrabbit.webdav.DavServletResponse;

/**
 * <code>ObservationDavServletResponse</code> provides extensions to the
 * {@link DavServletResponse} interface used for dealing with observation.
 */
public interface ObservationDavServletResponse extends DavServletResponse {

	/**
	 * Send the response to a sucessful POLL request.
	 * 
	 * @param eventdiscovery
	 *            {@link EventDiscovery} object to be returned in the response
	 *            body.
	 * @throws IOException
	 */
	public void sendPollResponse(EventDiscovery eventdiscovery)
			throws IOException;

	/**
	 * Send the response to a successful SUBSCRIBE request.
	 * 
	 * @param subsription
	 *            that needs to be represented in the response body.
	 * @throws IOException
	 */
	public void sendSubscriptionResponse(Subscription subsription)
			throws IOException;
}