package com.android.jtwallet;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.android.jtwallet.client.Remote;
import com.android.jtwallet.client.bean.AccountInfo;
import com.android.jtwallet.client.bean.AccountTums;
import com.android.jtwallet.connection.Connection;
import com.android.jtwallet.connection.ConnectionFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountInstrumentedTest {
    private static final String TAG = "AccountInstrumentedTest";
    //	 static String server = "wss://s.jingtum.com:5020";// 生产环境
    static String server = "ws://ts5.jingtum.com:5020";// 测试环境
    static Boolean local_sign = true;// 是否使用本地签名方式提交交易
    static Connection conn = ConnectionFactory.getCollection(server);
    static Remote remote = new Remote(conn, local_sign);

    /**
     * 获取账户swtc
     *
     * @return
     */
    @Test
    public void getAccountSwtc() {
        String account = "j3UcBBbes7HFgmTLmGkEQQShM2jdHbdGAe";
        AccountInfo result1 = remote.requestAccountInfo(account, null, "trust");
        Log.v(TAG, "swtc：" + result1.getAccountData().getBalance());
        Assert.assertNotNull(result1.getAccountData().getBalance());

        AccountInfo result2 = remote.requestAccountInfo(account, null, "authorize");
        Log.v(TAG, "swtc：" + result1.getAccountData().getBalance());
        Assert.assertNotNull(result1.getAccountData().getBalance());

        AccountInfo result3 = remote.requestAccountInfo(account, null, "freeze");
        Log.v(TAG, "swtc：" + result1.getAccountData().getBalance());
        Assert.assertNotNull(result1.getAccountData().getBalance());
    }

    /**
     * 获取账户可接收和发送的货币
     *
     * @return
     */
    @Test
    public void getAccountTum() {
        String account = "jBciDE8Q3uJjf111VeiUNM775AMKHEbBLS";
        AccountTums result = remote.requestAccountTums(account, null);
        Log.v(TAG, "可接收Tum：" + result.getReceiveCurrencies().toString());
        Log.v(TAG, "可接收Tum：" + "可发送Tum：" + result.getSendCurrencies().toString());
        Assert.assertNotNull(result.getReceiveCurrencies().toString());
        Assert.assertNotNull(result.getSendCurrencies().toString());
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.android.jtwallet.test", appContext.getPackageName());
    }
}
