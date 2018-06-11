package com.jd.wly.intercom.input;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jd.wly.intercom.discover.AudioHandler;
import com.jd.wly.intercom.job.JobHandler;
import com.jd.wly.intercom.util.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class Sender extends JobHandler {


    // UI界面Handler
    private Handler handler;

    // 组播Socket
    private Socket socket;

    // IPV4地址
    private InetAddress inetAddress;
    private static int request_timeout = 10000;
    private String url_path;
    private InputStream inputStream = null;

    public Sender() {

    }

    public Sender(Handler handler) {
        this.handler = handler;
        initMulticastNetwork();
    }

    /**
     * 初始化组播网络
     */
    private void initMulticastNetwork() {
        try {
            inetAddress = InetAddress.getByName(Constants.REQUEST_DATA_IP);
            url_path = "http:/"+inetAddress+":"+Constants.REQUEST_DATA_PORT;


//            datagramSocket.setBroadcast(true);
//            multicastSocket.setLoopbackMode(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRequest(byte[] audioData) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(url_path);
            if (url != null) {

                //openConnection获得当前URL的连接
                httpURLConnection = (HttpURLConnection) url.openConnection();
                //设置3秒的响应超时
                httpURLConnection.setConnectTimeout(request_timeout);
                //设置允许输入
                httpURLConnection.setDoInput(true);
                //设置为GET方式请求数据
                httpURLConnection.setRequestMethod("GET");

                Log.d("发送请求消息","send data to server ");
                //获取连接响应码，200为成功，如果为其他，均表示有问题
                int responseCode=httpURLConnection.getResponseCode();
                int len = 0;
                byte[] data=new byte[2048];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if(responseCode==200)
                {
                    //getInputStream获取服务端返回的数据流。
                    inputStream=httpURLConnection.getInputStream();
                    while((len=inputStream.read(data))!=-1)
                    {
                        //向本地文件中写入图片流
                        baos.write(data,0,len);

                    }
                    inputStream.close();
                    String receive = baos.toString();
                    baos.close();
                    Log.d("http data :", receive);
                    sendMsg2MainThread(receive);
                }

            }


            //is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            datagramSocket.send(datagramPacket);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg2MainThread(String content) {
        Message message = new Message();
        message.what = AudioHandler.AUDIO_OUTPUT;
        message.obj = content; // message内容
        handler.sendMessage(message);
    }

    @Override
    public void free() {
        super.free();

    }
}
