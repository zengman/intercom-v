package com.jd.wly.intercom.users;

import java.sql.Timestamp;

/**
 * Created by yanghao1 on 2017/4/13.
 */

public class IntercomUserBean {

    private String ipAddress;
    private String aliasName;
    private String level;
    private long timestamp;
    private String master ;

    public IntercomUserBean() {
    }

    public IntercomUserBean(String ipAddress) {
        this.ipAddress = ipAddress;
    }

//    public IntercomUserBean(String ipAddress, String aliasName) {
//        this.ipAddress = ipAddress;
//        this.aliasName = aliasName;
//    }
    public IntercomUserBean(String ipAddress, String aliasName, String level, String master, long timestamp){
        this.ipAddress = ipAddress;
        this.aliasName = aliasName;
        this.level = level;
        this.master = master;
        this.timestamp = timestamp;

    }
    public IntercomUserBean(String ipAddress, String level, long timestamp){
        this.ipAddress = ipAddress;
        this.level = level;
        this.timestamp = timestamp;
    }
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }
    public void updateUser(String level, String master, long timestamp){
        this.level = level;
        this.master = master;
        this.timestamp = timestamp;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntercomUserBean userBean = (IntercomUserBean) o;

        return ipAddress.equals(userBean.ipAddress);

    }

    @Override
    public int hashCode() {
        return ipAddress.hashCode();
    }
}
