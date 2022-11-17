package io.jalivv.es.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigInteger;

/**
 * @description:
 * @author: Jalivv
 * @create: 2022-11-16 11:17
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveBlock {

    private Long time;
    private String hash;
    private String parentHash;
    private String gasLimit;
    private String gasUsed;
    private String miner;
    private String difficulty;
    private String totalDifficulty;
    private String size;
    private String nonce;
    private BigInteger blockNumber;
    private String baseFeePerGas;
}

