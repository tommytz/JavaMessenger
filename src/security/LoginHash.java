package security;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginHash {
	private static final int iterations = 10000;
	private static final int keyLength = 512;

	/**
	 * Generates a random 16 byte array to use as a salt when hashing a password.
	 **/
	public static byte[] generateSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	/**
	 * Generates a hash of the inputed password, using the salt provided.
	 **/
	public static byte[] generatePasswordHash(char[] password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] passwordHash = skf.generateSecret(spec).getEncoded();
		return passwordHash;
	}

	/**
	 * Checks the hash of a user inputed password against a stored hashed password
	 * and returns true if they have no differences.
	 **/
	public static boolean validatePassword(char[] passwordInput, String storedPassword, String storedSalt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = fromHex(storedSalt);
		byte[] hash = fromHex(storedPassword);
		byte[] testHash = generatePasswordHash(passwordInput, salt);
		int diff = hash.length ^ testHash.length;
		for (int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}

	/* Byte and hexadecimal conversion methods */

	/**
	 * Converts a byte array value into a hexadecimal string.
	 **/
	public static String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}

	/**
	 * Converts a hexadecimal string into a byte array.
	 **/
	public static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

}
