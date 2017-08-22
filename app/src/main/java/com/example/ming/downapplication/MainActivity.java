package com.example.ming.downapplication;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button updata;
    private Button stop;
    private ProgressBar updataProgessBar;
    private int fileLength = 0; //下载文件的总长度
    private final static String DOWNLOADPATH = Environment.getExternalStorageDirectory() + "/hongshi/";


    //更新UI界面
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //设置完成的进度
                    updataProgessBar.setProgress(msg.arg1);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updata = (Button) findViewById(R.id.updata);
        stop = (Button) findViewById(R.id.stop);
        updataProgessBar = (ProgressBar) findViewById(R.id.updataProgress);

        updata.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    /**
     * 处理点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updata:
                //通过线程来建立网络请求，开启下载任务
                downLoadThread.start();
                break;
            case R.id.stop:
                downLoadThread.interrupt();
                break;
        }
    }

    //下载任务线程
    private Thread downLoadThread = new Thread(new Runnable() {
        @Override
        public void run() {
            HttpURLConnection connection = null;
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                //创建hongshi文件夹
                File fileDir = new File(DOWNLOADPATH);
                if (!fileDir.exists()) {
                    fileDir.mkdir();
                }
                File downFile = new File(fileDir, "1.apk");
                URL url = new URL("http://ad.nvdvr.cn/t/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(180000);
                connection.setReadTimeout(120000);
                System.setProperty("http.keepAlive","false");
                long sum = 0;
                if (downFile.exists()) {
                    //如果文件已经存在的话，设置断点续传的位置
                    sum = downFile.length();
                    connection.setRequestProperty("Range", "bytes=" + sum + "-");
                    //通过handler来更新进度条
                    Message message = new Message();
                    message.what = 0;
                    message.arg1 = (int) sum;
                    uiHandler.sendMessage(message);
                }
                //开始连接
                connection.connect();
                int contentLength = connection.getContentLength();
                Log.e("jni", "获取到的长度为" + contentLength);
                contentLength += sum;   //总长度
                if (fileLength == 0) {
                    fileLength = contentLength;
                    //设置进度条的最大值
                    updataProgessBar.setMax(fileLength);
                }
                is = connection.getInputStream();
                fos = new FileOutputStream(downFile, true);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {

                    fos.write(buffer,0,length);
                    sum += length;
                    //通知UI线程更新进度条
                    Log.e("ming","下载的进度为："+sum);
                    Message message = new Message();
                    message.what = 0;
                    message.arg1 = (int) sum;
                    uiHandler.sendMessage(message);
                }
                Log.e("ming","下载完成！！！！！！！");
            } catch (Exception exception) {
                Log.e("ming","出错了！！！！！！！"+exception.getMessage());
                exception.printStackTrace();
            } finally {
                //关闭必要的流，避免造成内存泄漏
                connection.disconnect();
                try {
                    if(is != null){
                        is.close();
                    }
                   if(fos !=null){
                       fos.close();
                   }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}

