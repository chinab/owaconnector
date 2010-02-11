package com.owaconnector.exchange.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.htmlcleaner.CommentToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.util.StringUtils;

import com.owaconnector.exception.AuthenticationFailedException;
import com.owaconnector.exception.UnknownHttpStatusException;
import com.owaconnector.exchange.ClientFacade;
import com.owaconnector.exchange.ExchangeProperties;
import com.owaconnector.exchange.util.StringUtil;

public class FormLoginDelegate extends AbstractExchangeLoginDelegate {

	private final static Logger LOG = Logger.getLogger(FormLoginDelegate.class);

	protected static final Set<String> USER_NAME_FIELDS = new HashSet<String>();

	static {
		USER_NAME_FIELDS.add("username");
		USER_NAME_FIELDS.add("txtUserName");
		USER_NAME_FIELDS.add("userid");
		USER_NAME_FIELDS.add("SafeWordUser");
	}

	protected static final Set<String> PASSWORD_FIELDS = new HashSet<String>();

	static {
		PASSWORD_FIELDS.add("password");
		PASSWORD_FIELDS.add("txtUserPass");
		PASSWORD_FIELDS.add("pw");
		PASSWORD_FIELDS.add("basicPassword");
	}

	protected static final Set<String> TOKEN_FIELDS = new HashSet<String>();

	static {
		TOKEN_FIELDS.add("SafeWordPassword");
	}

	/**
	 * Logon form user name field, default is username.
	 */
	private String userNameInput = "username";
	/**
	 * Logon form password field, default is password.
	 */
	private String passwordInput = "password";

	public FormLoginDelegate(ClientFacade facade, ExchangeProperties props, String username,
			String password) {
		super(facade, props, username, password);

	}

	public ExchangeProperties doLogin(String url) throws IOException,
			AuthenticationFailedException, UnknownHttpStatusException, HttpException,
			URISyntaxException {

		InputStream loginpage = getFacade().executeGet(url, true);
		InputStream inboxPage = doFormLogin(URI.create(url), loginpage);
		return getWellKnownFolders(inboxPage);
	}

