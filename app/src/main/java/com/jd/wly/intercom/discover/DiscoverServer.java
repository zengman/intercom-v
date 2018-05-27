package com.jd.wly.intercom.discover;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jd.wly.intercom.util.Command;
import com.jd.wly.intercom.util.Constants;
import com.jd.wly.intercom.util.IPUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoverServer implements Runnable {

    private DatagramSocket datagramSocket;
    private AudioHandler handler;

    public DiscoverServer(AudioHandler handler) throws IOException {
        // Keep a socket open to listen to all the UDP trafic that is destined for this port
        datagramSocket = new DatagramSocket(Constants.BROADCAST_PORT, InetAddress.getByName("0.0.0.0"));
        datagramSocket.setBroadcast(true);
        // handler
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte buf[] = new byte[1024];
                String level = handler.handleGetLevel();
                String master = handler.handleGetMaster();
                // 接收数据
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);
                String content = new String(packet.getData()).trim();

                if(packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
                    continue;
                }


                Log.d("接受请求", "from ip = "+ packet.getAddress().toString()+" receive  == " +  content);
                if (content.contains(Command.DISC_REQUEST) ) {
                    String response = Command.DISC_RESPONSE + "," + level + "," + master;
                    byte[] feedback = response.getBytes();
                    // 发送数据
                    DatagramPacket sendPacket = new DatagramPacket(feedback, feedback.length,
                            packet.getAddress(), Constants.BROADCAST_PORT);
                    datagramSocket.send(sendPacket);
                    // 发送Handler消息
                    //String [] temp = content.split(",");
                    //sendMsg2MainThread(packet.getAddress().toString()+","+temp[1]+","+temp[2]);
                } else if (content.contains(Command.DISC_RESPONSE)) {
                    // 发送Handler消息
                    // 取出msg中的type， level, master;
                    String [] temp = content.split(",");
                    sendMsg2MainThread(packet.getAddress().toString()+","+temp[1]+","+temp[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送Handler消息
     *
     * @param content
     */
    private void sendMsg2MainThread(String content) {
        // address, level
        Message msg = new Message();
        msg.what = AudioHandler.DISCOVERING_RECEIVE;
        msg.obj = content;
        handler.sendMessage(msg);
    }
}
