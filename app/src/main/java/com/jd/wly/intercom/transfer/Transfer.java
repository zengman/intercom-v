package com.jd.wly.intercom.transfer;

import android.os.Handler;

import com.jd.wly.intercom.input.Encoder;
import com.jd.wly.intercom.input.Recorder;
import com.jd.wly.intercom.input.Sender;
import com.jd.wly.intercom.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by yanghao1 on 2017/4/11.
 */
public class Transfer implements Runnable {


    private Socket socket;
    private byte[] content = new byte[]{};
    private String address;
    private DatagramSocket datagramSocket;

    public Transfer() {
        socket = new Socket();
        address = new String();
    }

    public void sendMessage(){

        try {
            this.address = this.address.replace("/","");
            InetAddress send_address = InetAddress.getByName(this.address);
            DatagramPacket datagramPacket = new DatagramPacket(
                    this.content, this.content.length, send_address, Constants.MULTI_BROADCAST_PORT);
            datagramSocket = new DatagramSocket();
            datagramSocket.send(datagramPacket);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void update(byte[] content, String address){
        this.content = content;
        this.address = address;
    }
    @Override
    public void run() {

        sendMessage();

        /*while (recording) {
            recorder.handleRequest(audioData);
        }*/
    }

    /**
     * 释放资源
     */
    public void free(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

