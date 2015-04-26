package com.example.picoclient.testclient;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class provides methods to encrypt and decrypt file.
 * If multiple crypto algorithms are to be used, the logic should be specified here
 */

public class Encryptor {
    SecretKey key;

    String encryptWithoutPassword(String plaintext){
        byte[] salt = Crypto.generateSalt();
        key = Crypto.generateRandomKey();
        return Crypto.encrypt(plaintext, key, salt);
    }
    String decrypt(String ciphertext, byte[] keyBytes) throws GeneralSecurityException, UnsupportedEncodingException {
        return Crypto.decryptPbkdf2(ciphertext, keyBytes);
    }

//    String encrypt(String plaintext, String password){
//        byte[] salt = Crypto.generateSalt();
//        key = deriveKey(password, salt);
//        return Crypto.encrypt(plaintext, key, salt);
//    }
//    SecretKey deriveKey(String password, byte[] salt){
//        return Crypto.deriveKeyPbkdf2(salt, password);
//    }
//    String decryptWithKey(String ciphertext, SecretKey key) throws GeneralSecurityException, UnsupportedEncodingException {
//        return Crypto.decryptPbkdf2WithKey(ciphertext, key);
//    }
//    String decryptWithPassword(String ciphertext, String password) throws GeneralSecurityException, UnsupportedEncodingException {
//        return Crypto.decryptPbkdf2WithPassword(ciphertext, password);
//    }
}
