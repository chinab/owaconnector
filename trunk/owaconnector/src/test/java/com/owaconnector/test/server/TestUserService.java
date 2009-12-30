package com.owaconnector.test.server;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.owaconnector.exception.DuplicateUserException;
import com.owaconnector.exception.NoSuchUserException;
import com.owaconnector.model.User;
import com.owaconnector.server.service.UserService;
import com.owaconnector.test.ImplTestCase;

public class TestUserService extends ImplTestCase{
	@Autowired
	private UserService userService;

	@Test 
	public void TestAddAndDeleteUser() throws DuplicateUserException, NoSuchUserException{
		String username = "TestAddAndDeleteUser";
		User user = new User(username,"TestPassword","TestEmail","TestDisplay");
		userService.createUser(user);
		User user2 = userService.getUser(username);
		Assert.assertNotNull(user2);
		userService.deleteUser(user2);
		
	}
	
	@Test
	public void TestDuplicateUser() {
		String username = "TestDuplicateUser";
		User user = new User(username,"TestPassword","TestEmail","TestDisplay");
		try {
			userService.createUser(user);
		} catch (DuplicateUserException e) {
			Assert.fail("The user should not exist, but is already present");
		}
		User user2 = new User(username,"TestPassword","TestEmail","TestDisplay");
		try {
			userService.createUser(user2);
		} catch (DuplicateUserException expected) {
			//expected
		}

		try {
			userService.deleteUser(user);
		} catch (NoSuchUserException e) {
			Assert.fail("The first user should be present, but isn't");
		}
			
	}
	
	@Test (expected=NoSuchUserException.class)
	public void TestNoSuchUser() throws DuplicateUserException, NoSuchUserException{
		String username = "TestNoSuchUser";
		userService.getUser(username);
			
	}
	

}
