package com.pqvcf.shared.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cryptographic hashing utility class for SHA-3 hashing operations.
 * Used for integrity protection of compliance proof elements.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class HashUtil {

    private HashUtil() {}

    /**
     * Computes the SHA-3-256 hash of the given input bytes.
     *
     * @param input the bytes to hash
     * @return the SHA-3-256 hash bytes
     */
    public static byte[] sha3(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA3-256 algorithm not available in default JCA providers", e);
        }
    }

    /**
     * Computes the SHA-3-256 hash of a string in UTF-8.
     *
     * @param input the input string
     * @return the SHA-3-256 hash bytes
     */
    public static byte[] sha3(String input) {
        if (input == null) return new byte[0];
        return sha3(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts raw bytes to a hexadecimal string representation.
     *
     * @param bytes the bytes to convert
     * @return the hex string
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
