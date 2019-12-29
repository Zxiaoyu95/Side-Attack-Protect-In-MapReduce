package Add_KeyEstimate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

public class MR_Estimate{
    static int numReduceTasks =7;
	static String password="xidian320";
	static int numberKey;
	static byte[] encryptV=JAES.encrypt("1", password);
	static ArrayList<String> key_set = new ArrayList<String>();
	static ArrayList<String> S_key_set = new ArrayList<String>();
	static HyperLogLog hyperLogLog = new HyperLogLog(0.1325);//64个桶
	static Map<String,String> result = new HashMap<String,String>();
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
			hyperLogLog.offer(s);
			if(! S_key_set.contains(s)){
				S_key_set.add(s);
			}
			//context.write(new Text(values[5]), new Text(new String(JAES.parseByte2HexStr(encryptV))));
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub	
			    //numberKey = S_key_set.size();
			    Long estimate = hyperLogLog.cardinality();
				byte[] encryptK=JAES.encrypt("NULL", password);
				byte[] encryptV=JAES.encrypt(String.valueOf(estimate), password);
				context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
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
			byte[] encryptV=JAES.encrypt(String.valueOf(MAX), password);
			numberKey = MAX;
			//context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));
			context.write(key, new Text(String.valueOf(MAX)));
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			
			super.cleanup(context);
		}
	}
	
/*job2*/
	public static class MyMapper2 extends Mapper<LongWritable,Text,Text,Text>{
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
			String strK =new String(decryptK).trim();
			if(! S_key_set.contains(strK)){
				S_key_set.add(strK);
			}		
			int r=(strK.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
			byte[] encryptK=JAES.encrypt(strK+"_"+r+"#"+r, password);
			context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))),new Text(new String(JAES.parseByte2HexStr(encryptV))));
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
			//int count=0;
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String keylongstr = new String(decryptK).trim().replace("\"", "");
			String mykey = keylongstr.substring(0,keylongstr.indexOf("_"));		
			for(Text value:values){
				int j=Integer.parseInt(keylongstr.substring(keylongstr.indexOf("_")+1,keylongstr.indexOf("#")));
				int r=Integer.parseInt(keylongstr.substring(keylongstr.indexOf("#")+1,keylongstr.length()));
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(value.toString()), password);
				String v = new String(decryptV).trim();
				if(j==r && !result.containsKey(mykey)){
					result.put(mykey, "0");
				}
				for(Map.Entry<String,String> str : result.entrySet()){
					 if(j==r && str.getKey().contains(mykey)){
						    int rel=Integer.valueOf(v)+Integer.valueOf(str.getValue());
		            	    result.put(mykey, String.valueOf(rel));
							//count+=Integer.parseInt(v.toString());
		            }
				}
	           
			}
//			if(count!=0){
//			context.write(new Text(mykey), new Text(String.valueOf(count)));}
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			for(Map.Entry<String,String> str : result.entrySet()){
				 //if( !str.getValue().equals("0")){
					 context.write(new Text(str.getKey()), new Text(str.getValue()));
	            //}
			}
			result = new HashMap<String,String>();
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
			String keylongstr = new String(decryptK).trim();
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
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
			    int Tnum = S_key_set.size();
				int r=("FAKE".hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				if(Tnum < numberKey){
					for(int j=0; j<(numberKey-Tnum);j++) {
						for(int i=0;i<numReduceTasks;i++){
							byte[] encryptK=JAES.encrypt("FAKE"+"_"+i+"#"+r, password);
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
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String vuleStr=new String(decryptK).trim();
			int r = Integer.parseInt(vuleStr.substring(vuleStr.indexOf("_")+1,vuleStr.indexOf("#")));
			return r;
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
    	job1.setJarByClass(MR_Estimate.class);
    	FileInputFormat.setInputPaths(job1, new Path(args[0]));
    	//对map阶段进行设置
    	job1.setMapperClass(MyMapper.class);
    	job1.setMapOutputKeyClass(Text.class);
    	job1.setMapOutputValueClass(Text.class);
    	//Partition
    	
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
        job2.setJarByClass(MR_Estimate.class);
    	FileInputFormat.setInputPaths(job2, new Path(args[0]));
    	//对map阶段进行设置
    	job2.setMapperClass(MyMapper2.class);
    	job2.setMapOutputKeyClass(Text.class);
    	job2.setMapOutputValueClass(Text.class);
    	//Partition
    	job2.setCombinerClass(MyCombiner.class);
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
