/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2009  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.owaconnector.exception;

/**
 * Custom exception to mark network down case.
 */
public class NetworkDownException extends DavMailException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Build a network down exception with the provided BundleMessage key.
	 * 
	 * @param key
	 *            message key
	 */
	public NetworkDownException(String key) {
		super(key);
	}

	/**
	 * Build a network down exception with the provided BundleMessage key.
	 * 
	 * @param key
	 *            message key
	 * @param message
	 *            detailed message
	 */
	public NetworkDownException(String key, Object message) {
		super(key, message);
	}
}
