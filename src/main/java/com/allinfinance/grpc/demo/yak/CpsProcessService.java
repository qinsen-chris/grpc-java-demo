package com.allinfinance.grpc.demo.yak;

import com.allinfinance.grpc.demo.CpsProcessProto;
import com.allinfinance.grpc.demo.CpsProcessServiceGrpc;
import com.allinfinance.grpc.demo.YakMessageProto;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class CpsProcessService extends CpsProcessServiceGrpc.CpsProcessServiceImplBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

//    @Resource(name="creditAuthorizationService")
//    private AuthorizationService creditAuthorizationService;

    @Override
    public void authorize(CpsProcessProto.YakMessageRequest request, StreamObserver<CpsProcessProto.YakMessageResponse> responseObserver) {
        logger.info("QueryRequest: " + request);

        try {
            // 模拟接口
            // YakMessageProto.YakMessage = creditAuthorizationService.authorize(request.getYakMessage());

            Map<Integer,String> map = new HashMap<>();
            map.put(1,"a");
            map.put(2,"b");
            YakMessageProto.YakMessage yakMessage = request.getYakMessage().toBuilder()
                    .putAllBodyAttributes(map)
                    .build();

            CpsProcessProto.YakMessageResponse rsp = CpsProcessProto.YakMessageResponse.newBuilder()
                    .setYakMessage(yakMessage)
                    .build();
            responseObserver.onNext(rsp);

        } catch (Exception e) {
            logger.error("query error", e);
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
