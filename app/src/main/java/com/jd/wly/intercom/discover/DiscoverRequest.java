package com.jd.wly.intercom.discover;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jd.wly.intercom.R;
import com.jd.wly.intercom.app.App;
import com.jd.wly.intercom.util.Command;
import com.jd.wly.intercom.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DiscoverRequest implements Runnable {

    private AudioHandler handler;

    private DatagramSocket datagramSocket;

    public DiscoverRequest(AudioHandler handler) {
        this.handler = handler;
        init();
    }

    private void init() {
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String level = handler.handleGetLevel();
        String master = handler.handleGetMaster();

        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        String requestInfo = Command.DISC_REQUEST + "," + level + "," + master; // send ip , level ,master
                        byte[] data = requestInfo.getBytes();

                        //byte[] data = new String(level).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(data,
                                data.length, broadcast, Constants.BROADCAST_PORT);
                        datagramSocket.send(sendPacket);
                        Log.d("发送请求", "pkt == " + requestInfo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   // Log.d("发送请求", getClass().getName() + ">>> Request packet sent to: " +
                   //         broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }
            //Log.d("发送请求", getClass().getName() +
            //        ">>> Done looping over all network interfaces. Now waiting for a reply!");
        } catch (IOException e) {
            e.printStackTrace();
            sendMsg2MainThread(App.getInstance().getResources().getString(R.string.send_failed), level);
        }
        sendMsg2MainThread(App.getInstance().getResources().getString(R.string.send_success), level);
    }

    /**
     * 发送消息到主线程
     *
     * @param msg
     */
    private void sendMsg2MainThread(String msg, String level) {
        Message message = new Message();
        message.what = AudioHandler.DISCOVERING_SEND;
        message.obj = msg;
        message.arg1 = Integer.valueOf(level).intValue();
        handler.sendMessage(message);
    }
}
