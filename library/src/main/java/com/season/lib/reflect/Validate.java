package com.season.lib.reflect;


/**
 * 异常输出
 */
class Validate {

    /**
     * 失败输出异常
     * @param expression
     * @param message 异常欣喜
     * @param values 值
     */
    static void isTrue(final boolean expression, final String message, final Object... values) {
        if (expression == false) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }
}
