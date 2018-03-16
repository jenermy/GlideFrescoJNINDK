package com.example.loadimage;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;


//glide和fresco的简单使用
public class MainActivity extends AppCompatActivity {
    private ImageView imageView1;
    private ImageView imageView2;
    private Button button1;
    private SimpleDraweeView imageView3;
    private Button button2;
    private SimpleDraweeView imageView4;
    private Button button3;
    private SimpleDraweeView imageView5;
    private Button button4;
    private SimpleDraweeView imageView6;
    private Button button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        imageView1 = (ImageView)findViewById(R.id.imageView1);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        imageView3 = (SimpleDraweeView)findViewById(R.id.imageView3);
        imageView4 = (SimpleDraweeView)findViewById(R.id.imageView4);
        imageView5 = (SimpleDraweeView)findViewById(R.id.imageView5);
        imageView6 = (SimpleDraweeView)findViewById(R.id.imageView6);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);
        button5 = (Button)findViewById(R.id.button5);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //加载静态图片
                Glide.with(MainActivity.this).load(Uri.parse("http://photocdn.sohu.com/20150912/mp31535219_1442022437896_17.jpeg"))
                        .placeholder(R.drawable.background)
                        .error(R.drawable.background)
                        //Glide会根据ImageView的大小来加载相应的图片像素到内存中，避免内存浪费，一般情况下不需要指定图片的大小
                        //如果一定要指定图片大小，就用override(100,100)，指定图片大小后，Glide只会加载指定的图片像素，而不管ImageView的大小了
//                        .override(100,100)
                        .into(imageView1);
                //加载GIF图片
                Glide.with(MainActivity.this)
                        .load(Uri.parse("http://p1.pstatp.com/large/166200019850062839d3"))
//                        .asGif()  //指定图片格式，asBitmap()指定为静态图片，asGif()指定为GIF图片，指定图片格式之后不需要Glide来自动判断图片的格式
                        .placeholder(R.drawable.background)
                        .error(R.drawable.background)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE) //少了这句GIF就加载不出来,而且只能选NONE或者SOURCE,其他的值GIF也加载不出来
                        .into(imageView2);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //加载静态图
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse("http://photocdn.sohu.com/20150912/mp31535219_1442022437896_17.jpeg"))
                        .setAutoPlayAnimations(true)
                        .build();
                imageView3.setController(controller);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //只能加载静态图,GIF图只会显示第一帧
                imageView4.setImageURI(Uri.parse("http://photocdn.sohu.com/20150912/mp31535219_1442022437896_17.jpeg"));
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //加载GIF图
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri("http://p1.pstatp.com/large/166200019850062839d3")
                        .setAutoPlayAnimations(true)
                        .build();
                imageView5.setController(controller);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ControllerListener controllerListener = new BaseControllerListener<ImageInfo>(){
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        Log.i("wanlijun","onFinalImageSet");
                        if(animatable != null){
                            animatable.start();
                        }
                    }
                };
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri("http://p1.pstatp.com/large/166200019850062839d3")
                        .setControllerListener(controllerListener)
                        .build();
                imageView6.setController(controller);
            }
        });
    }
}
