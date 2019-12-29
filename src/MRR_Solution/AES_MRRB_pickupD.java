package MRR_Solution;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class AES_MRRB_pickupD {
	static int numReduceTasks =7;
	static String password="xidian320";
	static byte[] encryptV=JAES.encrypt("1", password);
	static ArrayList<String> pickupD = new ArrayList<String>();
	static ArrayList<String> S_pickupD = new ArrayList<String>();
/*job1*/
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		@Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
		    pickupD.add("2013-1-1");pickupD.add("2013-1-2");pickupD.add("2013-1-3");pickupD.add("2013-1-4");pickupD.add("2013-1-5");pickupD.add("2013-1-6");pickupD.add("2013-1-7");pickupD.add("2013-1-8");
		    pickupD.add("2013-1-9");pickupD.add("2013-1-10");pickupD.add("2013-1-11");pickupD.add("2013-1-12");pickupD.add("2013-1-13");pickupD.add("2013-1-14");pickupD.add("2013-1-15");pickupD.add("2013-1-16");
		    pickupD.add("2013-1-17");pickupD.add("2013-1-18");pickupD.add("2013-1-19");pickupD.add("2013-1-20");pickupD.add("2013-1-21");pickupD.add("2013-1-22");pickupD.add("2013-1-23");pickupD.add("2013-1-24");
		    pickupD.add("2013-1-25");pickupD.add("2013-1-26");pickupD.add("2013-1-27");pickupD.add("2013-1-28");pickupD.add("2013-1-29");pickupD.add("2013-1-30");pickupD.add("2013-1-31");
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
//			byte[] encryptK=JAES.encrypt(values[5	], password);
//			byte[] encryptV=JAES.encrypt("1", password);
//			context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
			context.write(new Text(values[5]), new Text(new String(JAES.parseByte2HexStr(encryptV))));	
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
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
		protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String s =new String(decryptV).trim();
				count+=Integer.parseInt(s);
			}
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
			context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));	
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			for (String entry : pickupD){
				if(! S_pickupD.contains(entry)){
					byte[] encryptK=JAES.encrypt(entry, password);
					byte[] encryptV=JAES.encrypt("0", password);
					context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
				}
			}
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
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String s =new String(decryptK).trim();
			if(! S_pickupD.contains(s)){
				S_pickupD.add(s);
			}
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
			int s = (int)(Math.random()*numReduceTasks);
			return s;
		}
	}
/*job2*/
	 public static class MyMapper2 extends Mapper<LongWritable,Text,Text,Text>{
			@Override
			protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
				
			}
			@Override
			protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				String[] split=value.toString().split("	");
				context.write(new Text(split[0]),new Text(split[1]));
			}
			@Override
			protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.cleanup(context);
			
			}
		}
		public static class ShuffleReduce2 extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text,Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
				
			}
			@Override
			protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
				int count=0;
				for(Text v:values){
					byte[] value=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
					String valueStr=new String(value).trim();
					count+=Integer.parseInt(valueStr);
				}
				if(count!=0){
					byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
					context.write(new Text(new String(k).trim().replace("\"", "")), new Text(String.valueOf(count)));
				}
			}
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.cleanup(context);
				
			}
		}
		static class MyPartitioner2 extends HashPartitioner<Text,Text>{
			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String keyStr=new String(k).trim();
				return (keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				
				
			}
		}
		
    public static void main(String[] args) throws IOException,URISyntaxException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//获取配置对象信息
    	Configuration conf = new Configuration();
//job1设置 	
//    	Job job1 =Job.getInstance(conf,"job1");
    	Job job1 =new Job();
    	
    	//设置job的运行主类
    	job1.setJarByClass(AES_MRRB_pickupD.class);
    	FileInputFormat.setInputPaths(job1, new Path(args[0]));
    	//对map阶段进行设置
    	job1.setMapperClass(MyMapper.class);
    	job1.setMapOutputKeyClass(Text.class);
    	job1.setMapOutputValueClass(Text.class);
    	//Partition
    	job1.setCombinerClass(MyCombiner.class);
    	job1.setPartitionerClass(MyPartitioner.class);
    	//对reduce阶段设置、
    	job1.setReducerClass(ShuffleReduce.class);
    	job1.setOutputKeyClass(Text.class);
    	job1.setNumReduceTasks(7);//reduce 数量设定
    	job1.setOutputValueClass(Text.class);
    	//提交job
    	FileOutputFormat.setOutputPath(job1, new Path(args[1]));
    	//控制器设置
        ControlledJob ctrlJob1= new ControlledJob(conf);
        ctrlJob1.setJob(job1);
//job2设置
//        Job job2 =Job.getInstance(conf,"job2");
        Job job2 =new Job();
    	//设置job的运行主类
        job2.setJarByClass(AES_MRRB_pickupD.class);
    	FileInputFormat.setInputPaths(job2, new Path(args[1]));
    	//对map阶段进行设置
    	job2.setMapperClass(MyMapper2.class);
    	job2.setMapOutputKeyClass(Text.class);
    	job2.setMapOutputValueClass(Text.class);
    	//Partition
    	job2.setPartitionerClass(MyPartitioner2.class);
    	//对reduce阶段设置、
    	job2.setReducerClass(ShuffleReduce2.class);
    	job2.setOutputKeyClass(Text.class);
    	job2.setNumReduceTasks(7);//reduce 数量设定
    	job2.setOutputValueClass(Text.class);
    	//提交job
    	FileOutputFormat.setOutputPath(job2, new Path(args[2]));
    	//控制器设置
        ControlledJob ctrlJob2= new ControlledJob(conf);
        ctrlJob2.setJob(job2);
        if (job1.waitForCompletion(true)) {
            System.exit(job2.waitForCompletion(true) ? 0 : 1);
 	       }
    }
}
