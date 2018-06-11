package com.jd.wly.intercom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jd.wly.intercom.calculate.CoordinateTransform;
import com.jd.wly.intercom.calculate.NewCoordinate;
import com.jd.wly.intercom.discover.AudioHandler;
import com.jd.wly.intercom.discover.DiscoverRequest;
import com.jd.wly.intercom.discover.DiscoverServer;
import com.jd.wly.intercom.input.AudioInput;
import com.jd.wly.intercom.output.AudioOutput;
import com.jd.wly.intercom.transfer.Transfer;
import com.jd.wly.intercom.users.GPSstatelliteInfo;
import com.jd.wly.intercom.users.IntercomAdapter;
import com.jd.wly.intercom.users.IntercomUserBean;
import com.jd.wly.intercom.users.Statellite;
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
import java.util.Calendar;
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
    private LocationManager locationManager=null;
    private LocationListener locationListener=null;
    private GeomagneticField gmfield;
    private TextView locationView=null;
    private TextView infoView=null,tvNmea,gpsgvView;
    // 卫星编号01-32
//    private List<GPSstatelliteInfo> gpSstatelliteInfoList;
    private GPSstatelliteInfo [] gpSstatelliteInfoList = new GPSstatelliteInfo[33];
    //空间直角坐标

    private double x,y,z;
    //大地坐标系
    private double b,l,h;
    private long time;
    // 4颗卫星的内容
    public Statellite [] gSet = null;
    // 基站观测时的时间
    public long statellite_time;

    private CoordinateTransform coordinateTransform = new CoordinateTransform();
    private NewCoordinate newCoordinate = new NewCoordinate();

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
//        Button root_button = (Button) findViewById(R.id.become_root_button);
//        root_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setLevel("0");
//            }
//        });
        infoView = (TextView)findViewById(R.id.tv_show);
        locationView = (TextView)findViewById(R.id.gps_status);
        //显示gpgga数据
        tvNmea = (TextView) findViewById(R.id.tv_nmea);
        gpsgvView = (TextView)findViewById(R.id.show_gpgsv_info);
        locationManager_init();

        // 获取GPS中的NMEA-0183协议中的GPGGA数据！！！！
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("没有信号","consider calling");
            return;
        }

        locationManager.addNmeaListener(new NmeaListener() {

            @Override
            public void onNmeaReceived(long timestamp, String nmea) {
                tvNmea.invalidate();
                //此处以GPGGA为例
                //$GPGGA,232427.000,3751.1956,N,11231.1494,E,1,6,1.20,824.4,M,-23.0,M,,*7E
                if (!nmea.isEmpty()) {
                    String info[] = nmea.split(",");
                    //GPGGA中altitude是MSL altitude(平均海平面)
//                    tvWGS84.setText(nmea);

                    if(nmea.contains("GPGSV") ){
                        int length = info.length;
                        int state_number = length/4;
//                        System.out.println("state_number =" + state_number);

                        for(int i=1; i < state_number; i++){
                            if(info[i*4+3].equals("") || info[i*4 + 1].equals("")|| info[i*4+2].equals("") ) {
                                return;
                            }
                            GPSstatelliteInfo gpS = new GPSstatelliteInfo();
                            gpS.setNumber(Integer.valueOf(info[i*4]));
                            if(gpS.getNumber() > 32) return;
                            gpS.setBeta(Double.valueOf(info[i*4 + 1]));
                            gpS.setAlpha(Double.valueOf(info[i*4 + 2]));
                            if(i != state_number-1){

                                gpS.setIsAvailable(Double.valueOf(info[i*4 + 3]));
                            }else{
                                if(info[i*4 + 3].startsWith("*")){
                                    return;
                                }
                                String [] temp = info[i*4 + 3].split("\\*");

                                gpS.setIsAvailable(Double.valueOf(temp[0]));
                            }
                            gpsgvView.setText(nmea);
                            Log.d("GP","获取的xxx数据是："+nmea);
                            Log.d("获取卫星编号", info[i*4]);


                            gpSstatelliteInfoList[gpS.getNumber()] = gpS;

                        }



                    }

                }else{
                    tvNmea.setText("nononono");
                }

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
        gSet = new Statellite[Constants.STATELLITE_NUMBER];
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
//        gpSstatelliteInfoList = new ArrayList<>();

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
    public double idiv(double a, double b){
        return Math.floor(a/b);
    }
    public double getMdj(double year, double month, double day){
        double mdj = 367 * year
                - idiv(7 * (idiv(month + 9, 12) + year), 4)
                - idiv(3 * (idiv(idiv(month + 9, 12) + year - 1, 100) + 1), 4)
                + idiv(275 * month, 9)
                + day + 1721028 - 2400000;
        return mdj;
    }
    public int localTime2GPStime(){
        Calendar rightNow = Calendar.getInstance();
        /*用Calendar的get(int field)方法返回给定日历字段的值。
        HOUR 用于 12 小时制时钟 (0 - 11)，HOUR_OF_DAY 用于 24 小时制时钟。*/
        Integer year = rightNow.get(Calendar.YEAR);
        if(year < 1900) year = year + 1900;
        Integer month = rightNow.get(Calendar.MONTH)+1; //第一个月从0开始，所以得到月份＋1
        Integer day = rightNow.get(rightNow.DAY_OF_MONTH);

        Integer hours = rightNow.get(rightNow.HOUR_OF_DAY);
        hours -= 8;
        if(hours < 0) {
            hours += 24;
            day -= 1;
        }

        Integer minute = rightNow.get(rightNow.MINUTE);
        Integer second = rightNow.get(rightNow.SECOND);
        second += 18;
        if (second >= 60){
            second = second - 60;
            minute = minute + 1;
            if (minute>= 60){
                minute = minute - 60;
                hours = hours + 1;
                if (hours >= 24){
                    hours = hours - 24;
                    day = day + 1;
                }

            }

        }

        double elapsed = ((( hours * 60 ) + minute ) * 60 ) + second;
        double GpsDayCount = getMdj(year, month, day) - getMdj(1980, 1, 6) ;
        double GpsWeekCount = (int)(idiv(GpsDayCount, 7));
        double GpsDay = GpsDayCount % 7;
        int GpsSecond = (int)(( GpsDay * 86400 ) + elapsed);
        return GpsSecond;


    }
    /*locationManager初始化*/
    public void locationManager_init(){
        locationManager =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener_init();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0, locationListener);
    }
    /*locatonListener初始化*/
    public void locationListener_init(){
        locationListener=new LocationListener(){
            //位置变化时触发
            public void onLocationChanged(Location location) {
                //得到的数据---记录
                // location.getTime() Return the UTC time of this fix, in milliseconds since January 1, 1970.
//                time = location.getTime();
                time = localTime2GPStime();
                time = 374353;
                l = location.getLongitude();
                b = location.getLatitude();
                h = location.getAltitude();

//                b = 39.962935;
//                l = 116.354986;
//                h = 68;
                coordinateTransform.blh2xyz(b,l,h);
              //  coordinateTransform.blh2xyz(23.4401,107.4248,39);
                x = coordinateTransform.getX();
                y = coordinateTransform.getY();
                z = coordinateTransform.getZ();
                System.out.println(x+" "+y+" "+z);
//                z=4069717.0123;
                locationView.setText("时间："+time +"\n");
                locationView.append("经度："+l+"\n");
                locationView.append("纬度："+b+"\n");
                locationView.append("海拔："+h+"\n");
//                coordinateTransform.blh2xyz(b,l,h);
//                locationView.append("修正后经度："+y+"\n");
//                locationView.append("修正后纬度："+x+"\n");
//                locationView.append("修正后海拔："+z+"\n");
//                coordinateTransform.xyz2blh(x,y,z);
//                locationView.append("huu修正后经度："+coordinateTransform.getL()+"\n");
//                locationView.append("修正后纬度："+coordinateTransform.getB()+"\n");
//                locationView.append("修正后海拔："+coordinateTransform.getH()+"\n");

                if(gSet[0] != null ){
                    newCoordinate.calculate(gSet,gpSstatelliteInfoList,time, statellite_time, b,l,h, x,y,z);
                    tvNmea.setText("delta x=" + newCoordinate.getX()+"\n"+"y="+newCoordinate.getY()+"\nz="+newCoordinate.getZ());
                    x += newCoordinate.getX();
                    y += newCoordinate.getY();
                    z += newCoordinate.getZ();
                    coordinateTransform.xyz2blh(x,y,z);
//                    locationView.append("修正后经度："+y+"\n");
//                    locationView.append("修正后纬度："+x+"\n");
//                    locationView.append("修正后海拔："+z+"\n");
                    locationView.append("huu修正后经度："+coordinateTransform.getL()+"\n");
                    locationView.append("修正后纬度："+coordinateTransform.getB()+"\n");
                    locationView.append("修正后海拔："+coordinateTransform.getH()+"\n");

//                    gpSstatelliteInfoList.clear();
                }




            }
            //gps禁用时触发
            public void onProviderDisabled(String provider) {
                infoView.setText("当前GPS状态：禁用\n");
            }
            //gps开启时触发
            public void onProviderEnabled(String provider) {
                infoView.setText("当前GPS状态：开启\n");
            }
            //gps状态变化时触发
            public void onStatusChanged(String provider, int status,Bundle extras) {
                if(status== LocationProvider.AVAILABLE){
                    infoView.setText("当前GPS状态：可见的\n");
                }else if(status==LocationProvider.OUT_OF_SERVICE){
                    infoView.setText("当前GPS状态：服务区外\n");
                }else if(status==LocationProvider.TEMPORARILY_UNAVAILABLE){
                    infoView.setText("当前GPS状态：暂停服务\n");
                }
            }
        };
    }


}
