package com.owaconnector.test.server.stub;

import org.springframework.stereotype.Component;

import com.owaconnector.exception.NoSuchUserException;
import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;
import com.owaconnector.server.service.UserService;

@Component
public class UserServiceStub implements UserService {

	
	public void createUser(User user) {
	//	db.createDocument(user);
	}

	public void deleteUser(User user) {
//		db.delete(user);
	}

	public User getUserForConfiguration(CalendarConfiguration config) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveUser(User user) {
	}

	public User getUser(String username) throws NoSuchUserException {
		//db.
	//	throw new NoSuchUserException("User not found");
		return null;
	}

}
