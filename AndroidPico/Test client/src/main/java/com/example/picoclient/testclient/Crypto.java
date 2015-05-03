package com.example.picoclient.testclient;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is a utility class that provides the cryptographic operations we
 * might want to do on text strings. Used mainly by Encryptor instances.
 */

public class Crypto {
    private static final String TAG = Crypto.class.getSimpleName();
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int PKCS5_SALT_LENGTH = 8;
    private static String DELIMITER = "]";
    // AES uses 256 bit
    private static int KEY_LENGTH = 256;
    private static SecureRandom random = new SecureRandom();

    public static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    public static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);
        return b;
    }

    public static String encrypt(String plaintext, SecretKey key, byte[] salt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            byte[] iv = generateIv(cipher.getBlockSize());
            Log.d(TAG, "IV: " + toHex(iv));
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: "
                    + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            if (salt != null) {
                return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER,
                        toBase64(iv), DELIMITER, toBase64(cipherText));
            }

            return String.format("%s%s%s", toBase64(iv), DELIMITER,
                    toBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    public static String decrypt(byte[] cipherBytes, SecretKey key, byte[] iv) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
        byte[] plaintext = cipher.doFinal(cipherBytes);
        String plainrStr = new String(plaintext, "UTF-8");
        return plainrStr;
    }

    /*
    Uses the secretKey stored on server to decrypt. This is in contrast
    with deriving secretKey
    from password
    */
    public static String decryptPbkdf2(String ciphertext, byte[] keyBytes) throws GeneralSecurityException, UnsupportedEncodingException {
        String[] fields = ciphertext.split(DELIMITER);
        if (fields.length != 3) {
            throw new IllegalArgumentException("Invalid encypted text format");
        }
        byte[] iv = fromBase64(fields[1]);
        byte[] cipherBytes = fromBase64(fields[2]);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        return decrypt(cipherBytes, key, iv);
    }

    public static SecretKey generateRandomKey() {
        byte[] b = new byte[KEY_LENGTH / 8];
        random.nextBytes(b);
        return new SecretKeySpec(b, "AES");
    }
}
