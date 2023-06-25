package com.newind.util;

/**
 * Created by newind on 17-2-22.
 */
public class CycleTest {
    private byte[] boundary;
    private int ptr;
    private byte originCode = 0;
    private byte currentCode = 0;
    private byte[] testing;
    public CycleTest(byte[] boundary){
        this.testing = new byte[boundary.length];
        this.boundary = boundary;
        for (byte b : boundary){
            originCode ^= b;
        }
    }

    public boolean isFull(){
        return ptr >= boundary.length;
    }

    public byte get(int i){
        return testing[(i + ptr) % testing.length];
    }

    public void put(byte b){
        if (ptr >= testing.length){
            currentCode ^= get(0);
        }
        currentCode ^= b;
        testing[(ptr++)%testing.length] = b;
        if (ptr > Integer.MAX_VALUE / 2 && (ptr % testing.length) == 0){
            ptr = testing.length;
        }
    }

    public boolean isEqual(){
        if (ptr < testing.length || currentCode != originCode){
            return false;
        }
        for (int i = 0; i < boundary.length; i++){
            if (get(i) != boundary[i])
                return false;
        }
        return true;
    }
}
