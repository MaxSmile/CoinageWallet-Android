package com.coinomi.core.coins;

import com.coinomi.core.coins.families.PeerFamily;

/**
 * Created by vitaligrabovski on 12/31/16.
 */

public class RealPointCoinMain extends PeerFamily {
    private RealPointCoinMain() {
        id = "realpointcoin.main";

        addressHeader = 61;
        p2shHeader = 5;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 100;
        //dumpedPrivateKeyHeader = 153;

        name = "RealPointCoin (beta)";
        symbol = "RPТ";
        uriScheme = "realpointcoin";
        bip44Index = 139;
        unitExponent = 8;
        feeValue = value(100000); // 0.00001 RPT
        minNonDust = value(1);
        softDustLimit = value(1000000); // 0.01 RPT
        softDustPolicy = SoftDustPolicy.AT_LEAST_BASE_FEE_IF_SOFT_DUST_TXO_PRESENT;
        signedMessageHeader = toBytes("RealPointCoin Signed Message:\n");

    }

    private static RealPointCoinMain instance = new RealPointCoinMain();
    public static synchronized CoinType get() {
        return instance;
    }
}
