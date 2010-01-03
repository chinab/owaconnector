package com.owaconnector.web;

import java.security.PrivateKey;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.exception.NoCalendarFoundException;
import com.owaconnector.service.CalendarService;
import com.owaconnector.service.PasswordService;

@RequestMapping("/calendar/**")
@Controller
public class CalendarController {

	private final static Logger LOG = Logger
			.getLogger(CalendarController.class);

	@Autowired
	private PasswordService passwordService;
	@Autowired
	private CalendarService calendarService;

	@RequestMapping
	public String getCalendar(@RequestParam("token") String token,
			@RequestParam("key") String encryptionKey, ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response)
			throws NoCalendarFoundException {

		try {
			if (LOG.isDebugEnabled()) {
				StringBuilder msg = new StringBuilder();
				msg.append("getCalendar: ");
				msg.append("session: " + token + " ");
				msg.append("encryptionKey: " + encryptionKey);
				LOG.debug(msg.toString());
			}
			CalendarConfiguration result = (CalendarConfiguration) CalendarConfiguration
					.findCalendarConfigurationsByTokenEquals(token)
					.getSingleResult();
			if (LOG.isDebugEnabled()) {
				StringBuilder msg = new StringBuilder();
				msg.append("getCalendar: ");
				msg.append("session: " + token + " ");
				msg.append("result: " + result.toString());
				LOG.debug(msg.toString());
			}
			PrivateKey privateKey;

			// privateKey = passwordService.constructPrivateKey(encryptionKey);

			String password = result.getPasswordEncrypted();
			// byte[] decrypt = passwordService.decrypt(password.getBytes(),
			// privateKey);
			String decryptedPassword = password;// new String(decrypt);
			if(LOG.isDebugEnabled()) {
				StringBuilder msg = new StringBuilder();
				msg.append("getCalendar: ");
				msg.append("session: " + token + " ");
				msg.append("Password decryption succesfull");
				LOG.debug(msg.toString());
			}

			StringBuilder calendar = calendarService.getCalendar(result,
					decryptedPassword);
			if (calendar != null && calendar.length() > 0) {
				if(LOG.isDebugEnabled()) {
					StringBuilder msg = new StringBuilder();
					msg.append("getCalendar: ");
					msg.append("session: " + token + " ");
					msg.append("Calendar obtained from Exchange");
					LOG.debug(msg.toString());
				}

				modelMap.addAttribute("calendar", calendar);
				return "calendar/get";
			}
		} catch (NoResultException e) {
			if(LOG.isDebugEnabled()) {
				StringBuilder msg = new StringBuilder();
				msg.append("getCalendar: ");
				msg.append("session: " + token + " ");
				msg.append("CalendarConfiguration not found for token ");
				LOG.warn(msg);
			}
			throw new NoCalendarFoundException(e);
		} catch (Exception e) {
			LOG.error("getCalendar: ", e);
			throw new NoCalendarFoundException(e);
		}
		return null;
	}
}
