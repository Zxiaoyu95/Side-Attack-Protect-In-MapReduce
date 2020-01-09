package Add_KeyEstimate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import solution_in_paper.JAES;

public class MR_MiddleTalk_2j {
	static int numReduceTasks =4;
	static String password="xidian320";
	static byte[] encryptV=JAES.encrypt("1", password);
	static ArrayList<String> key_set = new ArrayList<String>();
	static ArrayList<String> S_key_set = new ArrayList<String>();
	static ArrayList<String> S_key_set2 = new ArrayList<String>();
	static int ALLnumberKey;//job1
	static int MAX;//job1
	static int keynum;//job2
/*job1*/
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		@Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(values[1]), password);
			String s =new String(decryptK).trim();
			if(! S_key_set.contains(s)){
				S_key_set.add(s);
			}
			context.write(new Text(values[1]), new Text(new String(JAES.parseByte2HexStr(encryptV))));
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
			
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
				byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String keyStr=new String(decryptK).trim();
				byte[] encryptK=JAES.encrypt(keyStr+"_"+count, password);
				String k_c = new String(JAES.parseByte2HexStr(encryptK));
				key_set.add(k_c);			
			}
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				int keymap = S_key_set.size();
				byte[] encryptV=JAES.encrypt(String.valueOf(keymap), password);
				context.write(new Text(new String("Estimate")), new Text(new String(JAES.parseByte2HexStr(encryptV))));
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
			int MAX=0;
			for(Text v:values){
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String s=new String(decryptV).trim();
				if(Integer.parseInt(s) > MAX) {
					MAX = Integer.parseInt(s);
				};
			}
			ALLnumberKey = MAX;	
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			Iterator it1 = key_set.iterator();
	        while(it1.hasNext()){
	        	byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(it1.next().toString()), password);
	        	String v=new String(decryptK).trim();
	        	int key=Integer.parseInt(v.toString().substring(0,v.indexOf("_")));
				int value=Integer.parseInt(v.toString().substring(v.toString().indexOf("_")+1,v.toString().length()));
				byte[] eK=JAES.encrypt(String.valueOf(key), password);	
				byte[] eV=JAES.encrypt(String.valueOf(value)+"_"+ALLnumberKey, password);
				//context.write(new Text(String.valueOf(key)),new Text(String.valueOf(value)+"_"+ALLnumberKey));
				context.write(new Text(new String(JAES.parseByte2HexStr(eK))),new Text(new String(JAES.parseByte2HexStr(eV))));
	        }
			super.cleanup(context);
		}
	}
	
/*job2*/
	public static class MyMapper2 extends Mapper<LongWritable,Text,Text,Text>{
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] split=value.toString().split("	");
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(split[0]), password);	
			byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(split[1]), password);	
			String keyStr=new String(decryptK).trim();
			String valueStr=new String(decryptV).trim();
        	int count=Integer.parseInt(valueStr.toString().substring(0,valueStr.indexOf("_")));
			keynum=Integer.parseInt(valueStr.toString().substring(valueStr.toString().indexOf("_")+1,valueStr.toString().length()));
			byte[] enV=JAES.encrypt(String.valueOf(count), password);
			if(! S_key_set.contains(keyStr)){
				S_key_set.add(keyStr);
			}
			int r=(keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
			byte[] encryptK=JAES.encrypt(keyStr+"_"+r+"#"+r, password);
			context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(enV))));	
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			
			super.cleanup(context);
			
		}
	}
	static class MyCombiner2 extends Reducer<Text,Text,Text,Text>{
		@Override
		protected void setup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
			
		}
		@Override
		protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				byte[] decryptv=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String s =new String(decryptv).trim();
				int truenum = Integer.parseInt(s);
				count+=truenum;
			}
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String keylongstr = new String(decryptK).trim();
			int r=Integer.parseInt(keylongstr.substring(keylongstr.indexOf("#")+1,keylongstr.length()));
			String mykey = keylongstr.substring(1,keylongstr.indexOf("_"));
			for(int i=0;i<numReduceTasks;i++){
				byte[] encryptK=JAES.encrypt(mykey+"_"+i+"#"+r, password);
				context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))),new Text(new String(JAES.parseByte2HexStr(encryptV))));
			}
		}
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			int factnum = S_key_set.size();
			int r=("FAKE".hashCode()&Integer.MAX_VALUE)%numReduceTasks;
			if(factnum < keynum) {
				for(int i=0;i<(keynum-factnum);i++) {
					for(int j=0;j<numReduceTasks;j++){
						byte[] encryptK=JAES.encrypt("FAKE_"+j+"#"+r, password);
						byte[] encryptV=JAES.encrypt("0", password);
						context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
					}
				}
			}
			super.cleanup(context);
			
		}
	}
	static class MyPartitioner2 extends HashPartitioner<Text,Text>{
		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String keylongstr = new String(k).trim();
			int r=Integer.parseInt(keylongstr.substring(keylongstr.indexOf("#")+1,keylongstr.length()));		
			return r;
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
		protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {	int count=0;
		int sum=0;
		byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
		String keylongstr = new String(decryptK).trim();
		for(Text v:values){
			byte[] decryptv=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
			int j=Integer.parseInt(keylongstr.substring(keylongstr.indexOf("_")+1,keylongstr.indexOf("#")));
			int r=Integer.parseInt(keylongstr.substring(keylongstr.indexOf("#")+1,keylongstr.length()));
			if(j == r) {
				String s =new String(decryptv).trim();
				int truenum = Integer.parseInt(s);
				sum+=truenum;
			}
		}
		//byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
		
		String mykey = keylongstr.substring(0,keylongstr.indexOf("_"));
		if(sum !=0){//真数据
			context.write(new Text(mykey), new Text(String.valueOf(sum)));
			}
		if(sum == 0){
			byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			context.write(new Text(mykey), new Text(String.valueOf(0)));
		}
		
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			
			super.cleanup(context);
			
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
    	job1.setJarByClass(MR_MiddleTalk_2j.class);
    	FileInputFormat.setInputPaths(job1, new Path(args[0]));
    	//对map阶段进行设置
    	job1.setMapperClass(MyMapper.class);
    	job1.setMapOutputKeyClass(Text.class);
    	job1.setMapOutputValueClass(Text.class);
    	//Partition
    	job1.setCombinerClass(MyCombiner.class);
    	
    	//对reduce阶段设置、
    	job1.setReducerClass(ShuffleReduce.class);
    	job1.setOutputKeyClass(Text.class);
    	job1.setNumReduceTasks(1);//reduce 数量设定
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
        job2.setJarByClass(MR_MiddleTalk_2j.class);
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
    	job2.setNumReduceTasks(4);//reduce 数量设定
    	job2.setOutputValueClass(Text.class);
    	//提交job
    	FileOutputFormat.setOutputPath(job2, new Path(args[2]));
    	//控制器设置
        ControlledJob ctrlJob2= new ControlledJob(conf);
        ctrlJob2.setJob(job2);    
        if (job1.waitForCompletion(true)) {
                System.exit(job2.waitForCompletion(true) ? 0 : 1);
        	       }
    	long endTime=System.currentTimeMillis();
    	System.out.println("运行时间："+(endTime-startTime)+"ms");
    }
}
