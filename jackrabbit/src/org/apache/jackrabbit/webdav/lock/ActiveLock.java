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
package org.apache.jackrabbit.webdav.lock;

import org.apache.jackrabbit.webdav.xml.XmlSerializable;

/**
 * <code>ActiveLock</code> encapsulates the lock information for a
 * {@link org.apache.jackrabbit.webdav.DavResource}.
 */
public interface ActiveLock extends XmlSerializable {

	/**
	 * Returns the lockroot.
	 * 
	 * @return lockroot
	 * @see <a
	 *      href="http://www.webdav.org/specs/rfc4918.html#ELEMENT_lockroot">RFC
	 *      4818</a>
	 */
	public String getLockroot();

	/**
	 * Return the name (or id) of the lock owner.
	 * 
	 * @return name (or id) of the lock owner.
	 */
	public String getOwner();

	/**
	 * Return the scope of this lock.
	 * 
	 * @return scope
	 */
	public Scope getScope();

	/**
	 * Return the number of milliseconds the lock will live until it is expired
	 * or -1 if the timeout is not available (or the client is not allowed to
	 * retrieve it).
	 * 
	 * @return numer of milliseconds.
	 */
	public long getTimeout();

	/**
	 * Return the lock token.
	 * 
	 * @return token string representing the lock token.
	 */
	public String getToken();

	/**
	 * Return the type of this lock.
	 * 
	 * @return type
	 */
	public Type getType();

	/**
	 * Return true if the lock is deep.
	 * 
	 * @return true if the lock is deep.
	 */
	public boolean isDeep();

	/**
	 * Returns true, if this lock is already expired.
	 * 
	 * @return true if the lock is expired
	 */
	public boolean isExpired();

	/**
	 * Return true, if the given token matches the lock token present in this
	 * lock thus indicating that any lock relevant operation should not fail due
	 * to a lock.
	 * 
	 * @param lockToken
	 *            to be checked
	 * @return true if the given lock token matches.
	 */
	public boolean isLockedByToken(String lockToken);

	/**
	 * Set the lock deepness.
	 * 
	 * @param isDeep
	 */
	public void setIsDeep(boolean isDeep);

	/**
	 * Set the lockroot.
	 * 
	 * @param lockroot
	 * @see <a
	 *      href="http://www.webdav.org/specs/rfc4918.html#ELEMENT_lockroot">RFC
	 *      4818</a>
	 */
	public void setLockroot(String lockroot);

	/**
	 * Set the name (or id) of the lock owner
	 * 
	 * @param owner
	 */
	public void setOwner(String owner);

	/**
	 * Defines the number of milliseconds until the timeout is reached.
	 * 
	 * @param timeout
	 */
	public void setTimeout(long timeout);
}
