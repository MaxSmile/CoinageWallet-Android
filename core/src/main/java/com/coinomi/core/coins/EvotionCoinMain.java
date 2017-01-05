package com.coinomi.core.coins;

import com.coinomi.core.coins.families.PeerFamily;

/**
 * @author Vitali Grabovski on 23.08.2016.
 */
public class EvotionCoinMain extends PeerFamily{
    private EvotionCoinMain() {
        id = "evotion.main";

        addressHeader = 22;
        p2shHeader = 51;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 100;
        //dumpedPrivateKeyHeader = 153;

        name = "Evotion (beta)";
        symbol = "EVO";
        uriScheme = "evotion";
        bip44Index = 98;
        unitExponent = 8;
        feeValue = value(100000); // 0.00001 EVO
        minNonDust = value(1);
        softDustLimit = value(1000000); // 0.01 BLK
        softDustPolicy = SoftDustPolicy.AT_LEAST_BASE_FEE_IF_SOFT_DUST_TXO_PRESENT;
        signedMessageHeader = toBytes("Evotion Signed Message:\n");

    }

    private static EvotionCoinMain instance = new EvotionCoinMain();
    public static synchronized CoinType get() {
        return instance;
    }
}
