package com.android.example;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jtwallet.keyStore.CipherException;
import com.android.jtwallet.keyStore.JtKeyPair;
import com.android.jtwallet.keyStore.KeyStore;
import com.android.jtwallet.keyStore.KeyStoreFile;
import com.android.jtwallet.qrCode.QrCodeGenerator;

public class MainActivity extends AppCompatActivity {

    private static ImageView imageView;
    private static TextView textView;
    private static EditText editText;
    private static Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.img_qr);
        textView = (TextView) findViewById(R.id.tv_data);
        editText = (EditText) findViewById(R.id.edt_data);
        button = (Button) findViewById(R.id.btn_qr);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = editText.getText().toString();
                if (!TextUtils.isEmpty(data)) {
                    getQrCoe(data);
                }
            }
        });

        JtKeyPair jtKeyPair = new JtKeyPair("jHY6aRcs7J6KnfgqD4FVwTQ247boj9nbDZ", "shExMjiMqza4DdMaSg3ra9vxWPZsQ");
        KeyStoreFile keyStoreFile = null;
        try {
            keyStoreFile = KeyStore.createLight("Key123456", jtKeyPair);
        } catch (CipherException e) {
            e.printStackTrace();
        }
        getQrCoe(keyStoreFile.toString());

    }

    private void getQrCoe(String data) {
        Bitmap bitmap = QrCodeGenerator.getQrCodeImage(data, 800, Color.BLACK);
        imageView.setImageBitmap(bitmap);
        textView.setText(QrCodeGenerator.decodeQrImage(bitmap));
    }
}