	private InputStream doFormLogin(URI url, InputStream loginpage) {

		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode node;
		try {
			node = cleaner.clean(loginpage);
			loginpage.close();
			List<?> forms = node.getElementListByName("form", true);
			List<?> frameList = node.getElementListByName("frame", true);
			if (forms.size() == 1) {
				return handleFormLogin(url, forms);
			} else if (frameList.size() == 1) {
				return handleFrameLogin(url, frameList);
			} else {
				// another failover for script based logon forms (Exchange
				// 2007)
				return handleScriptLogin(url, node);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		throw new IllegalStateException();
	}

	private InputStream handleScriptLogin(URI uri, TagNode node) throws IOException {
		List<?> scriptList = node.getElementListByName("script", true);
		String sUrl = null;
		String sLgn = null;
		for (Object script : scriptList) {
			List<?> contents = ((TagNode) script).getChildren();
			for (Object content : contents) {
				if (content instanceof CommentToken) {
					String scriptValue = ((CommentToken) content).getCommentedContent();
					sUrl = StringUtil.getToken(scriptValue, "var a_sUrl = \"", "\"");
					sLgn = StringUtil.getToken(scriptValue, "var a_sLgn = \"", "\"");
					if (!StringUtils.hasLength(sLgn)) {
						sLgn = StringUtil.getToken(scriptValue, "var a_sLgnQS = \"", "\"");
					}
				}
			}
		}
		if (StringUtils.hasLength(sUrl) && StringUtils.hasLength(sLgn)) {
			String src = getScriptBasedFormURL(uri, sLgn + sUrl);
			InputStream loginPage = getFacade().executeGet(src, false);
			return doFormLogin(URI.create(src), loginPage);
		}
		// FIXME proper exception
		throw new IllegalStateException();
	}

	private InputStream handleFrameLogin(URI uri, List<?> frameList) throws IOException {
		String src = ((TagNode) frameList.get(0)).getAttributeByName("src");
		if (src != null) {
			LOG.debug("Frames detected in form page, try frame content");
			InputStream loginPage = getFacade().executeGet(src, true);
			return doFormLogin(uri, loginPage);
		}
		// FIXME proper exception
		throw new IllegalStateException();
	}

	private InputStream handleFormLogin(URI uri, List<?> forms) throws IOException,
			UnsupportedEncodingException {
		TagNode form = (TagNode) forms.get(0);
		String logonMethodPath = form.getAttributeByName("action");

		String url = getAbsoluteUri(uri, logonMethodPath);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();

		List<?> inputList = form.getElementListByName("input", true);
		for (Object input : inputList) {
			String type = ((TagNode) input).getAttributeByName("type");
			String name = ((TagNode) input).getAttributeByName("name");
			String value = ((TagNode) input).getAttributeByName("value");
			// copy hidden attributes to new POST method.
			if ("hidden".equalsIgnoreCase(type) && StringUtils.hasLength(name)
					&& StringUtils.hasLength(value)) {
				formparams.add(new BasicNameValuePair(name, value));
			}
			// custom login form
			if (USER_NAME_FIELDS.contains(name)) {
				userNameInput = name;
			} else if (PASSWORD_FIELDS.contains(name)) {
				passwordInput = name;
			} else if ("addr".equals(name)) {
				InputStream newLoginPage = getFacade().executeGet(url, false);
				return doFormLogin(URI.create(url), newLoginPage);
			} else if (TOKEN_FIELDS.contains(name)) {
				throw new UnsupportedOperationException("Token fields are unsupported");
			}
		}
		formparams.add(new BasicNameValuePair(userNameInput, getUsername()));
		formparams.add(new BasicNameValuePair(passwordInput, getPassword()));
		formparams.add(new BasicNameValuePair("trusted", "4"));
		return getFacade().executePost(url, formparams, false);
	}

	// FIXME REWRITE
	private String getAbsoluteUri(URI uri, String path) {
		String currentPath = uri.getPath();
		String scheme = uri.getScheme();
		String authority = uri.getAuthority();
		String newPath = null;
		String fragment = uri.getFragment();
		if (path != null) {
			// reset query string
			// uri.setQuery(null);
			if (path.startsWith("/")) {
				// path is absolute, replace method path
				newPath = path;
			} else if (path.startsWith("http://") || path.startsWith("https://")) {
				return path;
			} else {
				// relative path, build new path
				int end = currentPath.lastIndexOf('/');
				if (end >= 0) {
					newPath = currentPath.substring(0, end + 1) + path;
				}
			}
		}
		try {
			return new URI(scheme, authority, newPath, null, fragment).toString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// FIXME REWRITE
	private String getScriptBasedFormURL(URI inituri, String pathQuery) {
		String scheme = inituri.getScheme();
		String authority = inituri.getAuthority();
		String path = null;
		String query = null;
		String fragment = inituri.getFragment();
		int queryIndex = pathQuery.indexOf('?');
		if (queryIndex >= 0) {
			if (queryIndex > 0) {
				// update path
				String newPath = pathQuery.substring(0, queryIndex);
				if (newPath.startsWith("/")) {
					// absolute path
					path = newPath;
				} else {
					String currentPath = inituri.getPath();
					int folderIndex = currentPath.lastIndexOf('/');
					if (folderIndex >= 0) {
						// replace relative path
						path = currentPath.substring(0, folderIndex + 1) + newPath;
					} else {
						// should not happen
						path = '/' + newPath;
					}
				}
			}
			query = pathQuery.substring(queryIndex + 1);
		}

		try {
			return new URI(scheme, authority, path, query, fragment).toString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// initmethodURI.getURI();
		return null;
	}

}
