package com.owaconnector.test.server.stub;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;
import com.owaconnector.server.service.ConfigurationService;
import com.owaconnector.util.ConfigurationUtil;

@Component
public class ConfigurationServiceTestImpl implements ConfigurationService {

	private HashMap<String, CalendarConfiguration> confs = new HashMap<String, CalendarConfiguration>();

	/**
	 * 
	 */
	public ConfigurationServiceTestImpl() {

		String username = "fakeusername";
		String encryptedPassword = "fakepassword";
		URI url = URI.create("https://owa.domain.com/exchange");
		int maxDaysInPast = 30;
		CalendarConfiguration config = new CalendarConfiguration(username,
				encryptedPassword.getBytes(), url, maxDaysInPast, null);
		String token = config.getToken();
		confs.put(token, config);
	}

	public void deleteCalendarConfiguration(CalendarConfiguration config) {

	}

	public CalendarConfiguration getConfigurationForToken(String token) {
		return confs.get(token);
	}

	public void saveCalendarConfiguration(CalendarConfiguration config) {
		String token = ConfigurationUtil.generateToken();
		confs.put(token, config);
	}

	public List<CalendarConfiguration> getConfigurationsForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

}
