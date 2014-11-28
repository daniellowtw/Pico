/**
 * This class provides methods to encrypt and decrypt file.
 * If multiple crypto algorithms are to be used, the logic should be specified here
 */

package com.example.picoclient.testclient;

import javax.crypto.SecretKey;

public class Encryptor {
    SecretKey key;
    SecretKey deriveKey(String password, byte[] salt){
        return Crypto.deriveKeyPbkdf2(salt, password);
    }
    String encrypt(String plaintext, String password){
        byte[] salt = Crypto.generateSalt();
        key = deriveKey(password, salt);
        return Crypto.encrypt(plaintext, key, salt);
    }
    String decrypt(String ciphertext, byte[] keyBytes){
        return Crypto.decryptPbkdf2(ciphertext, keyBytes);
    }

    String decryptWithPassword(String ciphertext, String password){
        return Crypto.decryptPbkdf2WithPassword(ciphertext, password);
    }
}
