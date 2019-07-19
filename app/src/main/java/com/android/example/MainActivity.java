package com.android.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.jtwallet.qrCode.QrCodeGenerator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static ImageView imageView;
    private static EditText editText;
    private static TextView textView;
    private static Button button;
    private static Button buttonIm;
    private static Button buttonCp;
    // 生产环境
    // static String server = "wss://s.jingtum.com:5020";
    // 测试环境
    private static String server = "ws://ts5.jingtum.com:5020";
    // 是否使用本地签名方式提交交易
    private static Boolean local_sign = true;
    private static Connection conn = ConnectionFactory.getCollection(server);
    private static Remote remote = new Remote(conn, local_sign);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.img_qr);
        textView = (TextView) findViewById(R.id.tv_data);
        editText = (EditText) findViewById(R.id.edt_data);
        button = (Button) findViewById(R.id.btn_qr);
        buttonIm = (Button) findViewById(R.id.btn_import);
        buttonCp = (Button) findViewById(R.id.btn_cp);
        buttonCp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clipboard("", textView.getText());
            }
        });
        buttonIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImportActivity.class);
                startActivity(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = editText.getText().toString();
                if (!TextUtils.isEmpty(data)) {
                    getQrCode(data);
                }
            }
        });

        Wallet wallet = Wallet.fromSecret("shExMjiMqza4DdMaSg3ra9vxWPZsQ");
        KeyStoreFile keyStoreFile = null;
        try {
            keyStoreFile = KeyStore.createLight("Key123456", wallet);
        } catch (CipherException e) {
            e.printStackTrace();
        }
        getQrCode(keyStoreFile.toString());
        transfer();

    }

    private void getQrCode(String data) {
        Bitmap bitmap = QrCodeGenerator.getQrCodeImage(data, 800, Color.BLACK);
        imageView.setImageBitmap(bitmap);
        textView.setText(QrCodeGenerator.decodeQrImage(bitmap));
    }

    private void clipboard(CharSequence label, CharSequence text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
    }

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
        Toast.makeText(this, bean.getEngineResultMessage(), Toast.LENGTH_SHORT);
    }
}
