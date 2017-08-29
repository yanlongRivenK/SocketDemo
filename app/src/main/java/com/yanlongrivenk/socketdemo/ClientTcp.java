package com.yanlongrivenk.socketdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClientTcp extends AppCompatActivity {

    @Bind(R.id.show_message)
    TextView mShowMessage;
    @Bind(R.id.et)
    EditText mEt;
    protected ExecutorService pools;
    Socket mSocket;
    private BufferedReader br;
    private OutputStream os;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_tcp);
        ButterKnife.bind(this);

        //线程池
        pools = Executors.newCachedThreadPool();
        //启动服务端
        startService(new Intent(this, ServiceTcp.class));
    }

    @OnClick({R.id.connect_service, R.id.disconnect, R.id.sendMessage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.connect_service:

                pools.execute(new Runnable() {
                    @Override
                    public void run() {

                        while (mSocket == null || !mSocket.isConnected()) {
                            try {
                                mSocket = new Socket("localhost", 2000);
                            } catch (final IOException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ClientTcp.this, "连接失败" + "\r\n" + "一秒后重连" + "\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                try {
                                    Thread.sleep(1000);     //延时重连
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        try {
                            br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                            os = mSocket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ClientTcp.this, "连接成功", Toast.LENGTH_SHORT).show();
                            }
                        });


                        //循环接受消息
                        acceptMessage(br);
                    }
                });

                break;
            case R.id.disconnect:
                try {
                    mSocket.close();
                    if (!mSocket.isConnected()) Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sendMessage:
                String str = mEt.getText().toString();
                if (os != null && !TextUtils.isEmpty(str)){
                    try {
                        os.write((str + "\r\n").getBytes());
                        os.flush();
                        Log.d("ClientTcp", "消息发送了么");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void acceptMessage(BufferedReader br) {
        while (mSocket.isConnected()){
            try {
                while (!br.ready()){}
                final String response = br.readLine();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] split = response.split("~~");
                        mShowMessage.setText(split[0] + "\n" + split[1]);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
