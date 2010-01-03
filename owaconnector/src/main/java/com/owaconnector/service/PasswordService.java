package com.owaconnector.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.owaconnector.exception.OwaCryptoException;

/**
 * The password service manages functions related to the password that is stored
 * on the server. This service encrypts and decrypts the password. It also
 * converts PrivateKey objects to Strings so it can be The implementation should
 * have a two way encrytion technique using for instance the JCE framework.
 * 
 * @author b.walet
 * 
 */
public interface PasswordService {

	/**
	 * Encrypt the specified password using a public key which is generated for
	 * this user
	 * 
	 * @param password
	 *            the password that should be encrypted
	 * @param publickey
	 *            the public key that is used to encrypt the password
	 * @return the encrypted password as an array of bytes
	 * @throws OwaCryptoException
	 */
	public byte[] encrypt(byte[] password, PublicKey publickey)
			throws OwaCryptoException;

	/**
	 * Decrypt the specified hash using a privatekey
	 * 
	 * @param hash
	 *            the hash that should be decrypted
	 * @param prvk
	 *            the privatekey that should be used to decrypt the password
	 * @return the decrypted hash (password) as an array of bytes
	 * @throws OwaCryptoException
	 */
	public byte[] decrypt(byte[] hash, PrivateKey prvk)
			throws OwaCryptoException;

	/**
	 * Generate a public and private keypair, the public keypair is used to
	 * encrypt the password, the private key is supplied to the user and used in
	 * the servlet to decrypt the password.
	 * 
	 * @return a keypair containing a publickey and privatekey
	 * @throws OwaCryptoException
	 */
	public KeyPair generateKeys() throws OwaCryptoException;

	/**
	 * Construct a privatekey object from a UrlBase64 (bountycastle) encoded
	 * string.
	 * 
	 * @param encodedKey
	 *            the UrlBase64 encoded String representing the privatekey.
	 * @return a PrivateKey object which can be used to decode the password
	 * @throws OwaCryptoException
	 */
	public PrivateKey constructPrivateKey(String encodedKey)
			throws OwaCryptoException;

	/**
	 * Construct a UrlBase64 encoded String from a PrivateKey object. This
	 * String can be exchanged using URL's.
	 * 
	 * @param privateKey
	 *            The privatekey that should be converted to a URL safe string.
	 * @return an URL safe String representation of the privatekey.
	 */
	public String getStringRepresentation(PrivateKey privateKey);

}
