package com.jd.wly.intercom.output;

import android.os.Handler;
import android.os.Message;

import com.jd.wly.intercom.discover.AudioHandler;
import com.jd.wly.intercom.job.JobHandler;
import com.jd.wly.intercom.util.Constants;
import com.jd.wly.intercom.util.IPUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class Receiver extends JobHandler {

    // UI界面Handler
    private Handler handler;

    // 组播Socket
    private MulticastSocket multicastSocket;
    // IPV4地址
    private InetAddress group;

    public Receiver() {

    }

    public Receiver(Handler handler) {
        this.handler = handler;
        initMulticastNetwork();
    }

    private void initMulticastNetwork() {
        try {
            group = InetAddress.getByName(Constants.MULTI_BROADCAST_IP);
            multicastSocket = new MulticastSocket(Constants.MULTI_BROADCAST_PORT);
            multicastSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRequest(byte[] audioData) {
        audioData = new byte[2048];
        DatagramPacket datagramPacket = new DatagramPacket(audioData, audioData.length);
        try {
            multicastSocket.receive(datagramPacket);
            String str = new String(audioData);

            //sendMsg2MainThread(datagramPacket.getAddress().toString());
            if(!datagramPacket.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())){
                sendMsg2MainThread(str);


            };
           // getNextJobHandler().handleRequest(audioData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg2MainThread(String content) {
        Message message = new Message();
        message.what = AudioHandler.AUDIO_OUTPUT;
        message.obj = content;
        handler.sendMessage(message);
    }

    @Override
    public void free() {
        super.free();
        multicastSocket.close();
    }
}
