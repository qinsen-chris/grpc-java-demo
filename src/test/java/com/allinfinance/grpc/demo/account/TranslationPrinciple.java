/*
 * Copyright 2011-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See `LICENSE` in the project root for license information.
 */

package com.allinfinance.grpc.demo.account;

import java.lang.reflect.Field;

/**
 * <a href="https://gitee.com/oschina/bullshit-codes/blob/master/java/Translation.java">翻译的原理实现</a>
 *
 */
public final class TranslationPrinciple {

    public static void main(String[] args) {
        System.out.println("Hello");
    }

    static {
        try {
            Field value = String.class.getDeclaredField("value");
            value.setAccessible(true);
            value.set("Hello", value.get("你好"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
