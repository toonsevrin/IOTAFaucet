package com.sevrin.toon.IOTAFaucet.iota;

import jota.pow.JCurl;

/**
 * Created by toonsev on 6/15/2017.
 */
public class SuperICurl extends JCurl {
    @Override
    public JCurl absorb(int[] trits, int offset, int length) {
        do {
            System.arraycopy(trits, offset, getState(), 0, length < 243 ? length : 243);
            length -= 243;
            if (length > 243) {
                this.transform();
                offset += 243;
            }
        } while (length > 0);

        return this;
    }
}
