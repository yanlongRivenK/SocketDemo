package com.yanlongrivenk.socketdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceTcp extends Service {

    protected ExecutorService mThreadPools;
    private boolean isServideTcpDestory = false;

    private String[] mStrings = {"hello", "你好", "好个毛", "我不要面子的啊", "火车叨位去", "妈的,智障", "哈哈哈"};
    @Override
    public void onCreate() {
        super.onCreate();

        //线程池
        mThreadPools = Executors.newCachedThreadPool();
        mThreadPools.execute(new TcpAcceptRunnable());


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServideTcpDestory = true;
    }

    private class TcpAcceptRunnable implements Runnable{

        protected ServerSocket mServerSocket;

        @Override
        public void run() {
            try {
                mServerSocket = new ServerSocket(2000);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!isServideTcpDestory){
                try {
                    final Socket socket = mServerSocket.accept();   //这里是一个阻塞方法,如果没有客户端请求连接,线程会停在这里等待
                    mThreadPools.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                responseTcpClient(socket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }


    private void responseTcpClient(Socket client) throws IOException {
        //操作流
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        String acceptResponse = "";

        while (!isServideTcpDestory){

            while (!br.ready()){}

            acceptResponse = br.readLine();
            String responseStr = mStrings[new Random().nextInt(mStrings.length)];
            //因为readLine()方法在读到换行符之前会一直等待,这里用"~~"代替换行,在clientTcp中拿到数据再替换成换行符设置给textview
            bw.write("sendMessage: " + acceptResponse + "~~" + "receiveResponse: " + responseStr + "\r\n");
            bw.flush();

        }

        br.close();
        bw.close();
        client.close();

    }

}
