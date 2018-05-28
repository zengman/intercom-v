package com.jd.wly.intercom.output;

import android.location.LocationProvider;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jd.wly.intercom.discover.AudioHandler;
import com.jd.wly.intercom.job.JobHandler;
import com.jd.wly.intercom.util.Constants;
import com.jd.wly.intercom.util.IPUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class Receiver extends JobHandler {

    // UI界面Handler
    private Handler handler;

    // 组播Socket
    private Socket socket;
    // IPV4地址
    private InetAddress group;
    private static int socket_timeout = 10000;

    public Receiver() {

    }

    public Receiver(Handler handler) {
        this.handler = handler;
        initMulticastNetwork();
    }

    private void initMulticastNetwork() {
        try {
            group = InetAddress.getByName(Constants.REQUEST_DATA_IP);
            socket = new Socket(group, Constants.REQUEST_DATA_PORT);
            socket.setSoTimeout(socket_timeout);
        }catch (Exception e){
                e.printStackTrace();
        }

    }

    @Override
    public void handleRequest(byte[] audioData) {
        audioData = new byte[2048];
        String str = null;
        String result = new String();
        BufferedReader is=null;

        try {
            socket = new Socket(group, Constants.REQUEST_DATA_PORT);
            socket.setSoTimeout(socket_timeout);
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int temp = 0;
            while((temp = is.read() )!= -1){
                //System.out.println(str);//此时str就保存了一行字符串
                str += result;
            }

            is.close();
            Log.d("收到服务端数据","receive :"+str);
            //sendMsg2MainThread(datagramPacket.getAddress().toString());
            sendMsg2MainThread(str);

           // getNextJobHandler().handleRequest(audioData);
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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
