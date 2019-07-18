package com.android.jtwallet;

import com.android.jtwallet.keyStore.CipherException;
import com.android.jtwallet.keyStore.KeyStore;
import com.android.jtwallet.keyStore.KeyStoreFile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void keyStore() {
        JtKeyPair jtKeyPair = new JtKeyPair("jHY6aRcs7J6KnfgqD4FVwTQ247boj9nbDZ", "shExMjiMqza4DdMaSg3ra9vxWPZsQ");
        try {
            KeyStoreFile keyStoreFile = KeyStore.createLight("Key123456", jtKeyPair);
            System.out.println(keyStoreFile.toString());
            System.out.println("===============decrypt============");
            JtKeyPair decryptEthECKeyPair = KeyStore.decrypt("Key123456", keyStoreFile);
            System.out.println("address:" + decryptEthECKeyPair.getAddress());
            System.out.println("PrivateKey:" + decryptEthECKeyPair.getPrivateKey());

        } catch (CipherException e) {
            e.printStackTrace();
        }
    }
}