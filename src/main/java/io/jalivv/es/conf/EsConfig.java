package io.jalivv.es.conf;

import io.jalivv.es.utils.SysUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Objects;

/**
 * @description: es configuration
 * @author: Jalivv
 * @create: 2022-11-16 11:08
 **/
public class EsConfig {
    private static final String ES_HOST = SysUtils.getSystemEnv("ES_HOST", "192.168.31.55");
    private static final String ES_PORT = SysUtils.getSystemEnv("ES_PORT", "9202");
    private static final String ES_SCHEME = SysUtils.getSystemEnv("ES_SCHEME", "http");


    public static RestHighLevelClient newClientInstance(boolean singleton) {
        if (singleton)
            return RestHighLevelClientHandler.instance;
        else
            return new RestHighLevelClient(RestClient.builder(new HttpHost(ES_HOST, Integer.parseInt(ES_PORT), ES_SCHEME)));
    }

    public static class RestHighLevelClientHandler {
        private static RestHighLevelClient instance = new RestHighLevelClient(RestClient.builder(new HttpHost(ES_HOST, Integer.parseInt(ES_PORT), ES_SCHEME)));
    }


}
