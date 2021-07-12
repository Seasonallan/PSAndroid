package com.ripple.word;

import java.text.Normalizer;

/**
 * @author QuincySx
 * @date 2018/5/15 上午10:40
 */
final class Normalization {
    static String normalizeNFKD(final String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFKD);
    }

    static char normalizeNFKD(final char c) {
        return normalizeNFKD("" + c).charAt(0);
    }
}
