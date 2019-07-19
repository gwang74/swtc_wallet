package com.android.jtwallet;

import com.android.jtwallet.client.Remote;
import com.android.jtwallet.client.Transaction;
import com.android.jtwallet.client.Wallet;
import com.android.jtwallet.client.bean.AmountInfo;
import com.android.jtwallet.client.bean.TransactionInfo;
import com.android.jtwallet.connection.Connection;
import com.android.jtwallet.connection.ConnectionFactory;
import com.android.jtwallet.keyStore.CipherException;
import com.android.jtwallet.keyStore.KeyStore;
import com.android.jtwallet.keyStore.KeyStoreFile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    // 生产环境
    // static String server = "wss://s.jingtum.com:5020";
    // 测试环境
    private static String server = "ws://ts5.jingtum.com:5020";
    // 是否使用本地签名方式提交交易
    private static Boolean local_sign = true;
    private static Connection conn = ConnectionFactory.getCollection(server);
    private static Remote remote = new Remote(conn, local_sign);

    @Test
    public void keyStore() {
        Wallet jtKeyPair = new Wallet("shExMjiMqza4DdMaSg3ra9vxWPZsQ");
        try {
            KeyStoreFile keyStoreFile = KeyStore.createLight("Key123456", jtKeyPair);
            System.out.println("===============keyStore============");
            System.out.println(keyStoreFile.toString());
            Wallet decryptEthECKeyPair = KeyStore.decrypt("Key123456", keyStoreFile);
            System.out.println("address:" + decryptEthECKeyPair.getAddress());
            System.out.println("PrivateKey:" + decryptEthECKeyPair.getSecret());

        } catch (CipherException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void transfer() {
        AmountInfo amount = new AmountInfo();
        amount.setCurrency("SWT");
        amount.setValue("0.01");
        Transaction tx = remote.buildPaymentTx("j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe", "jNn89aY84G23onFXupUd7bkMode6aKYMt8", amount);
        tx.setSecret("ssWiEpky7Bgj5GFrexxpKexYkeuUv");
        List<String> memos = new ArrayList<String>();
        memos.add("测试转账");
        tx.addMemo(memos);
        TransactionInfo bean = tx.submit();
        System.out.println("===============transfer============");
        if ("0".equals(bean.getEngineResultCode())) {
            System.out.println("hash:" + bean.getTxJson().getHash());
        } else {
            System.out.println("msg:" + bean.getEngineResultMessage());
        }
    }
}