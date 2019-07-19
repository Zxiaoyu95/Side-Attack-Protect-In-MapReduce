package solution_in_paper;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class Lean_Shuffle {
	static int numReduceTasks =4;
	static String password="xidian320";
	static byte[] encryptV=JAES.encrypt("1", password);
	static ArrayList<String> key_set = new ArrayList<String>();
	static ArrayList<String> S_key_set = new ArrayList<String>();
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
			@Override
			protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				//passenN7
//				key_set.add("9");key_set.add("6");key_set.add("5");key_set.add("4");
//				key_set.add("3");key_set.add("2");key_set.add("1");key_set.add("0");
//				key_set.add("208");key_set.add("255");
				//pickupD5
//			key_set.add("2013-1-1");key_set.add("2013-1-2");key_set.add("2013-1-3");key_set.add("2013-1-4");key_set.add("2013-1-5");key_set.add("2013-1-6");key_set.add("2013-1-7");key_set.add("2013-1-8");
//				key_set.add("2013-1-9");key_set.add("2013-1-10");key_set.add("2013-1-11");key_set.add("2013-1-12");key_set.add("2013-1-13");key_set.add("2013-1-14");key_set.add("2013-1-15");key_set.add("2013-1-16");
//			key_set.add("2013-1-17");key_set.add("2013-1-18");key_set.add("2013-1-19");key_set.add("2013-1-20");key_set.add("2013-1-21");key_set.add("2013-1-22");key_set.add("2013-1-23");key_set.add("2013-1-24");
//				key_set.add("2013-1-25");key_set.add("2013-1-26");key_set.add("2013-1-27");key_set.add("2013-1-28");key_set.add("2013-1-29");key_set.add("2013-1-30");key_set.add("2013-1-31");
				//dAge1
//		key_set.add("7");key_set.add("6");key_set.add("5");key_set.add("4");
//		key_set.add("3");key_set.add("2");key_set.add("1");key_set.add("0");
				//dPOB35
//				key_set.add("6");key_set.add("5");key_set.add("4");
//				key_set.add("3");key_set.add("2");key_set.add("1");key_set.add("0");
				//iMarital29
			key_set.add("4");key_set.add("3");key_set.add("2");key_set.add("1");key_set.add("0");
//				super.setup(context);
			}
			@Override
			protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				String valueStr=value.toString();
				String [] values=valueStr.split("	");
				byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(values[29]), password);
				String s =new String(decryptK).trim();
				if(! S_key_set.contains(s)){
					S_key_set.add(s);
				}
				context.write(new Text(values[29]), new Text(new String(JAES.parseByte2HexStr(encryptV))));	
			}
			@Override
			protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				 for (String entry : key_set){
						if(! S_key_set.contains(entry)){
							byte[] encryptK=JAES.encrypt(entry, password);
							byte[] encryptV=JAES.encrypt("0", password);
							context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
						}
					}
				super.cleanup(context);
				
			}
		}
		public static class ShuffleReduce extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text,Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
			
			}
			@Override
			protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {	int count=0;
			int sum = 0;
			for(Text v:values){
				byte[] value=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String valueStr=new String(value).trim();
				sum+=Integer.parseInt(valueStr);
			}
			if(sum !=0){//真数据
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				context.write(new Text(new String(k).trim().replace("\"", "")), new Text(String.valueOf(sum)));
				}
			if(sum == 0){
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				context.write(new Text(new String(k).trim().replace("\"", "")), new Text(String.valueOf(0)));
			}
			
			}	
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				
				super.cleanup(context);
				
			}
		}
		static class MyCombiner extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
				
			}
			@Override
			protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
				int count=0;
				for(Text v:values){
					count+=1;
				}
				byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
//				byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
//				String s =new String(decryptK).trim();
//				if(! S_passenN.contains(s)){
//					S_passenN.add(s);
//				}
				context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));
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
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String keyStr=new String(k).trim();
				return (keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
			}
		}
		   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		    	long startTime=System.currentTimeMillis();
		    	//获取配置对象信息
		    	Configuration conf = new Configuration();
//		    	Job job =Job.getInstance(conf,"mapreduce");
		    	Job job =new Job();
		    	//设置job的运行主类
		    	job.setJarByClass(Lean_Shuffle.class);
		    	//对map阶段进行设置
		    	job.setMapperClass(MyMapper.class);
		    	job.setMapOutputKeyClass(Text.class);
		    	job.setMapOutputValueClass(Text.class);
		    	FileInputFormat.addInputPath(job, new Path(args[0]));
		    	//对reduce阶段设置、
		    	job.setCombinerClass(MyCombiner.class);
		    	job.setReducerClass(ShuffleReduce.class);
		    	job.setPartitionerClass(MyPartitioner.class);
		    	job.setOutputKeyClass(Text.class);
		    	job.setNumReduceTasks(numReduceTasks);//reduce 数量设定
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
