/*

 */
package com.owaconnector.security.oauth;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuth.Parameter;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class OAuthProcessingFilter extends GenericFilterBean implements
		InitializingBean {
	// logger
	@com.owaconnector.logger.Logger 
	private org.apache.log4j.Logger log;

	// properties
	private String callbackURL = null;
	private String consumerKey;
	private String consumerSecret;
	private String requestTokenURL;
	private String userAuthorizationURL;
	private String accessTokenURL;

	// objects
	private final static OAuthClient client = new OAuthClient(new HttpClient4());
	private OAuthServiceProvider serviceProvider = null;
	private OAuthConsumer consumer = null;
	private OAuthAccessor accessor = null;

	public void afterPropertiesSet() {
		// Validate required properties.
		if (!StringUtils.hasLength(callbackURL)) {
			throw new IllegalArgumentException("callbackURL must be specified");
		}
		if (!StringUtils.hasLength(consumerKey)) {
			throw new IllegalArgumentException("consumerKey must be specified");
		}
		if (!StringUtils.hasLength(consumerSecret)) {
			throw new IllegalArgumentException(
					"consumerSecret must be specified");
		}
		if (!StringUtils.hasLength(requestTokenURL)) {
			throw new IllegalArgumentException(
					"requestTokenURL must be specified");
		}
		if (!StringUtils.hasLength(userAuthorizationURL)) {
			throw new IllegalArgumentException(
					"userAuthorizationURL must be specified");
		}
		if (!StringUtils.hasLength(accessTokenURL)) {
			throw new IllegalArgumentException(
					"accessTokenURL must be specified");
		}
		// initialize required objects using supplied parameters
		serviceProvider = new OAuthServiceProvider(requestTokenURL,
				userAuthorizationURL, accessTokenURL);
		consumer = new OAuthConsumer(callbackURL, consumerKey, consumerSecret,
				serviceProvider);
		accessor = new OAuthAccessor(consumer);

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		doFilterHttp((HttpServletRequest) request,
				(HttpServletResponse) response, chain);

	}

	private void doFilterHttp(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String OAuthRequestToken = (String) request.getSession().getAttribute(
				OpenID4JavaConsumer.OAUTH_REQUEST_TOKEN);
		
		
		if (StringUtils.hasLength(OAuthRequestToken)) {
			log.debug("[doFilterHttp] authorized request token: "
					+ OAuthRequestToken);

			try {
				List<Parameter> parameters = OAuth.newList(OAuth.OAUTH_TOKEN,
						OAuthRequestToken);
				client.getAccessToken(accessor, OAuthMessage.GET, parameters);
				log.debug("[doFilterHttp] obtained access token: "
						+ accessor.accessToken);
				log.debug("[doFilterHttp] obtained token secret: "
						+ accessor.tokenSecret);
			} catch (OAuthException e) {

			} catch (URISyntaxException e) {
			} finally {
				request.getSession().removeAttribute(OpenID4JavaConsumer.OAUTH_REQUEST_TOKEN);
			}

		}
		chain.doFilter(request, response);
	}

	protected void onSuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException {

		// chain.doFilter(request, response);
	}

	public String getAccessTokenURL() {
		return accessTokenURL;
	}

	public String getCallbackURL() {
		return callbackURL;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public String getRequestTokenURL() {
		return requestTokenURL;
	}

	public String getUserAuthorizationURL() {
		return userAuthorizationURL;
	}

	public void setAccessTokenURL(String accessTokenURL) {
		this.accessTokenURL = accessTokenURL;
	}

	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public void setRequestTokenURL(String requestTokenURL) {
		this.requestTokenURL = requestTokenURL;
	}

	public void setUserAuthorizationURL(String userAuthorizationURL) {
		this.userAuthorizationURL = userAuthorizationURL;
	}

}
