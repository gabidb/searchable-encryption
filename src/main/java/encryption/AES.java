package encryption;

public class AES {

    final int rounds = 10; //10 iterations for AES-128
    public byte[] encrypt(byte[] block, byte[] key) {
        byte[] state = block;
        byte[] round_key = key;

//        for(int i = 0; i < rounds; i++) {
//            state = AddRoundKey(state, round_key);
//            state = SubBytes(state);
//            state = ShiftRows(state);
//            state = MixColumns(state);
//            round_key = KeySchedule(round_key);
//        }
//        state = AddRoundKey(state, round_key);
        return state;
    }
}
