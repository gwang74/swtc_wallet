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

import com.android.jtwallet.client.Wallet;
import com.android.jtwallet.keyStore.CipherException;
import com.android.jtwallet.keyStore.KeyStore;
import com.android.jtwallet.keyStore.KeyStoreFile;
import com.android.jtwallet.qrCode.QrCodeGenerator;

public class MainActivity extends AppCompatActivity {

    private static ImageView imageView;
    private static EditText editText;
    private static TextView textView;
    private static Button button;
    private static Button buttonIm;
    private static Button buttonCp;

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
}
