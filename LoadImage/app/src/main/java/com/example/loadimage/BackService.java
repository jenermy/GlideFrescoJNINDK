package com.example.loadimage;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author wanlijun
 * @description
 * @time 2018/3/8 17:54
 */

public class BackService extends Service {
    public static final String MESSAGE_ACTION = "com.lijun.wan.action.message";
    public static final String HEART_BEAT_ACTION = "com.lijun.wan.action.heartbeat";
    private LocalBroadcastManager mLocalBroadcastManager;
    private WeakReference<Socket> mSocket;
    private ReadThread readThread;
    private Handler mHandler = new Handler();
    private long sendTime = 0L;
    private IBackService.Stub iBackService = new IBackService.Stub() {
        @Override
        public boolean sendMessage(String message) throws RemoteException {
            return sendMsg(message);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBackService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new InitSocketThread().start();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
    private void initSocket(){
        try {
            Socket socket = new Socket("192.168.0.230",9800);
            mSocket = new WeakReference<Socket>(socket);
            readThread = new ReadThread(socket);
            readThread.start();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(System.currentTimeMillis() - sendTime >= 3000){
                        boolean isSuccess = sendMsg("");
                        if(!isSuccess){
                            mHandler.removeCallbacks(this);
                            readThread.release();
                            releaseLastSocket(mSocket);
                            new InitSocketThread().start();
                        }
                    }
                }
            },3000);
        }catch (Exception e){
            e.printStackTrace();
            Log.i("wanlijun",e.toString());
        }
    }

    private void releaseLastSocket(WeakReference<Socket> socketWeakReference){
        if(socketWeakReference != null){
            Socket socket = socketWeakReference.get();
            if(socket != null && !socket.isClosed()){
                socket.isClosed();
            }
            socket = null;
            socketWeakReference = null;
        }
    }

    private boolean sendMsg(String msg){
        if(mSocket == null || mSocket.get() == null){
            return false;
        }
        try {
            Socket socket = mSocket.get();
            if(!socket.isClosed() && socket.isOutputShutdown()){
                OutputStream outputStream = socket.getOutputStream();
                String message = msg + "\r\n";
                outputStream.write(message.getBytes());
                outputStream.flush();
                sendTime = System.currentTimeMillis();
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.i("wanlijun",e.toString());
            return false;
        }
        return true;
    }

    class InitSocketThread extends Thread{
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }
    class ReadThread extends Thread{
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;
        public ReadThread(Socket socket){
            this.mWeakSocket = new WeakReference<Socket>(socket);
        }
        public void release(){
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if(socket != null){
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024*4];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown()
                            && isStart && (length = inputStream.read(buffer))!=-1){
                        if(length > 0){
                            String message = new String(Arrays.copyOf(buffer,length)).trim();
                            if(message.equals("ok")){
                                Intent intent = new Intent(HEART_BEAT_ACTION);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            }else{
                                Intent intent = new Intent(MESSAGE_ACTION);
                                intent.putExtra("message",message);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("wanlijun",e.toString());
                }

            }
        }
    }
}
