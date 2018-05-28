package com.jd.wly.intercom;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jd.wly.intercom.discover.AudioHandler;
import com.jd.wly.intercom.discover.DiscoverRequest;
import com.jd.wly.intercom.discover.DiscoverServer;
import com.jd.wly.intercom.input.AudioInput;
import com.jd.wly.intercom.output.AudioOutput;
import com.jd.wly.intercom.transfer.Transfer;
import com.jd.wly.intercom.users.IntercomAdapter;
import com.jd.wly.intercom.users.IntercomUserBean;
import com.jd.wly.intercom.users.VerticalSpaceItemDecoration;
import com.jd.wly.intercom.util.Command;
import com.jd.wly.intercom.util.Constants;
import com.jd.wly.intercom.util.IPUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class AudioActivity extends Activity {

    private RecyclerView localNetworkUser;
    private TextView currentIp;

    private List<IntercomUserBean> userBeanList = new ArrayList<>();

    private String level;
    private String master;
    public static final int EXPIRE_TIME = 20;
    private IntercomAdapter intercomAdapter;

    private AudioHandler audioHandler = new AudioHandler(this);

    private AudioInput audioInput;
    private Transfer transfer;
    private AudioOutput audioOutput;

    // 创建缓冲线程池用于录音和接收用户上线消息（录音线程可能长时间不用，应该让其超时回收）
    private ExecutorService inputService = Executors.newCachedThreadPool();

    // 创建循环任务线程用于间隔的发送上线消息，获取局域网内其他的用户
    private ScheduledExecutorService discoverService = Executors.newScheduledThreadPool(1);

    // 设置音频播放线程为守护线程
    private ExecutorService outputService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        }
    });

    // 探测局域网内其他用户的线程
    private DiscoverRequest discoverRequest;
    private DiscoverServer discoverServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        initView();
        initData();
        Button root_button = (Button) findViewById(R.id.become_root_button);
        root_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLevel("0");
            }
        });
    }

    private void initView() {
        // 设置用户列表
        localNetworkUser = (RecyclerView) findViewById(R.id.activity_audio_local_network_user_rv);
        localNetworkUser.setLayoutManager(new LinearLayoutManager(this));
        localNetworkUser.addItemDecoration(new VerticalSpaceItemDecoration(10));
        localNetworkUser.setItemAnimator(new DefaultItemAnimator());
        intercomAdapter = new IntercomAdapter(userBeanList);
        localNetworkUser.setAdapter(intercomAdapter);
        // 设置当前IP地址
        level = "0";
        master = "null";
        currentIp = (TextView) findViewById(R.id.activity_audio_current_ip);
        String ip = "当前IP地址为：" + IPUtil.getLocalIPAddress() + "("+level+")";
        currentIp.setText(ip);

    }

    /**
     * 初始化录音、放音线程，并启动
     */
    private void initData() {
        // 初始化探测线程
        try {
            discoverRequest = new DiscoverRequest(audioHandler);
            discoverServer = new DiscoverServer(audioHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 启动探测局域网内其余用户的线程（每分钟扫描一次）

        discoverService.scheduleAtFixedRate(discoverRequest, 0, 10, TimeUnit.SECONDS);
        // 启动探测线程接收
        inputService.execute(discoverServer);

        // 初始化录音线程
        audioInput = new AudioInput(audioHandler);
        transfer = new Transfer();

        // 初始化并启动放音线程
        audioOutput = new AudioOutput(audioHandler);
        //outputService.execute(audioOutput);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_F2 ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                ) {
            startRec();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_F1 ||
//                keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
//                && audioInput.isRecording()) {
//            stopRec();
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }


    public void transfer(String content){

        for(IntercomUserBean userBean: userBeanList){
            if(Integer.valueOf(userBean.getLevel()) == 1 ){
                Log.d("转发消息", "tranfer content : " + content + " to "+ userBean.getIpAddress());
                transfer.update(content.getBytes(), userBean.getIpAddress());
                inputService.execute(transfer);
            }

        }
    }
    /**
     * 开始Recorder
     */
    private void startRec() {
       // audioInput.setRecording(true);
        //audioInput.sendMessage();
       inputService.execute(audioInput);
    }

    /**
     * 关闭Recorder
     */
    private void stopRec() {
        audioInput.setRecording(false);
    }

    /**
     * 在UI线程中发送Toast提示
     *
     * @param msg
     */
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 发现新的用户地址
     *
     * @param ipAddress
     */
    public void foundNewUser(String ipAddress, String level, String master, long timestamp) {
        IntercomUserBean userBean;
//        if (ipAddress.contains(IPUtil.getLocalIPAddress())) {
//            userBean = new IntercomUserBean(ipAddress, "我", level, master, timestamp);//如果是自己记录下信息
//        } else {
//            userBean = new IntercomUserBean(ipAddress, level, timestamp);
//        }
        userBean = new IntercomUserBean(ipAddress, level, timestamp);

        if (findAddressPositionInlist(ipAddress)==null) {
            userBeanList.add(0, userBean);
            intercomAdapter.notifyItemInserted(0);
            localNetworkUser.scrollToPosition(0);
        }else {
            IntercomUserBean user = findAddressPositionInlist(ipAddress);
            if(ipAddress.equals(master) && Integer.valueOf(this.level)  !=  Integer.valueOf(level) + 1){
                // master 的leve发生了改变
                setLevel("-1");
                setMaster("null");
                Log.d("通知消息", "master level change");
            }
            user.updateUser(level,master,timestamp);
            intercomAdapter.notifyDataSetChanged();
        }

    }
    public void clearExpiredPeers(){
        long timeStamp = System.currentTimeMillis()/1000;
        int cnt = 0;

        Iterator<IntercomUserBean> iterator = userBeanList.iterator();
        while(iterator.hasNext()){
            IntercomUserBean user = iterator.next();
            if(timeStamp - user.getTimestamp() > EXPIRE_TIME ){
                if(user.getIpAddress().equals(master)){
                    setLevel("-1");
                    setMaster("null");
                    Log.d("通知消息", "master timeout");
                }
                Log.d("remove user", "ip address = "+user.getIpAddress());
                iterator.remove();
                Log.d("remove user", "a");
                intercomAdapter.notifyItemRemoved(cnt);
                intercomAdapter.notifyDataSetChanged();
                Log.d("remove user", "b");
                intercomAdapter.notifyDataSetChanged();
                Log.d("remove user", "adb");

                cnt --;
            }

            cnt ++;

        }
        Log.d("删除消息", "remove ok ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        free();
    }

    /**
     * 释放系统资源
     */
    private void free() {
        // 释放线程池
        inputService.shutdown();
        discoverService.shutdown();
        outputService.shutdown();
        // 释放线程资源
        audioInput.free();
        audioOutput.free();

    }


    public String getLevel() {
        return level;
    }
    public void setLevel(String level){
        this.level = level;
        currentIp = (TextView) findViewById(R.id.activity_audio_current_ip);
        String ip = "当前IP地址为：" + IPUtil.getLocalIPAddress() + "("+this.level+")";
        currentIp.setText(ip);

    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public IntercomUserBean findAddressPositionInlist(String ipAddress){
        for(int i=0; i< userBeanList.size(); i++){

            if(userBeanList.get(i).getIpAddress().equals(ipAddress)){
                return userBeanList.get(i);
            }
        }
        return null;
    }

    public List<IntercomUserBean> getUserBeanList() {
        return userBeanList;
    }
}
