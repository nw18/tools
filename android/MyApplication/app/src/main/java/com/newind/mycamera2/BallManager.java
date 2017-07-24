package com.newind.mycamera2;

import java.math.BigInteger;

/**
 * Created by newind on 17-7-24.
 */

public class BallManager {
    public static final int B1C = 35;
    public static final int B1S = 5;
    public static final int B2C = 12;
    public static final int B2S = 2;
    private int[] ball1 = new int[B1C];
    private int[] ball2 = new int[B2C];
    BigInteger length1 = BigInteger.valueOf(ball1.length * ball1.length);
    BigInteger length2 = BigInteger.valueOf(ball2.length * ball2.length);

    public BallManager() {
        for (int i = 0; i < ball1.length; i++) {
            ball1[i] = i + 1;
        }
        for (int i = 0; i < ball2.length; i++) {
            ball2[i] = i + 1;
        }
    }

    public void randomMove(BigInteger value) {
        while (value.compareTo(length1.multiply(length2)) > 0) {
            int res = value.mod(length1).intValue();
            value = value.divide(length1);
            int pos1 = res / ball1.length;
            int pos2 = res % ball1.length;
            res = value.mod(length2).intValue();
            int temp = ball1[pos1];
            ball1[pos1] = ball1[pos2];
            ball1[pos2] = temp;
            pos1 = res / ball2.length;
            pos2 = res % ball2.length;
            value = value.divide(length2);
            temp = ball2[pos1];
            ball2[pos1] = ball2[pos2];
            ball2[pos2] = temp;
        }
    }

    public String randomSelect() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < B1S; i++) {
            sb.append(ball1[i]);
            sb.append(",");
        }
        sb.replace(sb.length() - 1,sb.length(),"\n");
        for (int i = 0; i < B2S; i++) {
            sb.append(ball2[i]);
            sb.append(",");
        }
        sb.replace(sb.length() - 1,sb.length(),"\n");
        return sb.toString();
    }
}
