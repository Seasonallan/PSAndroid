package com.ripple.word;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static com.ripple.word.Normalization.normalizeNFKD;

/**
 * @author QuincySx
 * @date 2018/5/15 上午10:38
 */
public final class SeedCalculator {
    private final byte[] fixedSalt = getUtf8Bytes("mnemonic");
    private final PBKDF2WithHmacSHA512 hashAlgorithm;

    public SeedCalculator(final PBKDF2WithHmacSHA512 hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public SeedCalculator() {
        this(SpongyCastlePBKDF2WithHmacSHA512.INSTANCE);
    }

    public byte[] calculateSeed(final List<String> mnemonicList, final String passphrase) {
        if (mnemonicList == null || mnemonicList.size() < 3) {
            throw new RuntimeException("The dictionary cannot be empty and the number of words must not be less than 3");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : mnemonicList) {
            stringBuilder.append(str).append(" ");
        }
        String mnemonic = stringBuilder.substring(0, stringBuilder.length() - 1);
        return calculateSeed(mnemonic, passphrase);
    }

    public byte[] calculateSeed(final String mnemonic, final String passphrase) {
        final char[] chars = normalizeNFKD(mnemonic).toCharArray();

        try {
            return calculateSeed(chars, passphrase);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    byte[] calculateSeed(final char[] mnemonicChars, final String passphrase) {
        final String normalizedPassphrase = normalizeNFKD(passphrase);
        final byte[] salt2 = getUtf8Bytes(normalizedPassphrase);
        final byte[] salt = combine(fixedSalt, salt2);
        clear(salt2);
        final byte[] encoded = hash(mnemonicChars, salt);
        clear(salt);
        return encoded;
    }

    private static byte[] combine(final byte[] array1, final byte[] array2) {
        final byte[] bytes = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, bytes, 0, array1.length);
        System.arraycopy(array2, 0, bytes, array1.length, bytes.length - array1.length);
        return bytes;
    }

    private static void clear(final byte[] salt) {
        Arrays.fill(salt, (byte) 0);
    }

    private byte[] hash(final char[] chars, final byte[] salt) {
        return hashAlgorithm.hash(chars, salt);
    }

    private static byte[] getUtf8Bytes(final String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
