package com.android.jtwallet;

import android.support.test.runner.AndroidJUnit4;

import com.android.jtwallet.client.Remote;
import com.android.jtwallet.client.Transaction;
import com.android.jtwallet.client.bean.AmountInfo;
import com.android.jtwallet.client.bean.TransactionInfo;
import com.android.jtwallet.connection.Connection;
import com.android.jtwallet.connection.ConnectionFactory;
import com.android.jtwallet.utils.JsonUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MsgConver {
    // static String server = "wss://s.jingtum.com:5020";// 生产环境
    static String server = "ws://ts5.jingtum.com:5020";// 测试环境
    static Boolean local_sign = true;// 是否使用本地签名方式提交交易
    static Connection conn = ConnectionFactory.getCollection(server);
    static Remote remote = new Remote(conn, local_sign);

    /**
     * 14 支付
     */
    @Test
    public void buildPaymentTx() {
        String from = "j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe";
        String to = "jNn89aY84G23onFXupUd7bkMode6aKYMt8";
        String memo = "{\r\n"
                + "		 \"vc_hetmc\" : \"测试合同名称\",\"vc_chengzkhmc\" : \"承租客户名称\",\"vc_biz\" : \"人民币\",\"dec_hetje\" : \"2000000\",\"dec_shengyje\" : \"100000\",\"dt_qizrq\" :\r\n"
                + "		 \"2018-11-16\",\"vc_hetzt\" : \"已启动\", \"type\" : \"type1\", \"dt_tijsj\" : \"2018-11-30 08:56:01\"\r\n"
                + "		 }";
        List<String> memos = new ArrayList<>();
        memos.add(memo);
        String fromSecret = "ssWiEpky7Bgj5GFrexxpKexYkeuUv";
        // #区块链服务器地址
        // #货币种类，三到六个字母或20字节的自定义货币
        // blockChain.currency=SWT
        // #货币数量
        // blockChain.basePrice=0.000001
        // #货币发行方
        // blockChain.issuer=
        // 使用签名
        AmountInfo amount = new AmountInfo();
        amount = new AmountInfo();
        amount.setCurrency("SWT");
        amount.setValue("0.000001");
        amount.setIssuer("");
        // String memo = "支付0.000001SWT";
        Transaction tx = remote.buildPaymentTx(from, to, amount);
        tx.addMemo(memos);
        tx.setSecret(fromSecret);
        tx.setFee(20);
        TransactionInfo bean = tx.submit();
        System.out.println("14 buildPaymentTx:\n" + JsonUtils.toJsonString(bean));
        Assert.assertNotNull(JsonUtils.toJsonString(bean));
    }
}