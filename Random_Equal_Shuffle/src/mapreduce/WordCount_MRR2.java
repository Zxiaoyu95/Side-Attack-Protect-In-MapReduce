package mapreduce;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

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

public class WordCount_MRR2 {
	static int numReduceTasks =7;
/*job1*/
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		@Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			StringTokenizer stringTokenizer = new StringTokenizer( valueStr);
			Text word = new Text();
			while (stringTokenizer.hasMoreTokens()) {
				String wordValue = stringTokenizer.nextToken();				
				word.set(wordValue);				
				context.write(word,  new Text("1"));
			}
			
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
				count+=Integer.parseInt(v.toString());
			}
			context.write(key, new Text(String.valueOf(count)));	
		}	
	}
	static class MyCombiner extends Reducer<Text,Text,Text,Text>{
		
		@Override
		protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				count+=Integer.parseInt(v.toString());
			}
			context.write(key, new Text(String.valueOf(count)));
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
				String valueStr=value.toString();
				String [] values=valueStr.split("	");
				int r=(values[0].hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				int s = (int)(Math.random()*numReduceTasks);
				context.write(new Text(values[0]),new Text("TURE_"+values[1]+"#"+r));
				if(s<(numReduceTasks/2)){
					int p = (int)(Math.random()*numReduceTasks);
					context.write(new Text(values[0]),new Text("FAKE_"+0+"#"+p));
				}
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
					count+=Integer.parseInt(v.toString().substring(v.toString().indexOf("_")+1,v.toString().indexOf("#")));
				}
				if(count!=0){
				context.write(key, new Text(String.valueOf(count)));
				}
			}
	
		}
		static class MyPartitioner2 extends HashPartitioner<Text,Text>{
			

			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				String vuleStr=value.toString();
				return Integer.parseInt(vuleStr.substring(vuleStr.indexOf("#")+1,vuleStr.length()));
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
    	job1.setJarByClass(WordCount_MRR2.class);
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
        job2.setJarByClass(WordCount_MRR2.class);
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
        if (job1.waitForCompletion(true)) {
            System.exit(job2.waitForCompletion(true) ? 0 : 1);
 	       }
    	long endTime=System.currentTimeMillis();
    	System.out.println("运行时间："+(endTime-startTime)+"ms");
    }
}

	
