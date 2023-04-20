package encryption;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class AES {

    private final int rounds;
    private final byte[][] keys;

    private final Logger logger = Logger.getLogger(getClass().getName());

    public AES(byte[] key) {
        switch (key.length) {
            case 16:
                rounds = 10;
                break;
            case 24:
                rounds = 12;
                break;
            case 32:
                rounds = 14;
                break;
            default:
                throw new IllegalArgumentException("Unsupported key size: " + key.length);
        }
        keys = KeySchedule.keyExpansion(rounds + 1, key);
    }

    /**
     Encrypts the given plain text using AES encryption algorithm in CBC (Cipher Block Chaining) mode.
     @param plainText the plain text to be encrypted
     @return the encrypted text in Base64-encoded string format
     */
    public String encrypt(String plainText) {
        try {
            List<String> strings = splitStringIntoBlocks(plainText, 16);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (String s : strings) {
                byte[] encryptedMessage = encryptBlock(stringToBlock16(s));
                outputStream.write(encryptedMessage);
            }
            byte[] encryptedData = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     Reads the content of the given file, encrypts it using CBC (Cipher Block Chaining) mode with the specified key and returns the encrypted data
     @param file the file to encrypt
     @return the encrypted data as a Base64-encoded string, or null if an error occurred during encryption
     */
    public String encryptFile(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String plaintext = new String(data, StandardCharsets.UTF_8);
            logger.info("File with name: " + file.getName() + " encrypted successfully!");
            return encrypt(plaintext);
        } catch (IOException e) {
            logger.info("File with name: " + file.getName() + " encrypted unsuccessfully!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     Decrypts the given encrypted text using AES encryption algorithm in CBC (Cipher Block Chaining) mode.
     @param encryptedText the encrypted text to decrypt
     @return the decrypted text, or null if there was an error during decryption
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(encryptedText.replaceAll("\\s", "").getBytes(StandardCharsets.UTF_8));
            List<byte[]> blocks = splitByteArrayIntoBlocks(encryptedData, 16);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (byte[] block : blocks) {
                byte[] decryptedMessage = decryptBlock(block);
                outputStream.write(decryptedMessage);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
        Splits a byte array into blocks of a specified block size.
     */
    public static List<byte[]> splitByteArrayIntoBlocks(byte[] data, int blockSize) {
        List<byte[]> blocks = new ArrayList<>();
        int numBlocks = (int) Math.ceil((double) data.length / blockSize);
        for (int i = 0; i < numBlocks; i++) {
            int offset = i * blockSize;
            int length = Math.min(blockSize, data.length - offset);
            byte[] block = new byte[length];
            System.arraycopy(data, offset, block, 0, length);
            blocks.add(block);
        }
        return blocks;
    }

    /*
        The encryptBlock method encrypts each 16-byte block.
     */
    private byte[] encryptBlock(byte[] block) {
        AesEncryption aesEncryption = new AesEncryption();
        byte[] state = block;
        byte[] round_key = keys[0];

        state = aesEncryption.addRoundKey(state, round_key);
        for(int i = 0; i < rounds; i++) {
            state = aesEncryption.subBytes(state);
            state = aesEncryption.shiftRows(state);
            state = aesEncryption.mixColumns(state);
            round_key = keys[i+1];
            state = aesEncryption.addRoundKey(state, round_key);
        }
        state = aesEncryption.subBytes(state);
        state = aesEncryption.shiftRows(state);
        state = aesEncryption.addRoundKey(state, keys[keys.length - 1]);
        return state;
    }

    /*
        The decryptBlock method decrypts each 16-byte block.
     */
    private byte[] decryptBlock(byte[] block) {
        AesDecryption aesDecryption = new AesDecryption();
        byte[] state = block;
        byte[] round_key = keys[keys.length - 1];

        state = aesDecryption.addRoundKey(state, round_key);
        state = aesDecryption.invShiftRows(state);
        state = aesDecryption.invSubBytes(state);

        for(int i = rounds; i > 0; i--) {
            round_key = keys[i];
            state = aesDecryption.addRoundKey(state, round_key);
            state = aesDecryption.invMixColumns(state);
            state = aesDecryption.invShiftRows(state);
            state = aesDecryption.invSubBytes(state);
        }
        state = aesDecryption.addRoundKey(state, keys[0]);
        return state;
    }

    /*
        Splits a string into blocks of a specified block size.
     */
    private List<String> splitStringIntoBlocks(String input, int blockSize) {
        List<String> blocks = new ArrayList<>();
        int length = input.length();
        for (int i = 0; i < length; i += blockSize) {
            int endIndex = Math.min(i + blockSize, length);
            blocks.add(input.substring(i, endIndex));
        }
        return blocks;
    }

    /*
        Converts a string into a byte array block of size 16 by truncating or padding the string to fit into 16 bytes.
        If the string is less than 16 bytes, it is padded with zeroes. If the string is greater than 16 bytes,
        it is truncated to the first 16 bytes.
     */
    private byte[] stringToBlock16(String str) {
        byte[] stringBytes = str.getBytes();
        int len = stringBytes.length;
        byte[] block16 = new byte[16];
        System.arraycopy(stringBytes, 0, block16, 0, Math.min(len, 16));
        return block16;
    }
}
