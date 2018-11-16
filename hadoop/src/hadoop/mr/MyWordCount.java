package hadoop.mr;
/**属性分布
 * age 0  0    *  MS 0 Married   * POB 0  U.S
 *     1  <13  *     1 Divorced  *     1  US Ter.
 *     2  <20  *     2 Widowed   *     2  Europe
 *     3  <30  *     3 Separated *     3  Asia
 *     4  <40  *     4 N.Married *     4  Americas
 *     5  <50  *                 *     5  Africa
 *     6  <65  *                 *     6  Oceania
 *     7  65+  *                 *
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class MyWordCount {
	
	public static class MyMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			//获取数据  每一行调用一次map
			String line= value.toString();
			//切分数据
			String [] words =line.split("	");
			List<String> Age = new ArrayList<>();
			List<String>POB = new ArrayList<>();
			List<String> MS= new ArrayList<>();
			Age.add(words[0]);
			POB.add(words[1]);
			MS.add(words[2]);
			//循环处理
			for(String word:MS){
				context.write(new Text(word), new IntWritable(1));
			}	
		}
		
	}
	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,Context context) 
				throws IOException, InterruptedException {
		int counter =0;
		for(IntWritable i:values){
			counter+=i.get();
		      }
		context.write(key, new IntWritable(counter));
		}
	}
	//驱动
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//获取配置对象信息
    	Configuration conf = new Configuration();
    	Job job =Job.getInstance(conf,"mywordcount");
    	//设置job的运行主类
    	job.setJarByClass(MyWordCount.class);
    	//对map阶段进行设置
    	job.setMapperClass(MyMapper.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(IntWritable.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	//对reduce阶段设置、
    	job.setReducerClass(MyReducer.class);
    	job.setOutputKeyClass(Text.class);
    	job.setNumReduceTasks(3);
    	job.setOutputValueClass(IntWritable.class);
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));
    	//提交job
    	int isok = job.waitForCompletion(true)? 0 : 1;
    	//退出
    	long endTime=System.currentTimeMillis();
    	System.out.println("运行时间："+(endTime-startTime)+"ms");
    	System.exit(isok);
    }

}
