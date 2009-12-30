package com.owaconnector.server.service.impl;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.UrlBase64;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.owaconnector.crypt.Cryptor;
import com.owaconnector.exception.OwaCryptoException;
import com.owaconnector.server.service.PasswordService;

/**
 * The implementation of the passwordservice uses the Cryptor class to encrypt
 * and decrypt the password. It also converts PrivateKey objects to Strings so
 * it can be transferred using URL's. 
 * 
 * In this implementation the Provider of the Cryptor can be supplied. 
 * 
 * @author b.walet
 * 
 */
@Service
public class PasswordServiceImpl implements PasswordService {

	private static final String encoding = "RSA";
	private final static String xform = "RSA/NONE/PKCS1PADDING";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.server.service.PasswordService#decrypt(java.lang.String,
	 * java.lang.String)
	 */
	public byte[] decrypt(byte[] hash, PrivateKey prvk)
			throws OwaCryptoException {
		try {
			return Cryptor.decrypt(hash, prvk, xform, new BouncyCastleProvider());
		} catch (Exception e) {
			throw new OwaCryptoException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.server.service.PasswordService#encrypt(java.lang.String,
	 * java.lang.String)
	 */
	public byte[] encrypt(byte[] password, PublicKey publickey)
			throws OwaCryptoException {
		try {
			return Cryptor.encrypt(password, publickey, xform, new BouncyCastleProvider());
		} catch (Exception e) {
			throw new OwaCryptoException(e.getMessage(), e);
		}
	}

	public KeyPair generateKeys() throws OwaCryptoException {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(encoding);
			kpg.initialize(512); // 512 is the keysize.
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new OwaCryptoException("Invalid Algorithm", e);
		}
	}

	public PrivateKey constructPrivateKey(String encodedPrivateKey)
			throws OwaCryptoException {
		Assert.notNull(encodedPrivateKey);
		try {
			byte[] base64decoded = UrlBase64.decode(encodedPrivateKey);
			KeyFactory keyFactory;

			keyFactory = KeyFactory.getInstance(encoding);

			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(
					base64decoded);
			return keyFactory.generatePrivate(privSpec);
		} catch (Exception e) {
			throw new OwaCryptoException(e.getMessage(), e);
		}

	}

	public String getStringRepresentation(PrivateKey privateKey) {
		Assert.notNull(privateKey);
		byte[] encoded = privateKey.getEncoded();
		byte[] base64encoded = UrlBase64.encode(encoded);
		return new String(base64encoded);
	}
}
