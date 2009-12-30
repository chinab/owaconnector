package com.owaconnector.crypt;

// Source: http://www.informit.com/articles/article.aspx?p=170967&seqNum=4

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * The Cryptor class can encrypt or decrypt bytes using the JCE framework. 
 * It is used by the PasswordService to encrypt and decrypt passwords stored on the server.
 * @author b.walet
 *
 */
public class Cryptor {
	public static byte[] encrypt(byte[] inpBytes, PublicKey key, String xform, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Security.addProvider(provider);

		Cipher cipher = Cipher.getInstance(xform);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(inpBytes);
	}

	public static byte[] decrypt(byte[] inpBytes, PrivateKey key, String xform, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
			 {
		Security.addProvider(provider);

		Cipher cipher = Cipher.getInstance(xform);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(inpBytes);
	}

}
