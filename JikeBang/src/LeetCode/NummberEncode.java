package Test;
public class NummberEncode {
    String a;
    double[] tmp =new double[5];
    int n;
    double p1, p2, p3, p4;
    public NummberEncode(String a, int n) {
        this.a = a;
        this.n = n;
        this.p1 = 0.6;
        this.p2 = 0.2;
        this.p3 = 0.1;
        this.p4 = 0.1;
    }
    public double Numencode() {
        double i = 0;
        double m = 1;
        double j = i + p1 * (m-i);
        double k = j + p2 * (m-i);
        double l = k + p3 * (m-i);
        tmp[0]=i;tmp[1]=j;tmp[2]=k;tmp[3]=l;tmp[4]=m;
        for (int num = 0; num < n; num++) {
            int count=a.charAt(num)-65;
            tmp[0]=tmp[count];
            tmp[4]=tmp[count+1];
            tmp[1]=tmp[0]+p1*(tmp[4]-tmp[0]);
            tmp[2]=tmp[1]+p2*(tmp[4]-tmp[0]);
            tmp[3]=tmp[2]+p3*(tmp[4]-tmp[0]);
        }
        return tmp[0];
    }
    public static void main(String[] args){
        String a="ACD";
        NummberEncode ne = new NummberEncode(a,3);
        System.out.println(ne.Numencode());
    }
}
