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

import MRR_Solution.JAES;

public class Full_Shuffle {
	static int numReduceTasks =7;
	static String password="xidian320";
	static byte[] encryptV=JAES.encrypt("1", password);
	static ArrayList<String> passenN = new ArrayList<String>();
	static ArrayList<String> S_passenN = new ArrayList<String>();
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
			@Override
			protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				passenN.add("9");passenN.add("6");passenN.add("5");passenN.add("4");
				passenN.add("3");passenN.add("2");passenN.add("1");passenN.add("0");
				passenN.add("208");
				super.setup(context);
			}
			@Override
			protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				String valueStr=value.toString();
				String [] values=valueStr.split("	");
				context.write(new Text(values[7]), new Text(new String(JAES.parseByte2HexStr(encryptV))));	
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
				int sum=0;
				Text newKey=new Text();
				for(Text value:values){
					byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(value.toString()), password);
					String v = new String(decryptV).trim();
					int j=Integer.parseInt(v.toString().substring(v.toString().indexOf("_")+1,v.toString().indexOf("#")));
					int r=Integer.parseInt(v.toString().substring(v.toString().indexOf("#")+1,v.toString().length()));
		            if(j==r){
							sum+=Integer.parseInt(v.toString().substring(0,v.toString().indexOf("_")));
							byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
							newKey=new Text(new String(decryptK).trim().replace("\"", ""));
		            }
				}
				if(sum!=0){
				context.write(newKey, new Text(String.valueOf(sum)));}
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
				String valueStr = String.valueOf(count);
				byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String s =new String(decryptK).trim().replace("\"", "");
				if(! S_passenN.contains(s)){
					S_passenN.add(s);
				}
				String keyStr=new String(decryptK).trim().replace("\"", "");
				int r=(keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				byte[] encryptK=JAES.encrypt(keyStr, password);
				for(int i=0;i<numReduceTasks;i++){
					byte[] encryptV=JAES.encrypt(valueStr+"_"+i+"#"+r, password);
					context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))),new Text(new String(JAES.parseByte2HexStr(encryptV))));
				}
			}
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				 for (String entry : passenN){
					if(! S_passenN.contains(entry)){
						for(int i=0;i<numReduceTasks;i++){
							byte[] encryptK=JAES.encrypt(entry, password);
							byte[] encryptV=JAES.encrypt("0_"+i+"#88", password);
							context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
							}
					}
				}
				super.cleanup(context);
				
			}
		}
		static class MyPartitioner extends HashPartitioner<Text,Text>{
			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(value.toString()), password);
				String vuleStr=new String(decryptV).trim().replace("\"", "");
				int r = Integer.parseInt(vuleStr.substring(vuleStr.indexOf("_")+1,vuleStr.indexOf("#")));
				return r;
			}
		}
		   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		    	long startTime=System.currentTimeMillis();
		    	//获取配置对象信息
		    	Configuration conf = new Configuration();
//		    	Job job =Job.getInstance(conf,"mapreduce");
		    	Job job =new Job();
		    	//设置job的运行主类
		    	job.setJarByClass(Full_Shuffle.class);
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
		    	job.setNumReduceTasks(7);//reduce 数量设定
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
