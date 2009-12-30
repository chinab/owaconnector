package com.owaconnector.server.controller;

import java.security.PrivateKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import com.owaconnector.exception.OwaCryptoException;
import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.server.service.CalendarService;
import com.owaconnector.server.service.ConfigurationService;
import com.owaconnector.server.service.PasswordService;

@Controller
public class OwaConnectorController {

	public final String TOKEN = "token";
	public final String ENCRYPTION_KEY = "key";
	@Autowired
	private CalendarService calendarService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private PasswordService passwordService;

	@RequestMapping("/calendar/*")
	public ModelAndView get(HttpServletRequest req, HttpServletResponse resp) {
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
