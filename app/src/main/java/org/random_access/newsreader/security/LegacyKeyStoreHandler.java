package org.random_access.newsreader.security;

import android.content.Context;

import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

public class LegacyKeyStoreHandler implements IKeyStoreHandler {

    private static final String ALGORITHM = "RSA";
    static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";

    private static final String KEY_STORE_PROVIDER = "AndroidKeyStore";
    static final String CIPHER_PROVIDER = "AndroidOpenSSL";

    private static final String X509_CONTENT = "CN=Sample Name, O=Android Authority";

    private static final String CHARSET = "UTF-8";

    private Context context;
    private KeyStore keyStore;

    public LegacyKeyStoreHandler (Context context) throws KeyStoreHandlerException {
        this.context = context;
        try {
            keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER);
            keyStore.load(null);
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public void createKeyPair(String alias) throws KeyStoreHandlerException {
        // Create new key if needed
        try {
            if (!keyStore.containsAlias(alias)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                @SuppressWarnings("deprecation")
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setSubject(new X500Principal(X509_CONTENT))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM, KEY_STORE_PROVIDER);
                generator.initialize(spec);
                generator.generateKeyPair();
            }
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
    public PublicKey getPublicKey(String alias) throws KeyStoreHandlerException {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            return privateKeyEntry.getCertificate().getPublicKey();
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public PrivateKey getPrivateKey(String alias) throws KeyStoreHandlerException {
        try{
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            return privateKeyEntry.getPrivateKey();
        } catch (Exception e) {
            throw new KeyStoreHandlerException(e.getMessage());
        }
    }

    @Override
    public String encryptString(String plainText, PublicKey publicKey) throws KeyStoreHandlerException {
        try {
            Cipher inCipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
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
            Cipher output = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
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
