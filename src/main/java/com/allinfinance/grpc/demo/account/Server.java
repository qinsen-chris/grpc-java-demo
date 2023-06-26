/*
 * Copyright 2011-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See `LICENSE` in the project root for license information.
 */

package com.allinfinance.grpc.demo.account;

import java.io.IOException;

import com.allinfinance.grpc.demo.AccountServiceGrpc;
import com.allinfinance.grpc.demo.CpsProcessServiceGrpc;
import com.allinfinance.grpc.demo.yak.CpsProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;
import com.allinfinance.grpc.demo.account.service.AccountService;

/**
 * 服务器端
 */
public final class Server {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int PORT = 8883;

    private io.grpc.Server server;

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.start(PORT);
        server.await();
    }

    /**
     * @see io.grpc.internal.ServerImpl#start()
     */
    private void start(final int port) {
        AccountServiceGrpc.AccountServiceImplBase accountService = new AccountService();

        CpsProcessServiceGrpc.CpsProcessServiceImplBase cpsProcessService = new CpsProcessService();

        this.server = ServerBuilder.forPort(port)
                .addService(accountService.bindService())
                .addService(cpsProcessService)
                .build();
        try {
            this.server.start();
            logger.info("gRPC started on {} (http)", port);

            this.addHook();
        } catch (IOException e) {
            logger.error("gRPC start error", e);
        }
    }

    /**
     * 阻塞当前线程，直到：
     * <p>所有已提交的任务（包括正在跑的和队列中等待的）都已执行完成；</p>
     * <p>或者到达超时时间；</p>
     * <p>或者线程被中断，抛出InterruptedException。</p>
     */
    private void await() throws InterruptedException {
        // Await termination on the main thread since the gRPC library uses daemon threads.
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    /**
     * 关闭服务
     */
    private void stop() {
        if (this.server != null) {
            this.server.shutdown();
        }
    }

    /**
     * 添加钩子，在程序关闭时自动关闭服务端
     *
     * @since 2020-06-27 13:22
     */
    private void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            this.stop();
            System.err.println("*** server shut down");
        }));
    }

}
