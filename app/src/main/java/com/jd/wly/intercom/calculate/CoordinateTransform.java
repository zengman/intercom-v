package com.jd.wly.intercom.calculate;


public class CoordinateTransform {

    private  double theta;
    // 维度
    private double B;
    // 经度
    private  double L;
    // 海拔
    private double H;

    private double x;
    private double y;
    private double z;

//    // 地球长半轴
//    private  static double a = 6378136.49;
//    // 地球短半轴
//    private static  double b = 6356755.00;
    // blh to xyz e
//    private double e_a ;
//    // xyz to blh e
//    private double e_b;



    public double dms_rad(double dms){
        //System.out.println("dms"+dms);
        double M_PI = 3.1415926;
        int deg = (int)dms ;
        //System.out.println("deg="+deg);
        int Min = (int)((dms-deg)*100);
        //System.out.println("Min="+Min);
        double sec = ((dms - deg)*100 - Min)*100;
        //System.out.println("sec="+sec);


        double rad = (deg + (double)Min/60.0 + sec/3600.0)/180.0*M_PI;
        //System.out.println("min/60="+(double)Min/60.0);
        //System.out.println("rad="+rad);
        return rad;
    }
    public double rad_dms(double rad){
        double M_PI = 3.1415926;
        double ar = rad;
        if (rad < 0){
            ar = -rad;
        }
        ar += 10E-10;
        ar = ar * 180.0/M_PI;
        int deg = (int)ar;
        double am = (ar-deg) * 60.0;

        int Min = (int)am;

        double sec = (am-Min)*60.0;
        double dms = deg + (double)Min/100.0 + sec/10000.0;
        if (rad < 0) {
            dms = - dms;
        }
        return dms;
    }
    public void blh2xyz(double B, double L, double H){
        double dsm = 6378137;
        double df = 1/298.257223563;
        // 转换这一步很重要
        L = dms_rad(L);

        B = dms_rad(B);
        //System.out.println("B="+B);


        double N = dsm/Math.sqrt(1.0-df*(2.0-df)*Math.sin(B)*Math.sin(B));


        x = (N + H) * Math.cos(B) * Math.cos(L);

        y = (N + H) * Math.cos(B) * Math.sin(L);
        z = (N*(1-df*(2-df))+H)*Math.sin(B);


    }
    public void xyz2blh(double x, double y, double z){
        double dsm = 6378137;
        double df = 1/298.257223563;
        double R = Math.sqrt(x*x + y*y);
        double b0 = Math.atan2(z,R);
        double N;
//        double b;
        while (true) {
            N = dsm / Math.sqrt(1.0 - df * (2 - df) * Math.sin(b0) * Math.sin(b0));
            B = Math.atan2(z + N * df * (2 - df) * Math.sin(b0), R);
            if (Math.abs(B - b0) < 1.0E-10) {
                break;
            }

            b0 = B;
        }

        L = Math.atan2(y,x);
        H = R/Math.cos(B)-N;

        B = rad_dms(B);
        L = rad_dms(L);


    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getB() {
        return B;
    }

    public double getL() {
        return L;
    }

    public double getH() {
        return H;
    }
}
