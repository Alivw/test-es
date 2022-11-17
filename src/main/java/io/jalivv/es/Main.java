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
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description:
 * @author: Jalivv
 * @create: 2022-11-16 11:08
 **/
@Slf4j
public class Main {

    private static final RestHighLevelClient CLIENT = EsConfig.newClientInstance(true);


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
        // 1、创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        // 2、客户端执行请求 IndicesClient,请求后获得响应
        CLIENT.indices().create(request, RequestOptions.DEFAULT);

    }

    public static void main(String[] args) {
        initEs();
        initConsumerQueue();
        long s = System.currentTimeMillis();
        for (int i = 0; i < CAPACITY; i++) {
            SaveBlock b = createSaveBlock(i);
            try {
                blockQueue.put(b);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        checkComplete();
        log.info("write {} block to es, time:{}ms", CAPACITY, System.currentTimeMillis() - s);
    }

    private static void checkComplete() {
        while (true) {
            if (blockQueue.size() == 0) {
                log.info("complete");
                break;
            }
        }
    }

    private static void initConsumerQueue() {
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);
        consumeAsync(CLIENT);

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
                // 创建请求
                IndexRequest request = new IndexRequest("block");
                // 规则 put /kuang_index/_doc/1
                request.timeout(TimeValue.timeValueSeconds(1));
                // 将我们的数据放入请求 json
                try {
                    request.id(block.getBlockNumber().toString());
                    request.source(SysUtils.OBJECTMAPPER.writeValueAsString(block), XContentType.JSON);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                // 客户端发送请求 , 获取响应的结果
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
