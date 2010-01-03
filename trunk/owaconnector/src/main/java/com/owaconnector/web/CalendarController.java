package com.owaconnector.web;

import java.security.PrivateKey;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.exception.NoCalendarFoundException;
import com.owaconnector.exception.OwaCryptoException;
import com.owaconnector.service.CalendarService;
import com.owaconnector.service.ConfigurationService;
import com.owaconnector.service.PasswordService;

@RequestMapping("/calendar/**")
@Controller
public class CalendarController {

	@Autowired
	private PasswordService passwordService;
	@Autowired
	private CalendarService calendarService;
	@Autowired
	private ConfigurationService configurationService;

	@RequestMapping
	public String getCalendar(@RequestParam("token") String token,
			@RequestParam("key") String encryptionKey, ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) throws NoCalendarFoundException {

		try {

			CalendarConfiguration result = configurationService.getConfigurationForToken(token);
			PrivateKey privateKey;

			//privateKey = passwordService.constructPrivateKey(encryptionKey);

			String password = result.getPasswordEncrypted();
//			byte[] decrypt = passwordService.decrypt(password.getBytes(),
//					privateKey);
			String decryptedPassword = password;// new String(decrypt);

			StringBuilder calendar = calendarService.getCalendar(result,
					decryptedPassword);
			if (calendar.length() > 0) {

				modelMap.addAttribute("calendar", calendar);
				return "calendar/get";
			}
		} catch (OwaCryptoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoResultException e) {
			throw new NoCalendarFoundException();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
