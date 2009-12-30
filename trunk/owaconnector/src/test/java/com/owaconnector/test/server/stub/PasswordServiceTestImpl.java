package com.owaconnector.test.server.stub;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.stereotype.Component;

import com.owaconnector.exception.OwaCryptoException;
import com.owaconnector.server.service.PasswordService;
import com.owaconnector.server.service.impl.PasswordServiceImpl;

@Component
public class PasswordServiceTestImpl implements PasswordService {

	private PasswordService passwordService;
	public PasswordServiceTestImpl()
	{
		this.passwordService = new PasswordServiceImpl();
	}
	/**
	 * @param encodedKey
	 * @return
	 * @throws OwaCryptoException
	 * @see com.owaconnector.server.service.PasswordService#constructPrivateKey(java.lang.String)
	 */
	public PrivateKey constructPrivateKey(String encodedKey)
			throws OwaCryptoException {
		return passwordService.constructPrivateKey(encodedKey);
	}
	/**
	 * @param hash
	 * @param prvk
	 * @return
	 * @throws OwaCryptoException
	 * @see com.owaconnector.server.service.PasswordService#decrypt(byte[], java.security.PrivateKey)
	 */
	public byte[] decrypt(byte[] hash, PrivateKey prvk)
			throws OwaCryptoException {
		return passwordService.decrypt(hash, prvk);
	}
	/**
	 * @param password
	 * @param publickey
	 * @return
	 * @throws OwaCryptoException 
	 * @see com.owaconnector.server.service.PasswordService#encrypt(byte[], java.security.PublicKey)
	 */
	public byte[] encrypt(byte[] password, PublicKey publickey) throws OwaCryptoException {
		return passwordService.encrypt(password, publickey);
	}
	/**
	 * @return
	 * @throws OwaCryptoException
	 * @see com.owaconnector.server.service.PasswordService#generateKeys()
	 */
	public KeyPair generateKeys() throws OwaCryptoException {
		return passwordService.generateKeys();
	}
	/**
	 * @param pk
	 * @return
	 * @see com.owaconnector.server.service.PasswordService#getStringRepresentation(java.security.PrivateKey)
	 */
	public String getStringRepresentation(PrivateKey pk) {
		return passwordService.getStringRepresentation(pk);
	}
	
}
