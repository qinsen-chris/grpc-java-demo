/*
 * Copyright 2011-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See `LICENSE` in the project root for license information.
 */

package com.allinfinance.grpc.demo.account;

import java.math.BigDecimal;
import java.util.Iterator;
import javax.annotation.Nullable;

import com.allinfinance.grpc.demo.AccountProto;
import com.allinfinance.grpc.demo.AccountServiceGrpc;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import com.allinfinance.grpc.demo.account.service.AccountService;

/**
 * 客户端测试
 *
 */
public class ClientTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 单次测试
     *
     * @since
     */
    @Test
    public void testSingle01() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8883)
                .usePlaintext()
                .build();
        // 同步阻塞 stub
        AccountServiceGrpc.AccountServiceBlockingStub stub = AccountServiceGrpc.newBlockingStub(channel);

        AccountProto.QueryRequest request = AccountProto.QueryRequest.newBuilder()
                .setSerialNo("serial#0001")
                .setUserId("user#0001")
                .build();

        AccountProto.QueryResponse rsp = stub.query(request);
        logger.info("QueryResponse: {}", rsp);
        channel.shutdown();
    }

    /**
     * 循环测试
     */
    @Test
    public void testMulti02() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8883)
                .usePlaintext()
                .build();
        // 同步阻塞 stub
        AccountServiceGrpc.AccountServiceBlockingStub stub = AccountServiceGrpc.newBlockingStub(channel);

        AccountProto.QueryRequest.Builder builder = AccountProto.QueryRequest.newBuilder();
        for (int i = 1; i <= 20; i++) {
            long start = System.currentTimeMillis();
            for (int j = 1; j <= 1024; j++) {
                AccountProto.QueryRequest request = builder.setSerialNo("serial#" + i + '#' + j)
                        .setUserId("user#" + i + '#' + j)
                        .build();

                AccountProto.QueryResponse rsp = stub.query(request);
                logger.info("QueryResponse: {}", rsp);
            }
            logger.debug(System.currentTimeMillis() - start + " ms");
        }
        // 在循环外关闭channel 2019-07-16 09:54
        channel.shutdown();
    }

    /**
     * 测试 服务端流式 RPC
     *
     * @since
     */
    @Test
    public void testQueryServerStreaming03() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8883)
                .usePlaintext()
                .build();
        // 同步阻塞 stub
        AccountServiceGrpc.AccountServiceBlockingStub stub = AccountServiceGrpc.newBlockingStub(channel);

        AccountProto.QueryRequest request = AccountProto.QueryRequest.newBuilder()
                .setSerialNo("serial#0001")
                .setUserId("user#0001")
                .build();

        Iterator<AccountProto.QueryResponse> rspIterator = stub.queryServerStreaming(request);
        while (rspIterator.hasNext()) {
            AccountProto.QueryResponse rsp = rspIterator.next();
            logger.info("QueryResponse: {}", rsp);
        }
        channel.shutdown();
    }

    /**
     * @since
     * @since 修改为测试 {@link AccountService#addClientStreaming(io.grpc.stub.StreamObserver)}
     * @since 客户端的观察者使用 {@link ClientCallStreamObserver}
     */
    @Test
    public void testAddClientStreaming04() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8883)
                .usePlaintext()
                .build();
        // 异步 stub
        AccountServiceGrpc.AccountServiceStub asyncStub = AccountServiceGrpc.newStub(channel);

        // client stream
        logger.info("---client stream rpc---");
        StreamObserver<AccountProto.AddResponse> responseObserver = new ClientCallStreamObserver<AccountProto.AddResponse>() {

            @Override
            public void onNext(AccountProto.AddResponse rsp) {
                logger.info("AddResponse: {}", rsp);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Observe response error", t);
            }

            @Override
            public void onCompleted() {
                // 关闭channel
                channel.shutdown();
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setOnReadyHandler(Runnable onReadyHandler) {
                logger.info("setOnReadyHandler");
            }

            @Override
            public void disableAutoInboundFlowControl() {
                logger.info("disableAutoInboundFlowControl");
            }

            @Override
            public void request(int count) {
                logger.info("request: {}", count);
            }

            @Override
            public void setMessageCompression(boolean enable) {
                logger.info("setMessageCompression: {}", enable);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                logger.warn("cancel: " + message, cause);
            }
        };

        StreamObserver<AccountProto.AddRequest> requestObserver = asyncStub.addClientStreaming(responseObserver);
        AccountProto.AddRequest.Builder builder = AccountProto.AddRequest.newBuilder();
        for (int i = 1; i <= 1024; i++) {
            AccountProto.AddRequest request = builder.setSerialNo("Serial#" + i)
                    .setUserId("user#" + i)
                    .setUserName("User#" + i)
                    .setAmount(this.calcAmount(666.6604D, i))
                    .build();
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
        try {
            // 线程休眠，等待异步结果
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("Thread.sleep error", e);
        }
    }

    /**
     * 计算金额
     *
     * @param amount 金额
     * @param i 顺序
     * @return 金额
     */
    private double calcAmount(double amount, int i) {
        BigDecimal val = BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(i));
        return val.doubleValue();
    }

    /**
     * @since
     * @since 客户端的观察者使用 {@link ClientCallStreamObserver}
     */
    @Test
    public void testQueryBidiStreaming05() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8883)
                .usePlaintext()
                .build();
        // 异步 stub
        AccountServiceGrpc.AccountServiceStub asyncStub = AccountServiceGrpc.newStub(channel);

        // bidirectional stream
        logger.info("---bidirectional stream rpc---");
        StreamObserver<AccountProto.QueryResponse> responseObserver = new ClientCallStreamObserver<AccountProto.QueryResponse>() {

            @Override
            public void onNext(AccountProto.QueryResponse rsp) {
                logger.info("QueryResponse: {}", rsp);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Observe response error", t);
            }

            @Override
            public void onCompleted() {
                // 关闭channel
                channel.shutdown();
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setOnReadyHandler(Runnable onReadyHandler) {
                logger.info("setOnReadyHandler");
            }

            @Override
            public void disableAutoInboundFlowControl() {
                logger.info("disableAutoInboundFlowControl");
            }

            @Override
            public void request(int count) {
                logger.info("request: {}", count);
            }

            @Override
            public void setMessageCompression(boolean enable) {
                logger.info("setMessageCompression: {}", enable);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                logger.warn("cancel: " + message, cause);
            }
        };

        StreamObserver<AccountProto.QueryRequest> requestObserver = asyncStub.queryBidiStreaming(responseObserver);
        AccountProto.QueryRequest.Builder builder = AccountProto.QueryRequest.newBuilder();
        for (int i = 1; i <= 2; i++) {
            AccountProto.QueryRequest request = builder.setSerialNo("serial#" + i)
                    .setUserId("user#" + i)
                    .build();
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
        try {
            // 线程休眠，等待异步结果
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("Thread.sleep error", e);
        }
    }

}
