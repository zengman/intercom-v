package com.jd.wly.intercom.calculate;

public class ArithmeticOfCalcFour {
    private double x,y,z,d;
//    public static void main(String[] args) {
//// 例子
//// x+y-z=9;
//// x-y+2z=8;
//// 2x+y-z=7;
//// x+y+z=m;
//// 解x=-2,y=32,z=21,m=51
//        double[][] strss={
//                {1,1,-1,0,9},
//                {1,-1,2,0,8},
//                {2,1,-1,0,7},
//                {1,1,1,-1,0},
//        };
//        calculate(strss);
//
//    }

    public boolean calculate(double[][] strss) {
        // 算出解返回true，否则返回false

        double[][] dss={
                {strss[0][0],strss[0][1],strss[0][2],strss[0][3]},
                {strss[1][0],strss[1][1],strss[1][2],strss[1][3]},
                {strss[2][0],strss[2][1],strss[2][2],strss[2][3]},
                {strss[3][0],strss[3][1],strss[3][2],strss[3][3]}
        };
        double[][] d1ss={
                {strss[0][4],strss[0][1],strss[0][2],strss[0][3]},
                {strss[1][4],strss[1][1],strss[1][2],strss[1][3]},
                {strss[2][4],strss[2][1],strss[2][2],strss[2][3]},
                {strss[3][4],strss[3][1],strss[3][2],strss[3][3]}
        };
        double[][] d2ss={
                {strss[0][0],strss[0][4],strss[0][2],strss[0][3]},
                {strss[1][0],strss[1][4],strss[1][2],strss[1][3]},
                {strss[2][0],strss[2][4],strss[2][2],strss[2][3]},
                {strss[3][0],strss[3][4],strss[3][2],strss[3][3]}
        };
        double[][] d3ss={
                {strss[0][0],strss[0][1],strss[0][4],strss[0][3]},
                {strss[1][0],strss[1][1],strss[1][4],strss[1][3]},
                {strss[2][0],strss[2][1],strss[2][4],strss[2][3]},
                {strss[3][0],strss[3][1],strss[3][4],strss[3][3]}
        };
        double[][] d4ss={
                {strss[0][0],strss[0][1],strss[0][2],strss[0][4]},
                {strss[1][0],strss[1][1],strss[1][2],strss[1][4]},
                {strss[2][0],strss[2][1],strss[2][2],strss[2][4]},
                {strss[3][0],strss[3][1],strss[3][2],strss[3][4]}
        };

        double d=calculateFour(dss);
        if(d==0){
            System.out.println("无解或多个解");

            return false;
        }
        double d1=calculateFour(d1ss);
        double d2=calculateFour(d2ss);
        double d3=calculateFour(d3ss);
        double d4=calculateFour(d4ss);

        double x=d1/d;
        double y=d2/d;
        double z=d3/d;
        double m=d4/d;
        this.x = x;
        this.y = y;
        this.z = z;
        this.d = m;
        System.out.println("结果：x="+x+" y="+y+" z="+z+" m="+m);
        System.out.println("计算完毕!");
        return true;
    }

    /**
     * 计算4阶线性方程组
     * @param strss
     * @return
     */
    private static double calculateFour(double[][] strss) {

// {strss[0][0],strss[0][1],strss[0][2],strss[0][3]},
// {strss[1][0],strss[1][1],strss[1][2],strss[1][3]},
// {strss[2][0],strss[2][1],strss[2][2],strss[2][3]},
// {strss[3][0],strss[3][1],strss[3][2],strss[3][3]}

        double[][] t00={
                {strss[1][1],strss[1][2],strss[1][3]},
                {strss[2][1],strss[2][2],strss[2][3]},
                {strss[3][1],strss[3][2],strss[3][3]}
        };
        double[][] t01={
                {strss[1][0],strss[1][2],strss[1][3]},
                {strss[2][0],strss[2][2],strss[2][3]},
                {strss[3][0],strss[3][2],strss[3][3]}
        };
        double[][] t02={
                {strss[1][0],strss[1][1],strss[1][3]},
                {strss[2][0],strss[2][1],strss[2][3]},
                {strss[3][0],strss[3][1],strss[3][3]}
        };
        double[][] t03={
                {strss[1][0],strss[1][1],strss[1][2]},
                {strss[2][0],strss[2][1],strss[2][2]},
                {strss[3][0],strss[3][1],strss[3][2]}
        };
        double result=Math.pow(-1, 0+0)*strss[0][0]*calculateThree(t00) + Math.pow(-1, 0+1)*strss[0][1]*calculateThree(t01) +
                Math.pow(-1, 0+2)*strss[0][2]*calculateThree(t02) + Math.pow(-1, 0+3)*strss[0][3]*calculateThree(t03) ;

        return result;
    }


    /**
     * 计算3阶行列式
     * @param strss
     * @return
     */
    private static double calculateThree(double[][] strss) {
// {strss[0][0],strss[0][1],strss[0][2]
// {strss[1][0],strss[1][1],strss[1][2]
// {strss[2][0],strss[2][1],strss[2][2]

        double result=strss[0][0]*strss[1][1]*strss[2][2] +  strss[0][1]*strss[1][2]*strss[2][0] +  strss[0][2]*strss[1][0]*strss[2][1] +
                -strss[0][2]*strss[1][1]*strss[2][0] - strss[0][1]*strss[1][0]*strss[2][2] - strss[0][0]*strss[2][1]*strss[1][2] ;

        return result;
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

    public double getD() {
        return d;
    }
}
