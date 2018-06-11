package com.jd.wly.intercom.users;

public class Statellite {
    private double drm = 0;
    private double time_ratio = 0;
    private double x,y,z = 0;
    private String number = "";

    @Override
    public String toString() {
        return drm + "," + time_ratio + "," + x + "," + y + "," + z + ",G" + number;
    }

    public double getDrm() {
        return drm;
    }

    public void setDrm(double drm) {
        this.drm = drm;
    }

    public double getTime_ratio() {
        return time_ratio;
    }

    public void setTime_ratio(double time_ratio) {
        this.time_ratio = time_ratio;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
