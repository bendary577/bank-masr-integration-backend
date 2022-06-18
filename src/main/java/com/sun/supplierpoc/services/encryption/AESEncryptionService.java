package com.sun.supplierpoc.services.encryption;

import com.sun.supplierpoc.models.constants.AESConstants;
import oracle.security.crypto.core.InvalidKeyException;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class AESEncryptionService {

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
    }

    public static IvParameterSpec generateIv() {
        return new IvParameterSpec(AESConstants.IV_PARAMETER_SPEC);
    }

    public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, java.security.InvalidKeyException {
        if (input == null || input.equals(""))
            return "";

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, java.security.InvalidKeyException {
        if(cipherText == null)
            return "";

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }

    public String encryptProcess(String plainText) {
        SecretKey key = null;
        IvParameterSpec ivParameterSpec = null;
        String ciphertext = "";
        try {
            key = getKeyFromPassword(AESConstants.PASSWORD, AESConstants.SALT);
            ivParameterSpec = generateIv();

            ciphertext = encrypt(AESConstants.ALGORITHM, plainText, key, ivParameterSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | java.security.InvalidKeyException e) {
            e.printStackTrace();
        }
        return ciphertext;
    }

    public String decryptProcess(String ciphertext) {
        SecretKey key = null;
        IvParameterSpec ivParameterSpec = null;
        String plainText = "";
        try {
            key = getKeyFromPassword(AESConstants.PASSWORD, AESConstants.SALT);
            ivParameterSpec = generateIv();

            plainText = decrypt(AESConstants.ALGORITHM, ciphertext, key, ivParameterSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | java.security.InvalidKeyException e) {
            e.printStackTrace();
        }
        return plainText;
    }
}