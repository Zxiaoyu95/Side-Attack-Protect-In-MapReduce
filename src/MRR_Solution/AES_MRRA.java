package MRR_Solution;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import MRR_Solution.JAES;

public class AES_MRRA {
	static int numReduceTasks =7;
	static String password="xidian320";
	static byte[] encryptV=JAES.encrypt("1", password);
	static long start_job1_map;
	static long end_job1_map;
	static long start_job1_combiner;
	static long end_job1_combiner;
	static long start_job1_reduce;
	static long end_job1_reduce;
	static long start_job2_map;
	static long end_job2_map;
	static long start_job2_reduce;
	static long end_job2_reduce;
/*job1*/
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		@Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			start_job1_map=System.currentTimeMillis();
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
//			byte[] encryptK=JAES.encrypt(values[5], password);
//			byte[] encryptV=JAES.encrypt("1", password);
//			context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
			context.write(new Text(values[35]), new Text(new String(JAES.parseByte2HexStr(encryptV))));
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		end_job1_map=System.currentTimeMillis();
		super.cleanup(context);
		}
	}
	public static class ShuffleReduce extends Reducer<Text,Text,Text,Text>{
		@Override
		protected void setup(Reducer<Text,Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			start_job1_reduce=System.currentTimeMillis();
			super.setup(context);
		}
		@Override
		protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String s=new String(decryptV).trim();
				count+=Integer.parseInt(s);
			}
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
			context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));	
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			end_job1_reduce=System.currentTimeMillis();
			super.cleanup(context);
		}
	}
	static class MyCombiner extends Reducer<Text,Text,Text,Text>{
		@Override
		protected void setup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
			start_job1_combiner=System.currentTimeMillis();
		}
		@Override
		protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				count+=1;
			}
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
			context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));
		}
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			end_job1_combiner=System.currentTimeMillis();
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
				start_job2_map=System.currentTimeMillis();
				super.setup(context);
			}
			@Override
			protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				String[] split=value.toString().split("	");
				byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(split[0]), password);	
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(split[1]), password);	
				String keyStr=new String(decryptK).trim();
				String valueStr=new String(decryptV).trim();
				int r=(keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				byte[] encryptK=JAES.encrypt(keyStr, password);
				for(int i=0;i<numReduceTasks;i++){
					byte[] encryptV=JAES.encrypt(valueStr+"_"+i+"#"+r, password);
				context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))),new Text(new String(JAES.parseByte2HexStr(encryptV))));
				}
			}
			@Override
			protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			end_job2_map=System.currentTimeMillis();
			super.cleanup(context);
			}
		}
		public static class ShuffleReduce2 extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text,Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				start_job2_reduce=System.currentTimeMillis();
				super.setup(context);
			}
			@Override
			protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
				int count=0;
				Text newKey=new Text();
				for(Text value:values){
					byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(value.toString()), password);
					String v = new String(decryptV).trim();
					int j=Integer.parseInt(v.toString().substring(v.toString().indexOf("_")+1,v.toString().indexOf("#")));
					int r=Integer.parseInt(v.toString().substring(v.toString().indexOf("#")+1,v.toString().length()));
		            if(j==r){
							count+=Integer.parseInt(v.toString().substring(0,v.toString().indexOf("_")));
							byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
							newKey=new Text(new String(decryptK).trim().replace("\"", ""));
		            }
				}
				if(count!=0){
				context.write(newKey, new Text(String.valueOf(count)));}
			}
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				end_job2_reduce=System.currentTimeMillis();
				super.cleanup(context);
			}
		}
		static class MyPartitioner2 extends HashPartitioner<Text,Text>{
			
			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(value.toString()), password);
				String vuleStr=new String(decryptV).trim();
				int r = Integer.parseInt(vuleStr.substring(vuleStr.indexOf("_")+1,vuleStr.indexOf("#")));
				return r;
			}
		}
		
    public static void main(String[] args) throws IOException,URISyntaxException, ClassNotFoundException, InterruptedException{
    	//获取配置对象信息
    	Configuration conf = new Configuration();
//job1设置 	
//    	Job job1 =Job.getInstance(conf,"job1"); 
    	Job job1 =new Job();
    	//设置job的运行主类
    	job1.setJarByClass(AES_MRRA.class);
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
        job2.setJarByClass(AES_MRRA.class);
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
       
// //设置作业之间关系，job2输入job1输出
//        ctrlJob2.addDependingJob(ctrlJob1);
// //设置主控制器，控制job1和job2两个作业
//        JobControl jobCtrl= new JobControl("myCtrl");
//        jobCtrl.addJob(ctrlJob1);
//        jobCtrl.addJob(ctrlJob2);
//    	
//        //在线程中启动
//        Thread thread = new Thread(jobCtrl);
//        thread.start();
//        while (true) {
//            if (jobCtrl.allFinished()) {
//                System.out.println(jobCtrl.getSuccessfulJobList());
//                jobCtrl.stop();
//                break;
//            }
//        }
        File file=new File("Log1");
   		if (!file.exists()) {
   			file.createNewFile();// 创建目标文件
           }
   		FileWriter fpout = new FileWriter(file,true);
   		fpout.write("job1_map： "+(AES_MRRA.end_job1_map - AES_MRRA.start_job1_map)+"ms"+"	"
   		+"job1_combiner： "+(AES_MRRA.end_job1_combiner - AES_MRRA.start_job1_combiner)+"ms"+"	"
   		+"job1_reduce： "+(AES_MRRA.end_job1_reduce - AES_MRRA.start_job1_reduce)+"ms"+"	"
   		+"job2_map： "+(AES_MRRA.end_job2_map - AES_MRRA.start_job2_map)+"ms"+"	"
   		+"job2_reduce： "+(AES_MRRA.end_job2_reduce - AES_MRRA.start_job2_reduce)+"ms");
   		fpout.close();
        if (job1.waitForCompletion(true)) {
                System.exit(job2.waitForCompletion(true) ? 0 : 1);
        	       }
       
    }
}

	
