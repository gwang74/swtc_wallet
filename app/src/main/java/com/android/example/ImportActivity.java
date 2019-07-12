package com.android.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.jtwallet.keyStore.CipherException;
import com.android.jtwallet.keyStore.JtKeyPair;
import com.android.jtwallet.keyStore.KeyStore;
import com.android.jtwallet.keyStore.KeyStoreFile;

import java.io.IOException;

public class ImportActivity extends AppCompatActivity {
    private static EditText editText;
    private static EditText editTextPwd;
    private static Button button;
    private static TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        editText = (EditText) findViewById(R.id.edt_data);
        editTextPwd = (EditText) findViewById(R.id.edt_pwd);
        button = (Button) findViewById(R.id.btn_commit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = editText.getText().toString();
                String pwd = editTextPwd.getText().toString();
                if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(pwd)) {
                    try {
                        KeyStoreFile keyStoreFile = KeyStoreFile.parse(data);
                        JtKeyPair jtKeyPair = KeyStore.decrypt(pwd, keyStoreFile);
                        textView.setText("Address:" + jtKeyPair.getAddress() + "\nPrivateKey:" + jtKeyPair.getPrivateKey());
                    } catch (IOException e) {
                        e.printStackTrace();
                        textView.setText(e.getMessage());
                    } catch (CipherException e) {
                        e.printStackTrace();
                        textView.setText(e.getMessage());
                    }
                }
            }
        });
        textView = (TextView) findViewById(R.id.tv_wallet);
    }
}
