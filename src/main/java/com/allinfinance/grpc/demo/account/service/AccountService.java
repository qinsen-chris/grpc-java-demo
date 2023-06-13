/*
 * Copyright 2011-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See `LICENSE` in the project root for license information.
 */

package com.allinfinance.grpc.demo.account.service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.rpc.Code;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import com.allinfinance.grpc.demo.AccountProto;
import com.allinfinance.grpc.demo.AccountServiceGrpc;

/**
 * 账户服务实现
 *
 */
public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int DATA_COUNT = 1024;

    /**
     * {@inheritDoc}
     */
    @Override
    public void query(AccountProto.QueryRequest request,
            StreamObserver<AccountProto.QueryResponse> responseObserver) {
        logger.info("QueryRequest: " + request);
        try {
            AccountProto.QueryResponse rsp = AccountProto.QueryResponse.newBuilder()
                    .setCode(Code.OK)
                    .setSerialNo(request.getSerialNo())
                    .setMsg("OK; User: " + request.getUserId())
                    .setAmount(666.6601D)
                    .build();
            responseObserver.onNext(rsp);
        } catch (Exception e) {
            logger.error("query error", e);
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queryServerStreaming(AccountProto.QueryRequest request,
            StreamObserver<AccountProto.QueryResponse> responseObserver) {
        logger.info("QueryRequest: " + request);
        for (int i = 1; i <= DATA_COUNT; i++) {
            AccountProto.QueryResponse rsp = AccountProto.QueryResponse.newBuilder()
                    .setCode(Code.OK)
                    .setSerialNo(request.getSerialNo() + '#' + i)
                    .setMsg("OK; User: " + request.getUserId())
                    .setAmount(this.calcAmount(666.6602D, i))
                    .build();
            responseObserver.onNext(rsp);
        }
        responseObserver.onCompleted();
    }

    /**
     * 计算金额
     *
     * @param amount 金额
     * @param i 顺序
     * @return 金额
     * @since
     */
    private double calcAmount(double amount, int i) {
        BigDecimal val = BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(i));
        return val.doubleValue();
    }

    /**
     * {@inheritDoc}
     *
     * @since Rename: queryClientStreaming -> addClientStreaming
     * @since 修改参数为 AddRequest、AddResponse
     * @since 服务端的观察者使用 {@link ServerCallStreamObserver}
     */
    @Override
    public StreamObserver<AccountProto.AddRequest> addClientStreaming(
            StreamObserver<AccountProto.AddResponse> responseObserver) {

        // 返回 observer 应对多个请求对象
        return new ServerCallStreamObserver<AccountProto.AddRequest>() {

            private final AtomicInteger count = new AtomicInteger(0);

            private final ThreadLocal<String> localSerialNo = new InheritableThreadLocal<>();

            @Override
            public void onNext(AccountProto.AddRequest request) {
                logger.info("AddRequest: {}", request);
                this.localSerialNo.set(request.getSerialNo());
                // 获取客户端推送的流数据，处理成功（比如添加到数据库中）后返回结果
                this.count.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                // other side closed with non OK
                logger.error("Build response error", t);
            }

            @Override
            public void onCompleted() {
                // other side closed with OK
                AccountProto.AddResponse rsp = AccountProto.AddResponse.newBuilder()
                        .setCode(Code.OK)
                        .setSerialNo(this.localSerialNo.get())
                        .setMsg("All users added.")
                        .setCount(this.count.get())
                        .build();
                responseObserver.onNext(rsp);
                responseObserver.onCompleted();
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
                logger.info("request: " + count);
            }

            @Override
            public void setMessageCompression(boolean enable) {
                logger.info("setMessageCompression: " + enable);
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void setOnCancelHandler(Runnable onCancelHandler) {
                logger.info("setOnCancelHandler");
            }

            @Override
            public void setCompression(String compression) {
                logger.info("setCompression: " + compression);
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @since
     * @since 服务端的观察者使用 {@link ServerCallStreamObserver}
     */
    @Override
    public StreamObserver<AccountProto.QueryRequest> queryBidiStreaming(
            StreamObserver<AccountProto.QueryResponse> responseObserver) {

        // 返回observer应对多个请求对象
        return new ServerCallStreamObserver<AccountProto.QueryRequest>() {

            private final AccountProto.QueryResponse.Builder builder = AccountProto.QueryResponse.newBuilder();

            @Override
            public void onNext(AccountProto.QueryRequest request) {
                logger.info("QueryRequest: {}", request);
               // for (int i = 1; i <= DATA_COUNT; i++) {
                for (int i = 1; i <= 2; i++) {
                    AccountProto.QueryResponse rsp = this.builder.setSerialNo(request.getSerialNo() + '#' + i)
                            .setCode(Code.OK)
                            .setMsg("OK; User: " + request.getUserId())
                            .setAmount(calcAmount(666.6603D, i))
                            .build();
                    responseObserver.onNext(rsp);
                }
            }

            @Override
            public void onError(Throwable t) {
                // other side closed with non OK
                logger.error("Build response error", t);
            }

            @Override
            public void onCompleted() {
                // other side closed with OK
                responseObserver.onCompleted();
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
                logger.info("request: " + count);
            }

            @Override
            public void setMessageCompression(boolean enable) {
                logger.info("setMessageCompression: " + enable);
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void setOnCancelHandler(Runnable onCancelHandler) {
                logger.info("setOnCancelHandler");
            }

            @Override
            public void setCompression(String compression) {
                logger.info("setCompression: " + compression);
            }
        };
    }

}
