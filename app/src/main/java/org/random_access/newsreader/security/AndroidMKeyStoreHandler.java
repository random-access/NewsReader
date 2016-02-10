package org.random_access.newsreader.security;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public class AndroidMKeyStoreHandler implements IKeyStoreHandler {

    static final String CIPHER_TYPE = "RSA/ECB/OAEPWithSHA-512AndMGF1Padding";

    private static final String KEY_STORE_PROVIDER = "AndroidKeyStore";

    private static final String CHARSET = "UTF-8";

    private KeyStore keyStore;

    public AndroidMKeyStoreHandler() throws KeyStoreHandlerException {
        try {
            keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER);
            keyStore.load(null);
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void createKeyPair(String alias) throws KeyStoreHandlerException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_PROVIDER);
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            alias,
                            KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .build());
            keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public void deleteKeyPair(String alias) throws KeyStoreHandlerException {
        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public Enumeration<String> getKeyAliases() throws KeyStoreHandlerException {
        try {
            return keyStore.aliases();
        } catch (Exception e) {
            throw  new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public boolean hasKeyWithAlias(String alias) throws KeyStoreHandlerException{
        try {
            return keyStore.containsAlias(alias);
        } catch (Exception e) {
            throw  new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public PublicKey getPublicKey(String alias) throws KeyStoreHandlerException {
        try {
            return keyStore.getCertificate(alias).getPublicKey();
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public PrivateKey getPrivateKey(String alias) throws KeyStoreHandlerException {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            return privateKeyEntry.getPrivateKey();
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public String encryptString(String plainText, PublicKey publicKey) throws KeyStoreHandlerException {
        try {
            Cipher inCipher = Cipher.getInstance(CIPHER_TYPE);
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(plainText.getBytes(CHARSET));
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();

            return Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public String decryptString(String cipherText, PrivateKey privateKey) throws KeyStoreHandlerException {
        try {
            Cipher output = Cipher.getInstance(CIPHER_TYPE);
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }

            return new String(bytes, 0, bytes.length, CHARSET);
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }
}
