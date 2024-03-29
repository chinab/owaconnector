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
package org.apache.jackrabbit.webdav;

import java.util.Iterator;
import java.util.List;

/**
 * <code>DavResourceIteratorImpl</code> implementation of the
 * {@link DavResourceIterator} interface.<br>
 * NOTE: {@link #remove()} is not implemented.
 */
public class DavResourceIteratorImpl implements DavResourceIterator {

	private Iterator<?> it;
	private int size;

	/**
	 * Create a new DavResourceIterator from the specified list.
	 * 
	 * @param list
	 */
	public DavResourceIteratorImpl(List<?> list) {
		it = list.iterator();
		size = list.size();
	}

	/**
	 * @see DavResourceIterator#hasNext()
	 */
	public boolean hasNext() {
		return it.hasNext();
	}

	/**
	 * @see DavResourceIterator#next()
	 */
	public Object next() {
		return it.next();
	}

	/**
	 * @see DavResourceIterator#nextResource()
	 */
	public DavResource nextResource() {
		return (DavResource) next();
	}

	/**
	 * @see DavResourceIterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				"Remove not allowed with DavResourceIteratorImpl");
	}

	/**
	 * Returns the size of the initial list.
	 * 
	 * @see DavResourceIterator#size()
	 */
	public int size() {
		return size;
	}
}