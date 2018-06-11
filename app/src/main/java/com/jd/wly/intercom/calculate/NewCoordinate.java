package com.jd.wly.intercom.calculate;

import android.net.http.SslCertificate;

import com.jd.wly.intercom.users.GPSstatelliteInfo;
import com.jd.wly.intercom.users.IntercomUserBean;
import com.jd.wly.intercom.users.Statellite;
import com.jd.wly.intercom.util.Constants;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class NewCoordinate {
    private double x=0,y=0,z=0,d=0;

    public void calculate_beifen(Statellite [] gSet, List<Integer> statelist, List<Double> Ylist,long time, long statellite_time, double gps_x, double gps_y, double gps_z){

        ArithmeticOfCalcFour arithmeticOfCalcFour = new ArithmeticOfCalcFour();
        // 1 初始化矩阵

        double x1 = gSet[statelist.get(0)].getX(), x2 = gSet[statelist.get(1)].getX(), x3 = gSet[statelist.get(2)].getX(), x4 = gSet[statelist.get(3)].getX(),x5 = gSet[statelist.get(4)].getX();
        double y1 = gSet[statelist.get(0)].getY(), y2 = gSet[statelist.get(1)].getY(), y3 = gSet[statelist.get(2)].getY(), y4 = gSet[statelist.get(3)].getY(),y5 = gSet[statelist.get(4)].getY();
        double z1 = gSet[statelist.get(0)].getZ(), z2 = gSet[statelist.get(1)].getZ(), z3 = gSet[statelist.get(2)].getZ(), z4 = gSet[statelist.get(3)].getZ(),z5 = gSet[statelist.get(4)].getZ();


        // A = Rn + drm + time_ratio*(t-t0)
        //  计算手机伪距
        double A1 = Ylist.get(0)+ gSet[statelist.get(0)].getDrm() + gSet[statelist.get(0)].getTime_ratio()*(time-statellite_time);
        double A2 = Ylist.get(1)+ gSet[statelist.get(1)].getDrm() + gSet[statelist.get(1)].getTime_ratio()*(time-statellite_time);
        double A3 = Ylist.get(2)+ gSet[statelist.get(2)].getDrm() + gSet[statelist.get(2)].getTime_ratio()*(time-statellite_time);
        double A4 = Ylist.get(3)+ gSet[statelist.get(3)].getDrm() + gSet[statelist.get(3)].getTime_ratio()*(time-statellite_time);
        double A5 = Ylist.get(4)+ gSet[statelist.get(4)].getDrm() + gSet[statelist.get(4)].getTime_ratio()*(time-statellite_time);
        System.out.println("伪距改正数:"+gSet[statelist.get(0)].getDrm());
        System.out.println("伪距改正数:"+gSet[statelist.get(1)].getDrm());
        System.out.println("伪距改正数:"+gSet[statelist.get(2)].getDrm());
        System.out.println("伪距改正数:"+gSet[statelist.get(3)].getDrm());
        System.out.println("伪距改正数:"+gSet[statelist.get(3)].getDrm());

//        double A1 = distance(x1, y1, z1, gps_x, gps_y, gps_z)+ gSet[0].getDrm() + gSet[0].getTime_ratio()*(time-statellite_time);
//        double A2 = distance(x2, y2, z2, gps_x, gps_y, gps_z)+ gSet[1].getDrm() + gSet[1].getTime_ratio()*(time-statellite_time);
//        double A3 = distance(x3, y3, z3, gps_x, gps_y, gps_z)+ gSet[2].getDrm() + gSet[2].getTime_ratio()*(time-statellite_time);
//        double A4 = distance(x4, y4, z4, gps_x, gps_y, gps_z)+ gSet[3].getDrm() + gSet[3].getTime_ratio()*(time-statellite_time);
//        double A5 = distance(x5, y5, z5, gps_x, gps_y, gps_z)+ gSet[4].getDrm() + gSet[4].getTime_ratio()*(time-statellite_time);

        System.out.println("A1="+A1);
        System.out.println("A2="+A2);
        System.out.println("A3="+A3);
        System.out.println("A4="+A4);
        // 4-1
        double r1 = rn(x1,x2,y1,y2,z1,z2,A1,A2),
               r2 = rn(x2,x3,y2,y3,z2,z3,A2,A3),
               r3 = rn(x3,x4,y3,y4,z3,z4,A3,A4),
               r4 = rn(x4,x5,y4,y5,z4,z5,A4,A5);

        double [][] strss = new double[][]{
                {x2-x1, y2-y1, z2-z1, A1-A2},
                {x3-x2, y3-y2, z3-z2, A2-A3},
                {x4-x3, y4-y3, z4-z3, A3-A4},
                {x5-x4, y5-y4, z5-z4, A4-A5}
        };

//        strss = idive7(strss);
        Matrix matrix = new Matrix(strss);
        System.out.println("矩阵的秩:"+matrix.rank());

        double [][] strss2 = new double[][]{
                {x2-x1, y2-y1, z2-z1, A1-A2,r1},
                {x3-x2, y3-y2, z3-z2, A2-A3,r2},
                {x4-x3, y4-y3, z4-z3, A3-A4,r3},
                {x5-x4, y5-y4, z5-z4, A4-A5,r4}
        };
//        strss2 = idive7(strss2);
        Matrix rrtest = new Matrix(strss2);
        System.out.println("矩阵的秩:"+rrtest.rank());
        printstrss(matrix.getArray());
        double [][] rMatrix = new double[][]{
                {r1},{r2},{r3},{r4}
        };
//        rMatrix = idive7(rMatrix);
        printstrss(rMatrix);
        if(matrix.det()==0){
            System.out.println("不可逆矩阵");
            return;
        }else{
            System.out.println("逆矩阵:");
            printstrss(matrix.inverse().getArray());
        }
        Matrix matrix1 = new Matrix(rMatrix);

        Matrix result = matrix.solve(matrix1);

        double [][] resultMatrix = result.getArray();
        printstrss(resultMatrix);
        this.x = resultMatrix[0][0];
        this.y = resultMatrix[1][0];
        this.z = resultMatrix[2][0];
        this.d = resultMatrix[3][0];
//        x=-1.3614318958854903E7;
//        y=2.6591198210260846E7;
//        z= 1.8004526603952117E7;
//        d=-8.388608E7 ;
        System.out.println(x*(x2-x1)+ y*(y2-y1)+z*(z2-z1)+ d*(A1-A2)-r1);
//
    }

    public void calculate(Statellite [] gSet, GPSstatelliteInfo[] gpSstatelliteInfoList, long time, long statellite_time, double B, double L, double H, double X, double Y, double Z){
//        if(gpSstatelliteInfoList.isEmpty()){
//            System.out.println("gpslist is Empty");
//            return;
//        }
        int cnt = 0;
//        double [][] alist = new double[][]{}; // 存储Ai，最后能构成
//        double [][] plist = new double[][]{};
//        double [][] drmlist = new double[][]{};

//        List<Double> atmp = new ArrayList<>();
        List<Integer> statelist = new ArrayList<>();
        List<Double> Ylist = new ArrayList<>();
        for(Statellite statellite: gSet){
            if(statellite == null) {
                break;
            }
            int state_number = Integer.valueOf(statellite.getNumber().replace("G",""));
            if(gpSstatelliteInfoList[state_number] == null){
                continue;
            }else{
                //计算数据
                GPSstatelliteInfo gps = gpSstatelliteInfoList[state_number];
                System.out.println("找到该卫星 = " + state_number);
                statelist.add(state_number);
                //dtmp.add(statellite.getDrm());
                double [][] R_ = {
                        {-Math.sin(B) * Math.cos(L), -Math.sin(B) * Math.sin(L), Math.cos(B), 0},
                        {-Math.sin(L), Math.cos(L), 0, 0},
                        {Math.cos(L) * Math.cos(B), Math.sin(L)*Math.cos(B), Math.sin(B), 0},
                        {0,0,0,1}
                };
                Matrix R = new Matrix(R_);

                double [][] tt = {
                        {Math.cos(gps.getBeta()) * Math.cos(gps.getAlpha()), Math.cos(gps.getBeta() * Math.sin(gps.getAlpha())), Math.sin(gps.getBeta()), 1}
                };

                Matrix tmatrix = new Matrix(tt);
                //
                Matrix Ai = tmatrix.times(R); // 矩阵乘 Ai 为1x4矩阵， P为1x1
                System.out.println("Ai:");
                printstrss(Ai.getArray());
                double Pi = 1.0/Math.pow(1.02/(Math.sin(gps.getBeta()) + 0.02), 2);
                Matrix AiT = Ai.transpose();
                double delta_y = statellite.getDrm();
//                printstrss(AiT.times(Pi).times(Ai).times(1e2).getArray());
                Matrix A_pinv = AiT.times(Pi).times(Ai);
                Matrix delta_x = null;
                if(A_pinv.det() == 0){
                    System.out.println("不可逆");
                    SingularValueDecomposition svd =A_pinv.svd();
                    Matrix S = svd.getS();
                    Matrix V = svd.getV().transpose();
                    Matrix U = svd.getU();
                    Matrix sinv = UnaryNotZeroElement(S);
                    Matrix inv = V.times(sinv).times(U.transpose());

                    delta_x = inv.times(AiT).times(Pi).times(delta_y);
                }else {
                    delta_x = A_pinv.transpose().times(AiT).times(Pi).times(delta_y);
                }

                System.out.println("delta_x");
                printstrss(delta_x.getArray());
                Matrix gpsX = new Matrix(new double[][]{
                        {X},{Y},{Z},{0}
                });
                gpsX = gpsX.minus(delta_x);

                double Yi = distance(statellite.getX(),statellite.getY(), statellite.getZ(), gpsX.get(0,0),gpsX.get(1,0),gpsX.get(2,0));
                double dis = distance(statellite.getX(),statellite.getY(), statellite.getZ(), X,Y,Z);
                System.out.println("伪距+改正数 - 目前坐标到卫星的距离"+(Yi+ delta_y - dis ));
                System.out.println("伪距 - 目前坐标到卫星的距离"+(Yi - dis ) + " 改正数=" + delta_y);

                Ylist.add(Yi);


                //ptmp.add(Pi);


            }

        }
        if(Ylist.size()<5 ){
            System.out.println("取得的卫星数目不够 "+ Ylist.size());
            return;
        }

        calculate_beifen(gSet,statelist,Ylist,time,statellite_time,X,Y,Z);
//
//        Matrix A = new Matrix(trans2double(atmp));
//        Matrix P = new Matrix(trans2double(ptmp));
//        Matrix delta_Y = new Matrix(trans2double(dtmp));
//        Matrix AT = A.transpose(); // A的转置
//        Matrix delta_result = AT.times(P).times(A).inverse().times(AT).times(P).times(delta_Y);
//        double [][] delta = delta_result.getArray();
//        this.x = delta[0][0];
//        this.y = delta[0][1];
//        this.z = delta[0][1];



    }

    private static Matrix UnaryNotZeroElement(Matrix x) {
        double[][] array=x.getArray();
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array[i].length;j++){
                if(array[i][j]!=0){
                    array[i][j]=1.0/array[i][j];
                }
            }
        }
        return new Matrix(array);
    }
    public double[][] trans2double(List<Double> dlist){
       int length = dlist.size();
       double [][] tmp = new double[length][length];
       for(int i =0; i< length; i ++){
           tmp[0][i] = dlist.get(i);
       }
       return tmp;
    }
    public double distance(double s_x, double s_y, double s_z, double gps_x, double gps_y, double gps_z){
        double r = 0;
        r = Math.pow(s_x - gps_x, 2) + Math.pow(s_y - gps_y, 2) + Math.pow(s_z - gps_z, 2);
        r = Math.sqrt(r);
        return r;
    }
    public double rn(double x1 , double x2, double y1, double y2, double z1, double z2 , double A1, double A2){
       double tmp =  Math.pow(x2,2)-Math.pow(x1,2)+Math.pow(y2,2)-Math.pow(y1,2)+Math.pow(z2,2)-Math.pow(z1,2)+Math.pow(A1,2)-Math.pow(A2,2);
       return tmp/2;
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
    public void printstrss(double [][] strss){
        System.out.println("begin show matrix:");
        for(int i=0; i< strss.length; i++){
            for(int j=0; j<strss[0].length; j++){
                System.out.print(strss[i][j]+" ");
            }
            System.out.print("\n");
        }
        System.out.println("end show matrix:");
    }
//    public double[][] idive7(double [][] strss){
////        System.out.println("begin show matrix:");
//        for(int i=0; i< strss.length; i++){
//            for(int j=0; j<strss[0].length; j++){
//                strss[i][j] = strss[i][j]/1e7;
//            }
////            System.out.print("\n");
//        }
//        return strss;
//    }
    public void printlie(double [][]strss){
        System.out.println("begin show matrix:");
        for(int i=0; i< strss.length; i++){
            for(int j=0; j<strss[0].length; j++){
                System.out.println(strss[j][i]);
            }
            System.out.print("下一列");
        }
    }
}
