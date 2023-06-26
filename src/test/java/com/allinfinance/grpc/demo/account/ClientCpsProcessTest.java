/*
 * Copyright 2011-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See `LICENSE` in the project root for license information.
 */

package com.allinfinance.grpc.demo.account;

import com.allinfinance.grpc.demo.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端测试
 *
 */
public class ClientCpsProcessTest {

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
        CpsProcessServiceGrpc.CpsProcessServiceBlockingStub stub =
                CpsProcessServiceGrpc.newBlockingStub(channel);

        YakMessageProto.YakMessage yakMessage = CpsProcessProto.YakMessageRequest.newBuilder()
                .getYakMessageBuilder().putBodyAttributes(1,"hello")
                .build();

        CpsProcessProto.YakMessageRequest request = CpsProcessProto.YakMessageRequest.newBuilder()
                .setYakMessage(yakMessage)
                .build();
        CpsProcessProto.YakMessageResponse rsp = stub.authorize(request);
        logger.info("QueryResponse: {}", rsp);
        channel.shutdown();
    }

}
