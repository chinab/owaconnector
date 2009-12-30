package com.owaconnector.server.controller;

import java.security.PrivateKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import com.owaconnector.exception.OwaCryptoException;
import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.server.service.CalendarService;
import com.owaconnector.server.service.ConfigurationService;
import com.owaconnector.server.service.PasswordService;

@Controller
public class CalendarController extends AbstractController {

	public final String TOKEN = "token";
	public final String ENCRYPTION_KEY = "key";
	
	@Autowired
	private CalendarService calendarService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private PasswordService passwordService;

	/**
	 * @return the calendarService
	 */
	public CalendarService getCalendarService() {
		return calendarService;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * @return the passwordService
	 */
	public PasswordService getPasswordService() {
		return passwordService;
	}

	/**
	 * @param calendarService
	 *            the calendarService to set
	 */
	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	/**
	 * @param configurationService
	 *            the configurationService to set
	 */
	public void setConfigurationService(
			ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/**
	 * @param passwordService
	 *            the passwordService to set
	 */
	public void setPasswordService(PasswordService passwordService) {
		this.passwordService = passwordService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		ModelAndView result = new ModelAndView();
		result.setViewName("welcome");
		String token = req.getParameter(TOKEN);
		String encryptionKey = req.getParameter(ENCRYPTION_KEY);
		if (!StringUtils.isBlank(token) && !StringUtils.isBlank(encryptionKey)) {
			try {
				CalendarConfiguration config = null;
				config = configurationService.getConfigurationForToken(token);
				PrivateKey privateKey;

				privateKey = passwordService.constructPrivateKey(encryptionKey);

				byte[] bytes = config.getPassword();
				byte[] decrypt = passwordService.decrypt(bytes, privateKey);
				String decryptedPassword = new String(decrypt);

				StringBuilder calendar = calendarService.getCalendar(config,
						decryptedPassword);
				if (calendar.length() > 0) {
					result.addObject("calendar", calendar);
					result.setViewName("calendar");
				}
			} catch (OwaCryptoException e) {
				resp.setStatus(404);
			} catch (Exception e) {
				resp.setStatus(404);
			}

		}
		return result;
	}

}
