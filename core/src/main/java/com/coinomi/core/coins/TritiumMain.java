package com.coinomi.core.coins;

import com.coinomi.core.coins.families.PeerFamily;

/**
 * Created by vitaligrabovski on 3/17/17.
 */

public class TritiumMain extends PeerFamily {
    private TritiumMain() {
        id = "tritium.main";

        addressHeader = 65;
        p2shHeader = 10;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 600;
        //dumpedPrivateKeyHeader = 153;

        name = "Tritium (beta)";
        symbol = "TRT";
        uriScheme = "tritium";
        bip44Index = 1000;
        unitExponent = 8;
        feeValue = value(100000); // 0.00001 TRT
        minNonDust = value(1);
        softDustLimit = value(1000000); // 0.01 TRT
        softDustPolicy = SoftDustPolicy.AT_LEAST_BASE_FEE_IF_SOFT_DUST_TXO_PRESENT;
        signedMessageHeader = toBytes("Tritium Signed Message:\n");

    }

    private static TritiumMain instance = new TritiumMain();
    public static synchronized CoinType get() {
        return instance;
    }
}
