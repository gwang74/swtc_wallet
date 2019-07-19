package com.android.jtwallet;

import android.support.test.runner.AndroidJUnit4;

import com.android.jtwallet.client.Wallet;
import com.android.jtwallet.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WalletTest {
    Wallet wallet = null;
    String VALID_ADDRESS = "jahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRV";
    String INVALID_ADDRESS1 = null;
    String INVALID_ADDRESS2 = null;
    String INVALID_ADDRESS3 = "";
    String INVALID_ADDRESS4 = "xxxx";
    String INVALID_ADDRESS5 = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    String INVALID_ADDRESS6 = "jahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRVxxx";
    String INVALID_ADDRESS7 = "ahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRV";
    String INVALID_ADDRESS8 = "jahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRVjahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRV";
    String VALID_SECRET = "sszWqvtbDzzMQEVWqGDSA5DbMYDBN";
    String INVALID_SECRET1 = null;
    String INVALID_SECRET2 = null;
    String INVALID_SECRET3 = "";
    String INVALID_SECRET4 = "xxxx";
    String INVALID_SECRET5 = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    String INVALID_SECRET6 = "sszWqvtbDzzMQEVWqGDSA5DbMYDBNxx";
    String INVALID_SECRET7 = "zWqvtbDzzMQEVWqGDSA5DbMYDBN";
    String INVALID_SECRET8 = "sszWqvtbDzzMQEVWqGDSA5DbMYDBNsszWqvtbDzzMQEVWqGDSA5DbMYDBN";
    String SIGNATURE1 = "3045022100B53E6A54B71E44A4D449C76DECAE44169204744D639C14D22D941157F5A1418F02201D029783B31EE3DA88F18C56D055CF47606A9708FDCA9A42BAD9EFD335FA29FD";
    String MESSAGE1 = "hello";
    String MESSAGE2 = null;
    String MESSAGE4 = "";
    String SIGNATURE5 = "3045022100E9532A94BF33D4E094C0E0DA131B8BFB28D8275F0004341A5D76218C3134B40802201C8A32706AD5A719B21297B590D9AC52726C08773A65F54FD027C61ED65BCC77";

    /**
     * 测试钱包生成程序
     */
    @Test
    public void generate() {
        Wallet wallet = Wallet.generate();
        // assert wallet == null : wallet.getAddress();
        System.out.println("---------generate----------");
        System.out.println("secret=" + wallet.getSecret());
        System.out.println("address=" + wallet.getAddress());
        System.out.println("publicKey=" + wallet.getPublicKey());
        System.out.println(JsonUtils.toJsonString(wallet));
        Assert.assertNotNull(wallet.getSecret());
    }

    /**
     * 测试根据密钥生成
     */
    @Test
    public void fromSecret() {
        wallet = Wallet.fromSecret("shxtc5jzR4aHNNJKTpY6FjSMRooeK");
        Assert.assertEquals("023C17909EC1D3944C1F780B6DBC2578EF84703B71A65DF13E22AB323D6B850E9C", wallet.getPublicKey());
        System.out.println("publicKey=" + wallet.getPublicKey());
        System.out.println("---------fromSecret----------");
        wallet = Wallet.fromSecret(VALID_SECRET);
        Assert.assertEquals(VALID_SECRET, wallet.getSecret());
        Assert.assertEquals("jahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRV", wallet.getAddress());
        System.out.println("secret=" + wallet.getSecret());
        System.out.println("address=" + wallet.getAddress());
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET1);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("INVALID_SECRET1  为 NULL时 ：" + e.getMessage());
        }
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET3);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("INVALID_SECRET1  为 空时 ：" + e.getMessage());
        }
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET4);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("INVALID_SECRET1  为 过短时 异常：" + e.getMessage());
        }
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET5);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("INVALID_SECRET5  为 过长时 异常：" + e.getMessage());
        }
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET6);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("should fail when tail string 异常：" + e.getMessage());
        }
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET7);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("should fail when not start with s 异常：" + e.getMessage());
        }
        try {
            wallet = Wallet.fromSecret(INVALID_SECRET8);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("should fail when double secret 异常：" + e.getMessage());
        }
    }

    /**
     * 校验密钥
     */
    @Test
    public void isValidSecret() {

        Assert.assertEquals(true, Wallet.isValidSecret(VALID_SECRET));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET1));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET2));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET3));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET4));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET5));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET6));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET7));
        Assert.assertEquals(false, Wallet.isValidSecret(INVALID_SECRET8));
    }

    /**
     * 校验公钥
     */
    @Test
    public void isValidAddress() {

        Assert.assertEquals(true, Wallet.isValidAddress(VALID_ADDRESS));
        Assert.assertEquals(false, Wallet.isValidAddress(INVALID_ADDRESS1));
        Assert.assertEquals(false, Wallet.isValidAddress(INVALID_ADDRESS2));
        Assert.assertEquals(false, Wallet.isValidAddress(INVALID_ADDRESS3));
        Assert.assertEquals(false, Wallet.isValidAddress(INVALID_ADDRESS4));
        Assert.assertEquals(false, Wallet.isValidAddress(INVALID_ADDRESS5));
        Assert.assertEquals(true, Wallet.isValidAddress(INVALID_ADDRESS6));
        Assert.assertEquals(false, Wallet.isValidAddress(INVALID_ADDRESS7));
        Assert.assertEquals(true, Wallet.isValidAddress(INVALID_ADDRESS8));
    }

    /**
     * 根据密码生成钱包
     */
    @Test
    public void structure() {
        wallet = new Wallet(VALID_SECRET);
        Assert.assertEquals(VALID_SECRET, wallet.getSecret());
        Assert.assertEquals("jahbmVT3T9yf5D4Ykw8x6nRUtUfAAMzBRV", wallet.getAddress());
        try {
            wallet = new Wallet(INVALID_SECRET1);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET2);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET3);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET4);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET5);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET6);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET7);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
        try {
            wallet = new Wallet(INVALID_SECRET8);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("创建钱包  异常：" + e.getMessage());
        }
    }

    /**
     * 钱包签名
     */
    @Test
    public void structureInit() {
        wallet = new Wallet(VALID_SECRET);
        String signStr = wallet.sign(MESSAGE1);
        Assert.assertEquals(SIGNATURE1, signStr);

        try {
            signStr = wallet.sign(MESSAGE2);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            System.out.println("钱包签名  异常：" + e.getMessage());
        }

        signStr = wallet.sign(MESSAGE4);
        Assert.assertEquals("304402201C503CF5A01817D320A3A62438F40D6EF8C00C1724EA795E5EDCB6659A85ACAE02200B55FBBE83FFE2E45F70414D141E372E1C66389FF80DA6028135B1AC1E93F9CC", signStr);

        String MESSAGE5 = "";
        for (int i = 0; i < 1000; ++i) {
            MESSAGE5 = MESSAGE5 + 'x';
        }
        signStr = wallet.sign(MESSAGE5);
        Assert.assertEquals(SIGNATURE5, signStr);
    }
}
