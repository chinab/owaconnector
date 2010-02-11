package com.owaconnector.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

public interface ClientFacade {

	/**
	 * Get Http Status code for the given URL without any authentication.
	 * 
	 * @param url
	 *            url string
	 * @return HttpStatus code
	 * @throws IOException
	 *             on error
	 */
	public abstract int getHttpStatus(String url);

	/**
	 * Check the cookie store if a session cookie is present.
	 * 
	 * @return boolean indicating if the client is authenticated.
	 */
	public abstract boolean isAuthenticated();

	/**
	 * Execute a GET request.
	 * 
	 * @param url
	 *            URL of the GET request
	 * @param redirecting
	 *            boolean indicating if the client should follow redirects
	 * @return InputStream Content of the final page.
	 * @throws IOException
	 */
	public abstract InputStream executeGet(String url, boolean redirecting) throws IOException;

	/**
	 * Execute a GET request.
	 * 
	 * @param url
	 *            URL of the GET request
	 * @param redirecting
	 *            boolean indicating if the client should follow redirects
	 * @return InputStream Content of the final page.
	 * @throws IOException
	 */
	public abstract InputStream executeGet(String url, boolean redirecting, List<Header> headers)
			throws IOException;

	/**
	 * Execute a POST request.
	 * 
	 * @param url
	 *            URL of the POST request
	 * @param formparams
	 *            parameters that should be submitted in the POST request.
	 * @return InputStream Content of the final page.
	 */
	public abstract InputStream executePost(String url, List<NameValuePair> formparams,
			boolean redirecting);

	/**
	 * Execute PROPFIND method.
	 * 
	 * @param path
	 *            URL of the PROPFIND method
	 * @param depth
	 *            depth of the PROPFIND method
	 * @param properties
	 *            additional properties for the PROPFIND method.
	 * @return MultiStatusResponse[] list of MultiStatusResponse
	 * @throws IOException
	 */
	public abstract MultiStatusResponse[] executePropFindMethod(String path, int depth,
			DavPropertyNameSet properties) throws IOException;

	public abstract MultiStatusResponse[] executeSearchMethod(String encodePath, String searchQuery);

	public abstract void setAuthentication(String username, String password);

}