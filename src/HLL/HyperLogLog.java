package HLL;

public class HyperLogLog {
	 private final RegisterSet registerSet;
	    private final int log2m;   //等于log(m)
	    private final double alphaMM;


	    /**
	     *
	     *  rsd = 1.04/sqrt(m)
	     * @param rsd  相对标准偏差,每次估计的在估计均值上下的波动占估计均值的比例   relative standard deviation
	     */
	    public HyperLogLog(double rsd) {//当参数是相对均值误差
	        this(log2m(rsd));
	    }

	    /**
	     * rsd = 1.04/sqrt(m)
	     * m = (1.04 / rsd)^2
	     * @param rsd 相对标准偏差
	     * @return
	     */
	    private static int log2m(double rsd) {
	        return (int) (Math.log((1.106 / rsd) * (1.106 / rsd)) / Math.log(2));
	    }

	    private static double rsd(int log2m) {
	        return 1.106 / Math.sqrt(Math.exp(log2m * Math.log(2)));
	    }


	    /**
	     * accuracy = 1.04/sqrt(2^log2m) 精确度
	     *
	     * @param log2m
	     */
	    public HyperLogLog(int log2m) {//当构造函数参数是分桶数的log值   0000 0001 向左移位（log桶）数位0100 0000 64
	        this(log2m, new RegisterSet(1 << log2m));//转下构造函数
	    }

	    /**
	     *
	     * @param registerSet： 一个registerset存放一个桶的结果
	     */
	    public HyperLogLog(int log2m, RegisterSet registerSet) {
	        this.registerSet = registerSet; 
	        this.log2m = log2m;
	        int m = 1 << this.log2m; //从log2m中算出m 分桶数

	        alphaMM = getAlphaMM(log2m, m);
	    }


	    public boolean offerHashed(int hashedValue) {
	        // j 代表第几个桶,取hashedValue的前log2m位即可
	        // j 介于 0 到 m
	        final int j = hashedValue >>> (Integer.SIZE - log2m);//32位int取高位logm位作为桶的下标
	        // r代表 除去前log2m位剩下部分的前导零 + 1
	        final int r = Integer.numberOfLeadingZeros((hashedValue << this.log2m) | (1 << (this.log2m - 1)) + 1) + 1;
	        return registerSet.updateIfGreater(j, r);
	    }

	    /**
	     * 添加元素
	     * @param o  要被添加的元素
	     * @return
	     */
	    public boolean offer(Object o) {
	        final int x = MurmurHash.hash(o);
	        return offerHashed(x);
	    }


	    public long cardinality() {
	        double registerSum = 0;
	        int count = registerSet.count;
	        double zeros = 0.0;
	        //count是桶的数量
	        for (int j = 0; j < registerSet.count; j++) {
	            int val = registerSet.get(j);
	            registerSum += 1.0 / (1 << val);//调和平均数
	            if (val == 0) {
	                zeros++;
	            }
	        }

	        double estimate = alphaMM * (1 / registerSum);

	        if (estimate <= (5.0 / 2.0) * count) {  //小数据量修正
	            return Math.round(linearCounting(count, zeros));
	        } else {
	            return Math.round(estimate);
	        }
	    }


	    /**
	     *  计算前缀constant常数的取值
	     * @param p   log2m 分桶数的log值
	     * @param m   m     分桶数目
	     * @return
	     */
	    protected static double getAlphaMM(final int p, final int m) {
	        // See the paper.根据参数确定前缀常量值
	        switch (p) {
	            case 4:
	                return 0.673 * m * m;
	            case 5:
	                return 0.697 * m * m;
	            case 6:
	                return 0.709 * m * m;
	            default:
	                return (0.7213 / (1 + 1.079 / m)) * m * m;
	        }
	    }

	    /**
	     *
	     * @param m   桶的数目
	     * @param V   桶中0的数目
	     * @return
	     */
	    protected static double linearCounting(int m, double V) {
	        return m * Math.log(m / V);
	    }

	    public static void main(String[] args) {
	        HyperLogLog hyperLogLog = new HyperLogLog(0.1325);//64个桶
	        //集合中只有下面这些元素
	        for(int i=0; i<2300;i++) {
	        	hyperLogLog.offer(i);
	        }
	        
	        //估算基数
	        System.out.println(hyperLogLog.cardinality());
	    }
	}

