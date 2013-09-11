package com.enterpriseandroid.androidSecurity.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Demonstrates the use of symmetric encryption on Android (AES-256)
 */
public class AESEncryptionHelper {

    private String padding =
            "ISO10126Padding"; //"ISO10126Padding", "PKCS5Padding"

    private byte[] iv;
    private byte[] key;
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	
	public AESEncryptionHelper(byte[] key, byte[] iv) throws NoSuchAlgorithmException,
    NoSuchPaddingException, InvalidKeyException,
    InvalidAlgorithmParameterException {
		this.key = key;
		this.iv = iv;

        initEncryptor();
		initDecryptor();
	}
	
	private void initEncryptor() throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException
    {
	    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
	    //encryptCipher = Cipher.getInstance("AES/ECB/"+padding);
	    encryptCipher = Cipher.getInstance("AES/CBC/" + padding);
	    encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
	}
	
	private  void initDecryptor() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
	    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
	    decryptCipher = Cipher.getInstance("AES/CBC/" + padding);
	    decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
	}
	  
    public byte[] encrypt(byte[] dataBytes) throws IOException{
        ByteArrayInputStream bIn =
                new ByteArrayInputStream(dataBytes);
        @SuppressWarnings("resource")
		CipherInputStream cIn =
                new CipherInputStream(bIn, encryptCipher);
        ByteArrayOutputStream bOut =
                new ByteArrayOutputStream();
        int ch;
        while ((ch = cIn.read()) >= 0) {
          bOut.write(ch);
        }
        return bOut.toByteArray();
    } 

    public byte[] decrypt(byte[] dataBytes) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        CipherOutputStream cOut =
                new CipherOutputStream(bOut, decryptCipher);
        cOut.write(dataBytes);
        cOut.close();
        return bOut.toByteArray();    
    }
    
    public static void main(String[] args) throws Exception {
        String demoMessage =
                "This is a demo message from Java!";

        byte[] demoMesageBytes =
                demoMessage.getBytes();

        //shared secret
        byte[] demoKeyBytes = "abcdefghijklmnop".getBytes();

        // Initialization Vector - usually contains random data along
        // with a shared secret or transmitted along with a message.
        // Not all the ciphers require IV - we use IV in this
        // particular sample
        byte[] demoIVBytes =
                new byte[] {
                        0x00, 0x01, 0x02, 0x03,
                        0x04, 0x05, 0x06, 0x07,
                        0x08, 0x09, 0x0a, 0x0b,
                        0x0c, 0x0d, 0x0e, 0x0f};

        AESEncryptionHelper aesHelper =
                new AESEncryptionHelper(demoKeyBytes, demoIVBytes);
        byte[] encryptedMsg =
                aesHelper.encrypt(demoMesageBytes);
        System.out.println("Encrypted Msg: " +
                new String(encryptedMsg));
        byte[] decryptedMsg =
                aesHelper.decrypt(encryptedMsg);
        System.out.println("Decrypted Msg: " +
                new String(decryptedMsg));
    }    
}
