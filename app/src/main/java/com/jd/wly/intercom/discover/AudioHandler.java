package com.jd.wly.intercom.discover;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jd.wly.intercom.AudioActivity;
import com.jd.wly.intercom.users.IntercomUserBean;
import com.jd.wly.intercom.util.IPUtil;

import java.lang.ref.WeakReference;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class AudioHandler extends Handler {

    // Peer Discovering
    public static final int DISCOVERING_SEND = 0;
    public static final int DISCOVERING_RECEIVE = 1;
    // Communication
    public static final int AUDIO_INPUT = 2;
    public static final int AUDIO_OUTPUT = 3;


    private WeakReference<AudioActivity> activityWeakReference;

    public AudioHandler(AudioActivity audioActivity) {
        activityWeakReference = new WeakReference<AudioActivity>(audioActivity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        AudioActivity activity = activityWeakReference.get();
        if (activity != null) {
            clearExpirePeers();
            String content = (String) msg.obj;

            long timeStamp = System.currentTimeMillis()/1000;
            //String timeStamp = String.valueOf(time);
            if (msg.what == DISCOVERING_SEND) {
//                activity.toast(content);
                String level = Integer.toString(msg.arg1);
                activity.toast("send level="+level+",master = "+activity.getMaster());
                //activity.foundNewUser(IPUtil.getLocalIPAddress(),level,activity.getMaster(),timeStamp);//discover pkt 获取自己的level，放入list
            } else if (msg.what == DISCOVERING_RECEIVE) {
                String [] temp = content.split(","); //
                if(activity.getLevel().equals("-1") && !temp[1].equals("-1")){
                    // 如果自己的level = -1 ,下一步：加入网络，寻找第一个level不是-1的人
                    // 确定该链路是可行的
                    Log.d("收到消息", "receive determin master :" + content);
                    if(isGetRoot(temp[0])){
                        int new_level = Integer.valueOf(temp[1]) + 1;
                        activity.setLevel(String.valueOf(new_level));
                        activity.setMaster(temp[0]);
                    }
                }
                activity.foundNewUser(temp[0],temp[1],temp[2],timeStamp);//从msg中获取对方的level

                activity.toast("receive level="+temp[1]+", master ="+ temp[2]);
            } else if (msg.what == AUDIO_OUTPUT) {
                activity.toast(content);
                Log.d("收到消息", "start transfer");
                activity.transfer("transfer mes "+content);
                Log.d("收到消息", "end transfer");

            }
        }
    }
    public String handleGetLevel(){
        AudioActivity activity = activityWeakReference.get();
        return activity.getLevel();
    }
    public String handleGetMaster(){
        AudioActivity activity = activityWeakReference.get();
        return activity.getMaster();
    }
    public void clearExpirePeers(){
        AudioActivity activity = activityWeakReference.get();
        activity.clearExpiredPeers();
    }
    public boolean isGetRoot(String ipAddress){
        if(ipAddress == null || ipAddress.length() == 0){
            return false;
        }
        AudioActivity activity = activityWeakReference.get();
        IntercomUserBean userBean = new IntercomUserBean();
        while (true){
            userBean = activity.findAddressPositionInlist(ipAddress);
            if(userBean == null) {
                return false;
            }
            if(userBean.getLevel().equals("0")){
                return true;
            }

           ipAddress = userBean.getMaster();

        }
    }

//    public void handleGetIntercomUserBeanLIst(){
//        AudioActivity activity = activityWeakReference.get();
//        activity.getUserBeanList();
//    }


}
