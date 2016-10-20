package com.gustz.beehive.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * secure cipher helper
 *
 * @author zhangzhenfeng
 * @since 2016-09-29
 */
public final class SecureCipherHelper {

    private static final String CHARSET = "UTF-8";

    private static Cipher cipher = getCipher();

    private static Cipher getCipher() {
        try {
            return Cipher.getInstance("AES/CBC/NoPadding");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String encrypt(final String encKey, final String content) {
        try {
            init(cipher, Cipher.ENCRYPT_MODE, encKey);
            //　add byte[] to group
            ByteGroup byteGroup = new ByteGroup();
            byteGroup.addBytes(content.getBytes(CHARSET));
            // ... + pad: do padding char
            byte[] padBytes = PKCS7Encoder.encode(byteGroup.size());
            byteGroup.addBytes(padBytes);
            //
            byte[] encrypted = cipher.doFinal(byteGroup.toBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("failed to encrypt the secure field,e.msg=" + e.getMessage());
        }
    }

    public static String decrypt(final String encKey, final String encryptData) {
        try {
            init(cipher, Cipher.DECRYPT_MODE, encKey);
            //
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptData));
            // clear padding char
            decrypted = PKCS7Encoder.decode(decrypted);
            return new String(decrypted, CHARSET);
        } catch (Exception e) {
            throw new IllegalStateException("failed to decrypt the secure field,e.msg=" + e.getMessage());
        }
    }

    private static void init(Cipher cipher, int mode, String encKey) throws InvalidAlgorithmParameterException, InvalidKeyException {
        final byte[] aesKey = Base64.getDecoder().decode(encKey);
        IvParameterSpec iv = new IvParameterSpec(aesKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        cipher.init(mode, secretKeySpec, iv);
    }

    public static String getMd5Key(String content) {
        return getMd5Key(content, ReturnType.HEX);
    }

    public static String getMd5Key(String content, ReturnType retType) {
        return getMsgDigest(content, retType, "MD5");
    }

    public static String getSha1Key(String content) {
        return getSha1Key(content, ReturnType.HEX);
    }

    public static String getSha1Key(String content, ReturnType retType) {
        return getMsgDigest(content, retType, "SHA-1");
    }

    private static String getMsgDigest(String content, ReturnType retType, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.reset();
            digest.update(content.getBytes(CHARSET));
            return retType.toString(digest.digest());
        } catch (Exception e) {
            throw new IllegalStateException(algorithm + " message digest fail,e.msg=" + e.getMessage());
        }
    }

    public enum ReturnType {
        B64 {
            @Override
            public String toString(byte[] bs) {
                return Base64.getEncoder().encodeToString(bs);
            }
        }, HEX {
            @Override
            public String toString(byte[] bs) {
                return Hex.encodeHexString(bs);
            }
        };

        public abstract String toString(byte[] bs);
    }

    public static class PKCS7Encoder {

        private static int BLOCK_SIZE = 32;

        /**
         * get padding char
         *
         * @param count char count
         * @return char
         * @throws UnsupportedEncodingException
         */
        public static byte[] encode(int count) throws UnsupportedEncodingException {
            // padding location
            int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
            if (amountToPad == 0) {
                amountToPad = BLOCK_SIZE;
            }
            // to pad char
            char padChr = (char) (amountToPad & 0xFF);
            String tmp = new String();
            for (int index = 0; index < amountToPad; index++) {
                tmp += padChr;
            }
            return tmp.getBytes(CHARSET);
        }

        /**
         * clear padding char
         *
         * @param decrypted
         * @return char
         */
        public static byte[] decode(byte[] decrypted) {
            int pad = (int) decrypted[decrypted.length - 1];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }
            return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
        }
    }

    public static class ByteGroup {

        private final List<Byte> byteContainer = new ArrayList<Byte>();

        public byte[] toBytes() {
            byte[] bytes = new byte[byteContainer.size()];
            for (int i = 0; i < byteContainer.size(); i++) {
                bytes[i] = byteContainer.get(i);
            }
            return bytes;
        }

        public ByteGroup addBytes(byte[] bytes) {
            for (byte b : bytes) {
                byteContainer.add(b);
            }
            return this;
        }

        public int size() {
            return byteContainer.size();
        }
    }

}
