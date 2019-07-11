package com.android.jtwallet;

import com.android.jtwallet.keyStore.CipherException;
import com.android.jtwallet.keyStore.JtKeyPair;
import com.android.jtwallet.keyStore.KeyStore;
import com.android.jtwallet.keyStore.KeyStoreFile;

import org.junit.Test;

import java.nio.charset.Charset;

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
        JtKeyPair jtKeyPair = new JtKeyPair("jBvrdYc6G437hipoCiEpTwrWSRBS2ahXN6", "snBPyRRpE56ea4QGCpTMVTQWoirT2");
        try {
            KeyStoreFile keyStoreFile = KeyStore.createLight("Key123456", jtKeyPair);
            System.out.println("address:" + jtKeyPair.getAddress());
            System.out.println("PrivateKey:" + jtKeyPair.getPrivateKey());
            System.out.println("cipher:" + keyStoreFile.getCrypto().getCipher());
            System.out.println("ciphertext:" + keyStoreFile.getCrypto().getCiphertext());
            System.out.println("cipherparams.iv:" + keyStoreFile.getCrypto().getCipherparams().getIv());
            System.out.println("kdf:" + keyStoreFile.getCrypto().getKdf());
            KeyStoreFile.ScryptKdfParams scryptKdfParams = (KeyStoreFile.ScryptKdfParams) (keyStoreFile.getCrypto().getKdfparams());
            System.out.println("kdfparams.dklen:" + scryptKdfParams.getDklen());
            System.out.println("kdfparams.n:" + scryptKdfParams.getN());
            System.out.println("kdfparams.p:" + scryptKdfParams.getP());
            System.out.println("kdfparams.r:" + scryptKdfParams.getR());
            System.out.println("kdfparams.salt:" + scryptKdfParams.getSalt());
            System.out.println("mac:" + keyStoreFile.getCrypto().getMac());
            System.out.println("id:" + keyStoreFile.getId());
            System.out.println("v:" + keyStoreFile.getVersion());
            JtKeyPair decryptEthECKeyPair = KeyStore.decrypt("Key123456", keyStoreFile);
            System.out.println("address:" + decryptEthECKeyPair.getAddress());
            System.out.println("PrivateKey:" + decryptEthECKeyPair.getPrivateKey());

        } catch (CipherException e) {
            e.printStackTrace();
        }
    }
}