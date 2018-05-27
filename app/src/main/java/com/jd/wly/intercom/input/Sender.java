package com.jd.wly.intercom.input;

import android.os.Handler;

import com.jd.wly.intercom.job.JobHandler;
import com.jd.wly.intercom.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class Sender extends JobHandler {


    // UI界面Handler
    private Handler handler;

    // 组播Socket
    private MulticastSocket multicastSocket;

    // IPV4地址
    private InetAddress inetAddress;

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
            inetAddress = InetAddress.getByName(Constants.MULTI_BROADCAST_IP);
            multicastSocket = new MulticastSocket();
            multicastSocket.setBroadcast(true);
//            multicastSocket.setLoopbackMode(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRequest(byte[] audioData) {
        DatagramPacket datagramPacket = new DatagramPacket(
                audioData, audioData.length, inetAddress, Constants.MULTI_BROADCAST_PORT);
        try {
            multicastSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void free() {
        super.free();
        multicastSocket.close();
    }
}
