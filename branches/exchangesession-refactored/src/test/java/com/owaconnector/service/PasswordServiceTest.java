package com.owaconnector.service;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PasswordServiceTest {

	@Autowired
	private PasswordService passwordService;

	@Test
	public void TestCryptorSameArray() throws Exception {
		byte[] secretData = "String to Encrypt".getBytes();
		KeyPair keyPair = passwordService.generateKeys();

		byte[] encryptedPassword = passwordService.encrypt(secretData, keyPair
				.getPublic());
		byte[] decryptedPassword = passwordService.decrypt(encryptedPassword,
				keyPair.getPrivate());
		boolean expected = java.util.Arrays.equals(secretData,
				decryptedPassword);

		Assert.assertTrue(expected);
		keyPair.getPrivate().getEncoded();
	}

	@Test
	public void TestCryptorStringRepresentationOfPrivateKeySimple()
			throws Exception {
		String secretData = "String to Encrypt";
		String decryptedPassword = encryptDecrypt(secretData);

		boolean expected = java.util.Arrays.equals(secretData.getBytes(),
				decryptedPassword.getBytes());
		Assert.assertTrue(expected);
		Assert.assertEquals(secretData, decryptedPassword);
	}

	@Test
	public void TestCryptorStringRepresentationOfPrivateKeyComplex()
			throws Exception {
		String secretData = "String to Encrypt!@#$%^&*()_+;.,></";
		String decryptedPassword = encryptDecrypt(secretData);

		boolean expected = java.util.Arrays.equals(secretData.getBytes(),
				decryptedPassword.getBytes());
		Assert.assertTrue(expected);
		Assert.assertEquals(secretData, decryptedPassword);
	}

	private String encryptDecrypt(String secretData)
			throws NoSuchAlgorithmException, Exception, InvalidKeySpecException {
		KeyPair keyPair = passwordService.generateKeys();

		byte[] encryptedPassword = passwordService.encrypt(secretData
				.getBytes(), keyPair.getPublic());
		String privateKeyAsString = passwordService
				.getStringRepresentation(keyPair.getPrivate());
		PrivateKey newPrivateKey = passwordService
				.constructPrivateKey(privateKeyAsString);

		byte[] decryptedPassword = passwordService.decrypt(encryptedPassword,
				newPrivateKey);
		return new String(decryptedPassword);
	}
}
