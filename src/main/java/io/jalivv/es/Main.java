package io.jalivv.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jalivv.es.conf.EsConfig;
import io.jalivv.es.entity.SaveBlock;
import io.jalivv.es.utils.SysUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description:
 * @author: Jalivv
 * @create: 2022-11-16 11:08
 **/
@Slf4j
public class Main {

    private static final RestHighLevelClient CLIENT = EsConfig.newClientInstance(true);
    private static final RestHighLevelClient CLIENT1 = EsConfig.newClientInstance(false);
    private static final RestHighLevelClient CLIENT2 = EsConfig.newClientInstance(false);
    private static final RestHighLevelClient CLIENT3 = EsConfig.newClientInstance(false);
    private static final RestHighLevelClient CLIENT4 = EsConfig.newClientInstance(false);


    private static LinkedBlockingQueue<SaveBlock> blockQueue = new LinkedBlockingQueue<>(1000);

    private static final int CAPACITY = 1000;

    private static final int CONSUMER_THREAD_SIZE = 4;

    private static void initEs() {
        try {
            if (!existIndex("block")) {
                createIndex("block");
            }
            log.debug("init es success");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean existIndex(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return CLIENT.indices().exists(request, RequestOptions.DEFAULT);
    }

    private static void createIndex(String indexName) throws IOException {
        // 1?????????????????????
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        // 2???????????????????????? IndicesClient,?????????????????????
        CLIENT.indices().create(request, RequestOptions.DEFAULT);

    }

    public static void main(String[] args) {
        //initEs();
        //initConsumerQueue();
        //long s = System.currentTimeMillis();
        //for (int i = 0; i < CAPACITY; i++) {
        //    SaveBlock b = createSaveBlock(i);
        //    try {
        //        blockQueue.put(b);
        //    } catch (InterruptedException e) {
        //        throw new RuntimeException(e);
        //    }
        //}
        //
        //checkComplete();
        //log.info("write {} block to es, time:{}ms", CAPACITY, System.currentTimeMillis() - s);


        new Thread(() -> {
            Scanner in = new Scanner(System.in);
            while (true){
                int i = in.nextInt();
                log.info("write block:{} information into es", i);
            }

        }).start();
    }

    private static void checkComplete() {
        while (true) {
            if (blockQueue.size() == 0) {
                log.info("complete");
                break;
            }
        }
    }

    /**
     * 10????????? 1???client  5803ms
     * 10????????? 2???client  5640ms
     * 10????????? 4???client  5781ms ----- 20????????? 4???client  3165ms
     * 10????????? 5???client  5633ms ----- 20????????? 5???client  2978ms
     *
     *
     */
    private static void initConsumerQueue() {
        //consumeAsync(CLIENT4);

    }

    private static void consumeAsync(RestHighLevelClient client) {
        new Thread(() -> {
            while (true) {
                SaveBlock block = null;

                try {
                    block = blockQueue.take();
                } catch (InterruptedException e) {
                    log.error("take block error", e);
                }
                long s = System.currentTimeMillis();
                // ????????????
                IndexRequest request = new IndexRequest("block");
                // ?????? put /kuang_index/_doc/1
                request.timeout(TimeValue.timeValueSeconds(1));
                // ?????????????????????????????? json
                try {
                    request.id(block.getBlockNumber().toString());
                    request.source(SysUtils.OBJECTMAPPER.writeValueAsString(block), XContentType.JSON);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                // ????????????????????? , ?????????????????????
                IndexResponse indexResponse;
                try {
                    indexResponse = client.index(request, RequestOptions.DEFAULT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.info("block:{} write to es :{},time:{}ms", block.getBlockNumber(), indexResponse.status(), System.currentTimeMillis() - s);
            }
        }).start();
    }


    private static SaveBlock createSaveBlock(int i) {
        SaveBlock b = new SaveBlock();
        b.setTime(System.currentTimeMillis());
        b.setHash("hash" + i);
        b.setParentHash("parentHash" + i);
        b.setGasLimit("gasLimit" + i);
        b.setGasUsed("gasUsed" + i);
        b.setBlockNumber(BigInteger.valueOf(i));
        b.setNonce("nonce" + i);
        b.setMiner("miner" + i);
        b.setDifficulty("difficulty" + i);
        b.setTotalDifficulty("totalDifficulty" + i);
        b.setSize("size" + i);
        b.setBaseFeePerGas("baseFeePerGas" + i);
        return b;
    }
}
