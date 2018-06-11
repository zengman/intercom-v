package com.jd.wly.intercom.users;

public class GPSstatelliteInfo {
    // 卫星编号
    private int number;
    // 卫星仰角、高度角
    private double beta;
    // 卫星方位角
    private  double alpha;
    // 卫星 讯号噪声比，如果空，则表示未接收到该卫星信号
    private  double isAvailable;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(double isAvailable) {
        this.isAvailable = isAvailable;
    }
}
