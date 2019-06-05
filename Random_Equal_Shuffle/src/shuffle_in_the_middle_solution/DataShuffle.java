package shuffle_in_the_middle_solution;
import mapreduce.JAES;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class DataShuffle {
	 static String password="xidian320";
	 static byte[] encryptC=JAES.encrypt("_1", password);
	 static int numReduceTasks =8;
	 static String dummyStr = "321";
	 static byte[] encryptD=JAES.encrypt(dummyStr, password);
	 static int MAX = 10000;
	 static int num0 = 0;
	 static int num1 = 0;
	 static int num2 = 0;
	 static int num3 = 0;
	 static int num4 = 0;
	 static int num5 = 0;
	 static int num6 = 0;
	 static int num7 = 0;
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		 @Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
			String valuesV = values[1];
			byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(valuesV), password);
			String keyStr=new String(decryptV).trim();
			int r = (keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
			if(r == 0)num0++;if(r == 1)num1++;if(r == 2)num2++;if(r == 3)num3++;if(r == 4)num4++;if(r == 5)num5++;if(r == 6)num6++;if(r == 7)num7++;
			String R = String.valueOf(r);
			byte[] encryptV=JAES.encrypt(valuesV, password);
			byte[] encryptR=JAES.encrypt(R, password);
			context.write(new Text(new String(JAES.parseByte2HexStr(encryptR))), new Text(new String(JAES.parseByte2HexStr(encryptV))+new String(JAES.parseByte2HexStr(encryptC))));
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			byte[] R1 = JAES.encrypt("0", password);
			byte[] R2 = JAES.encrypt("1", password);
			byte[] R3 = JAES.encrypt("2", password);
			byte[] R4 = JAES.encrypt("3", password);
			byte[] R5 = JAES.encrypt("4", password);
			byte[] R6 = JAES.encrypt("5", password);
			byte[] R7 = JAES.encrypt("6", password);
			byte[] R8 = JAES.encrypt("7", password);
			if(num0 < MAX){
				for(int i=0; i < (MAX-num0);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R1))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num1 < MAX){
				for(int i=0; i < (MAX-num1);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R2))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num2 < MAX){
				for(int i=0; i < (MAX-num2);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R3))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num3 < MAX){
				for(int i=0; i < (MAX-num3);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R4))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num4 < MAX){
				for(int i=0; i < (MAX-num4);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R5))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num5 < MAX){
				for(int i=0; i < (MAX-num5);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R6))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num6 < MAX){
				for(int i=0; i < (MAX-num6);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R7))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			if(num7 < MAX){
				for(int i=0; i < (MAX-num7);++i){
					context.write(new Text(new String(JAES.parseByte2HexStr(R8))),new Text(new String(JAES.parseByte2HexStr(encryptD))+new String(JAES.parseByte2HexStr(encryptC))));
				}
			}
			super.cleanup(context);
		}
	}
	public static class ShuffleReduce extends Reducer<Text,Text,Text,Text>{
		@Override
		protected void setup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
			int sum=0;
			String ifdummy = "";
			for(Text i:values){
				byte[] value=JAES.decrypt(JAES.parseHexStr2Byte(i.toString()), password);
				String valueStr=new String(value).trim();
				ifdummy = valueStr.substring(0,valueStr.indexOf("_"));
				String count = valueStr.substring(valueStr.indexOf("_")+1,valueStr.length());
				if(ifdummy != "321"){
					sum+=Integer.valueOf(count);
				}
			}
			if(ifdummy != "321"){
				//byte[] keyB=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				//String valueStr=new String(keyB).trim();
				byte[] keyB = JAES.decrypt(JAES.parseHexStr2Byte(ifdummy.replace("\"", "").trim()), password);
				context.write(new Text(new String(keyB)), new Text(String.valueOf(sum)));
			    }
			}
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			
			super.cleanup(context);
		}
		
	}
	static class MyPartitioner extends HashPartitioner<Text,Text>{
		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			//byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(value.toString()), password);
			String keyStr=new String(decryptK).trim();
			//String valueStr = new String(decryptV);
			int r =Integer.valueOf(keyStr);
			return r;		
		}
	}
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//获取配置对象信息
    	Configuration conf = new Configuration();
//    	Job job =Job.getInstance(conf,"mapreduce");
    	Job job =new Job();
    	//设置job的运行主类
    	job.setJarByClass(DataShuffle.class);
    	//对map阶段进行设置
    	job.setMapperClass(MyMapper.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(Text.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	//对reduce阶段设置、
    	//job.setCombinerClass(MyCombiner.class);
    	job.setReducerClass(ShuffleReduce.class);
    	job.setPartitionerClass(MyPartitioner.class);
    	job.setOutputKeyClass(Text.class);
    	job.setNumReduceTasks(8);//reduce 数量设定
    	job.setOutputValueClass(Text.class);
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));
    	//提交job
    	int isok = job.waitForCompletion(true)? 0 : 1;
    	//退出
    	long endTime=System.currentTimeMillis();
    	System.out.println("运行时间："+(endTime-startTime)+"ms");
    	System.exit(isok);
    }
}

	
