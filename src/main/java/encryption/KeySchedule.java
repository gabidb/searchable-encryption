package encryption;

import java.security.SecureRandom;

public class KeySchedule {

    public byte[][] keyExpansion(int rounds) {
        byte[] round_keys = new byte[16 * rounds];
        byte[] key = generateKey();
        int n = key.length / 4;

        for (int i = 0; i < 4 * rounds; i++) {
            if(i < n) {
                setWord(round_keys, getWord(key, i), i);
            }
            else if(i % n == 0) {
                byte[] word = XOR(getWord(round_keys, i - n), subWord(rotWord(getWord(round_keys, i - 1))));
                word = XOR(word, roundConstants(i / n));
                setWord(round_keys, word, i);
            }
            else if(n > 6 && i % n == 4) {
                byte[] word = XOR(getWord(round_keys, i - n), subWord(getWord(round_keys, i - 1)));
                setWord(round_keys, word, i);
            }
            else {
                byte[] word = XOR(getWord(round_keys, i - n), getWord(round_keys, i - 1));
                setWord(round_keys, word, i);
            }
        }
        return getRoundKeys(round_keys, rounds);
    }
    /*
        Generate a secure random 128-bit key
     */
    private byte[] generateKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        return key;
    }
    /*
        Get 4 bytes from
     */
    private byte[] getWord(byte[] round_key, int index)
    {
        byte[] tmp = new byte[4];
        System.arraycopy(round_key, index * 4, tmp, 0, 3);
        return tmp;
    }

    private void setWord(byte[] round_key, byte[] word, int index)
    {
        System.arraycopy(word, 0, round_key, index * 4, 3);
    }

    private byte[] XOR(byte[] w1, byte[] w2)
    {
        byte[] tmp = new byte[4];
        for (int i = 0; i < 3; i++) {
            tmp[i] = (byte) (w1[i] ^ w2[i]);
        }
        return tmp;
    }

    private byte[] subWord(byte[] word) {
        byte[] tmp = new byte[4];
        for (int i = 0; i < 3; i++) {
            tmp[i] = AesEncryption.SBOX[word[i] & 0xff];
        }
        return tmp;
    }

    private byte[] rotWord(byte[] word) {
        byte[] tmp = new byte[4];
        tmp[0] = word[1];
        tmp[1] = word[2];
        tmp[2] = word[3];
        tmp[3] = word[0];
        return tmp;
    }

    private byte[] roundConstants(int i) {
        byte[] rci = new byte[] {   (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10, (byte) 0x20, (byte) 0x40,
                                    (byte) 0x80, (byte) 0x1B, (byte) 0x36, (byte) 0x6C, (byte) 0xD8, (byte) 0xAB, (byte) 0x4D, (byte) 0x9A,
                                    (byte) 0x2F, (byte) 0x5E, (byte) 0xBC, (byte) 0x63, (byte) 0xC6, (byte) 0x97, (byte) 0x35, (byte) 0x6A,
                                    (byte) 0xD4, (byte) 0xB3, (byte) 0x7D, (byte) 0xFA, (byte) 0xEF, (byte) 0xC5 };
        return new byte[] { rci[i], 0x00, 0x00, 0x00 };
    }

    private byte[][] getRoundKeys(byte[] round_keys, int rounds) {
        byte[][] keys = new byte[rounds][16];
        for (int i = 0; i < rounds; i++) {
            System.arraycopy(round_keys, i * 16, keys[i],0, 16);
        }
        return keys;
    }
}
