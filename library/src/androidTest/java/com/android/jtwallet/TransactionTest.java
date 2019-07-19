package com.android.jtwallet;

import android.support.test.runner.AndroidJUnit4;

import com.android.jtwallet.client.Remote;
import com.android.jtwallet.client.Transaction;
import com.android.jtwallet.client.bean.Account;
import com.android.jtwallet.client.bean.AccountInfo;
import com.android.jtwallet.client.bean.AccountTx;
import com.android.jtwallet.client.bean.AmountInfo;
import com.android.jtwallet.client.bean.Memo;
import com.android.jtwallet.client.bean.TransactionInfo;
import com.android.jtwallet.client.bean.Transactions;
import com.android.jtwallet.connection.Connection;
import com.android.jtwallet.connection.ConnectionFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易相关测试
 *
 * @author yyl
 * @date 2019年5月17日
 */
@RunWith(AndroidJUnit4.class)
public class TransactionTest {

    // static String server = "wss://s.jingtum.com:5020";// 生产环境
    static String server = "ws://ts5.jingtum.com:5020";// 测试环境
    static Boolean local_sign = true;// 是否使用本地签名方式提交交易
    static Connection conn = ConnectionFactory.getCollection(server);
    static Remote remote = new Remote(conn, local_sign);

    /**
     * 数据上链
     *
     * @return
     */
    @Test
    public void sendTransaction() {
        String account = "j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe";
        String to = "jNn89aY84G23onFXupUd7bkMode6aKYMt8";
        String secret = "ssWiEpky7Bgj5GFrexxpKexYkeuUv";
        AmountInfo amount = new AmountInfo();
        amount.setCurrency("SWT");
        amount.setValue("0.01");
        Transaction tx = remote.buildPaymentTx(account, to, amount);
        tx.setSecret(secret);
        List<String> memos = new ArrayList<String>();
        memos.add("测试数据111");
        memos.add("测试数据222");
        memos.add("测试数据333");
        tx.addMemo(memos);
        TransactionInfo bean = tx.submit();
        Assert.assertNotNull(bean.getTxJson().getHash());
    }

    /**
     * 根据hash获取交易信息
     *
     * @return
     */
    @Test
    public void getTx() {
        String hash = "77D66074F56B76618DF30B04DCDB12E0A5E8D3B895404402E46C737E7EB194BD";
        Account bean = remote.requestTx(hash);
        System.out.println("from:" + bean.getAccount());
        System.out.println("to:" + bean.getDestination());
        if (bean.getAmount() != null) {
            System.out.println("currency:" + bean.getAmount().getCurrency());
            System.out.println("value:" + bean.getAmount().getValue());
            System.out.println("issuer:" + bean.getAmount().getIssuer());
        }
        if (bean.getMemos() != null) {
            for (Memo memo : bean.getMemos()) {
                System.out.println("memo:" + memo.getMemoData());
            }
        }
        Assert.assertNotNull(bean.getAccount());
    }

    /**
     * 获取账号SWTC余额信息
     *
     * @return
     */
    @Test
    public void getSwtcBleans() {
        String account = "j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe";
        AccountInfo bean = remote.requestAccountInfo(account, null, null);
        System.out.println("j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe---SWTC余额：" + bean.getAccountData().getBalance());
        Assert.assertNotNull(bean.getAccountData().getBalance());
    }

    /**
     * 获取交易记录列表
     *
     * @return
     */
    @Test
    public void getTxs() {
        String account = "j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe";
        AccountTx accountTx = remote.requestAccountTx(account, 5, null);// 前五条数据
        AccountTx accountTx2 = remote.requestAccountTx(account, 5, accountTx.getMarker());// 后五条数据
        for (Transactions a : accountTx.getTransactions()) {
            System.out.println("date:" + a.getDate() + "======hash:" + a.getHash());
            if (a.getMemos() != null) {
                for (Memo memo : a.getMemos()) {
                    System.out.println(memo.getMemoData());
                }
            }
            System.out.println(a.getEffects().toString());
        }
        System.out.println("------------------");
        for (Transactions a : accountTx2.getTransactions()) {
            System.out.println(a.getDate());
            if (a.getMemos() != null) {
                for (Memo memo : a.getMemos()) {
                    System.out.println(memo.getMemoData());
                }
            }
            System.out.println(a.getEffects().toString());
        }
        Assert.assertEquals(accountTx.getTransactions().size(), 5);
        Assert.assertEquals(accountTx2.getTransactions().size(), 5);
    }

}
