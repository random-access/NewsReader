package org.random_access.newsreader.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

public interface IKeyStoreHandler {

    /**
     * Creates a new key pair (public key and private key)
     * @param alias String identifying a key pair.
     * @throws KeyStoreHandlerException wraps any kind of exception occurring here
     */
    void createKeyPair(String alias) throws KeyStoreHandlerException;

    /**
     * Deletes the key pair with the given alias
     * @param alias String identifying a key pair.
     * @throws KeyStoreHandlerException wraps any kind of exception occurring here
     */
    void deleteKeyPair(String alias) throws KeyStoreHandlerException;

    /**
     * Returns a list containing all key aliases from the key store
     * @return all key aliases
     *  @throws KeyStoreHandlerException wraps any kind of exception occurring here
     */
    Enumeration<String> getKeyAliases() throws KeyStoreHandlerException ;

    /**
     * Returns the public key stored under the given alias
     * @param alias String identifying a key pair
     * @return the public RSA key
     * @throws KeyStoreHandlerException wraps any kind of exception occurring here
     */
    PublicKey getPublicKey(String alias) throws KeyStoreHandlerException;

    /**
     * Returns the public key stored under the given alias
     * @param alias String identifying a key pair
     * @return the public RSA key
     * @throws KeyStoreHandlerException wraps any kind of exception occurring here
     */
    PrivateKey getPrivateKey(String alias) throws KeyStoreHandlerException;

    /**
     * Encrypts a given plaintext with the public key identified by its alias and returns ciphertext.
     * @param input plaintext that should be encrypted
     * @param key public key that should be used for encryption
     * @return the corresponding ciphertext
     * @throws KeyStoreHandlerException wraps any kind of exception occurring here
     *
     */
    String encryptString(String input, PublicKey key) throws KeyStoreHandlerException;

    /**
     * Decrypts a given ciphertext with the private key identified by its alias and returns plaintext.
     * @param input ciphertext that should be decrypted
     * @param key private key that should be used for decryption
     * @return the corresponding plaintext
     * @throws KeyStoreHandlerException wraps any kind of exception occurring here
     */
    String decryptString(String input, PrivateKey key) throws KeyStoreHandlerException;

}
