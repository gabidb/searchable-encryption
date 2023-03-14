package encryption;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AES {

    private final int rounds = 10; //10 iterations for AES-128
    private final byte[][] keys;
    public AES(byte[] key) {
        KeySchedule keySchedule = new KeySchedule();
        keys = keySchedule.keyExpansion(rounds + 1, key);
    }

    public String encrypt(String plain_text) {
        List<String> strings = splitStringIntoBlocks(plain_text, 16);
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            byte[] encryptedMessage = encryptBlock(stringToBlock16(s));
            sb.append(new String(encryptedMessage, StandardCharsets.ISO_8859_1));
        }
        return sb.toString();
    }

    public String decrypt(String encrypted_text) {
        byte[] encryptedBytes = encrypted_text.getBytes(StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder();
        int blockSize = 16;
        for (int i = 0; i < encryptedBytes.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(encryptedBytes, i, i + blockSize);
            byte[] decryptedMessage = decryptBlock(block);
            sb.append(new String(decryptedMessage, StandardCharsets.UTF_8).replaceAll("\0", ""));
        }
        return sb.toString();
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

    private List<String> splitStringIntoBlocks(String input, int blockSize) {
        List<String> blocks = new ArrayList<>();
        int length = input.length();
        for (int i = 0; i < length; i += blockSize) {
            int endIndex = Math.min(i + blockSize, length);
            blocks.add(input.substring(i, endIndex));
        }
        return blocks;
    }

    private byte[] stringToBlock16(String str) {
        byte[] stringBytes = str.getBytes();
        int len = stringBytes.length;
        byte[] block16 = new byte[16];
        System.arraycopy(stringBytes, 0, block16, 0, Math.min(len, 16));
        return block16;
    }
}
