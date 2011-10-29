package util;
import java.util.Random;

public class RandBytes {

    private int index = 0;
    private byte[] bytes = new byte[1024 * 1024];

    public RandBytes() {
        new Random().nextBytes(bytes);
    }

    public byte[] get(int k) {
        k = 1024 * k;

        byte[] ret = new byte[k];

        for (int i = 0; i < k; ++i) {
            index++;
            if (index == bytes.length)
                index = 0;

            ret[i] = bytes[index];
        }
        return ret;
    }
}
