package com.owaconnector.exchange.original;

import org.apache.http.HttpStatus;

public class HttpUtil {
	/**
	 * Check if status is a redirect (various 30x values).
	 * 
	 * @param status
	 *            Http status
	 * @return true if status is a redirect
	 */
	public static boolean isRedirect(int status) {
		return status == HttpStatus.SC_MOVED_PERMANENTLY
				|| status == HttpStatus.SC_MOVED_TEMPORARILY
				|| status == HttpStatus.SC_SEE_OTHER
				|| status == HttpStatus.SC_TEMPORARY_REDIRECT;
	}
}
