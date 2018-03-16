package com.example.loadimage;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.megvii.livenesslib.LivenessActivity;

import java.lang.ref.WeakReference;

public class Main2Activity extends AppCompatActivity {
    public static final String MESSAGE_ACTION = "com.lijun.wan.action.message";
    public static final String HEART_BEAT_ACTION = "com.lijun.wan.action.heartbeat";
    private LocalBroadcastManager localBroadcastManager;
    private TextView resultTv,contentTv;
    private Button sendBtn;
    private Button soBtn;
    private MessageBroadCastReceiver messageBroadCastReceiver;
    private IntentFilter filter;
    private Intent serviceIntent;
    private IBackService iBackService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
           iBackService = IBackService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iBackService = null;
        }
    };
    static {
        System.loadLibrary("HelloJni");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        resultTv = (TextView)findViewById(R.id.resultTv);
        contentTv = (TextView)findViewById(R.id.contentTv);
        sendBtn = (Button)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Main2Activity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //进行权限请求
                    ActivityCompat.requestPermissions(Main2Activity.this,
                            new String[]{Manifest.permission.CAMERA},
                            101);
                } else {
                    Intent intent = new Intent(Main2Activity.this,LivenessActivity.class);
                    startActivity(intent);
                }
//                try {
//                    boolean isSuccess = iBackService.sendMessage("人生若如初相见");
//                    Toast.makeText(Main2Activity.this, isSuccess ? "success" : "fail",
//                            Toast.LENGTH_SHORT).show();
//                }catch (Exception e){
//                    e.printStackTrace();
//                    Log.i("wanlijun",e.toString());
//                }
            }
        });
        messageBroadCastReceiver = new MessageBroadCastReceiver(contentTv);
        serviceIntent = new Intent(this,BackService.class);
        filter = new IntentFilter();
        filter.addAction(BackService.MESSAGE_ACTION);
        filter.addAction(BackService.HEART_BEAT_ACTION);
        soBtn = (Button)findViewById(R.id.soBtn);
        soBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelloJni helloJni = new HelloJni();
                resultTv.setText(helloJni.printStr());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(messageBroadCastReceiver,filter);
        bindService(serviceIntent,connection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        localBroadcastManager.unregisterReceiver(messageBroadCastReceiver);
    }

    class  MessageBroadCastReceiver extends BroadcastReceiver{
        private WeakReference<TextView> textViewWeakReference;
        public MessageBroadCastReceiver(TextView textView){
            textViewWeakReference = new WeakReference<TextView>(textView);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView textView = textViewWeakReference.get();
            if(action.equals(MESSAGE_ACTION)){
                if(textView != null) {
                    textView.setText(intent.getStringExtra("message"));
                }
            }else if(action.equals(HEART_BEAT_ACTION)){
                if(textView != null){
                    textView.setText("heart beat");
                }
            }
        }
    }
}
