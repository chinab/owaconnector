package com.owaconnector.test.model;

import java.lang.reflect.Method;

import org.junit.Test;

import com.owaconnector.model.User;
import com.owaconnector.test.ImplTestCase;

public class TestUser extends ImplTestCase {

	@Test
	public void TestUser1() {
		String username = "username";
		String password = "password";
		String emailAddress = "email@domain.com";
		String displayName = "displayName";
		User user = new User(username, password, emailAddress, displayName);

		Method[] methods = user.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			methods[i].getAnnotations();
		}
	}
}
