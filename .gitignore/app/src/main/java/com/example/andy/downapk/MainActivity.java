package com.example.andy.downapk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.andy.downapk.R.id.url;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView urlTv;
    private NumberProgressBar proBar;
    private  int mCurrProgress = -1;
    private boolean isDown;
    private int mCruuDownsize = 0 ;
    private  File downApk;
    private int fileSize;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            proBar.setProgress(mCurrProgress);
            if(mCurrProgress==100) {
                isDown = false;
                mCruuDownsize=0;
                fileSize=0;
                mCurrProgress=-1;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(downApk),
                        "application/vnd.android.package-archive");
                startActivity(intent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        urlTv= (TextView) findViewById(url);
        proBar= (NumberProgressBar) findViewById(R.id.probar);
        downApk = new File(getExternalCacheDir(), "jinritoutiao.apk");
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.start){
            startDown();
        }else if(v.getId()==R.id.pause){
            stopDown();
        }
    }

    private void stopDown() {
        isDown=false;
    }

    private void startDown() {
        if(isDown)
            return;

        Thread thread =new Thread(){
            @Override
            public void run() {
                try {
                    URL url =new URL(urlTv.getText().toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    int responseCode = 200;
                    if(fileSize!=0) {
                        conn.setRequestProperty("Range", "bytes=" + mCruuDownsize + "-" + fileSize);
                        responseCode=206;
                    }else{
                        fileSize=conn.getContentLength();
                    }
                    Log.e("run", "run: "+conn.getResponseCode());
                    if(conn.getResponseCode()==responseCode){

                        InputStream in = conn.getInputStream();
                        RandomAccessFile raf =new RandomAccessFile(downApk,"rw");

                        raf.seek(mCruuDownsize);

                        byte [] buff = new byte[1024];
                        int len =-1;
                        while(isDown && (len=in.read(buff))!=-1){
                            raf.write(buff,0,len);
                            mCruuDownsize+=len;

                            int pro = (int)(mCruuDownsize*100L/fileSize);
                            if(pro!=mCurrProgress){
                                mHandler.sendEmptyMessage(1);
                                mCurrProgress = pro;
                            }
                        }
                        raf.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        isDown= true;
    }
}
