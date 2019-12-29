package MRR_Solution;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.apache.hadoop.util.LineReader;

public class Partition {
	public static class MyRecordReader extends RecordReader<LongWritable, Text>{
        private long start;//分片开始位置
        private long end;//分片结束位置
        private long pos;
        private FSDataInputStream fin = null;
        //自定义自己的key与value
        private LongWritable key = null;
        private Text value = null;
        private LineReader reader = null;
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			fin.close();
			
		}

		@Override
		public LongWritable getCurrentKey() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return key;
		}

		@Override
		public Text getCurrentValue() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return value;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void initialize(InputSplit split,TaskAttemptContext context) throws IOException, InterruptedException {
			FileSplit fileSplit=(FileSplit)split;
			start = fileSplit.getStart();
			end = start + fileSplit.getLength();
            Path path = fileSplit.getPath();//获取输入分片的路径
            Configuration conf = context.getConfiguration();
            //获取文件系统路径
            FileSystem fs = path.getFileSystem(conf);
            fin = fs.open(path);
            reader = new LineReader(fin);
            pos = 0;		
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if(key == null){
                key = new LongWritable();
            }
            key.set(pos);//设置key
            if(value == null){
                value = new Text();
            }
            //并没有跨块，跨文件，而是一个文件作为不可分割的 
            if(reader.readLine(value)==0){//一次读取行的内容,并设置值
                return false;
            }
            if(pos<2){
            	pos++;
            }
            else{
            	pos=0;
            }
            return true;
		}
		}
		public static class MyInputFormat extends FileInputFormat<LongWritable, Text>{
		@Override
		public RecordReader<LongWritable,Text> createRecordReader(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			return new MyRecordReader();
		}
		@Override
		protected boolean isSplitable(JobContext context,Path filename){
			return false;//判断是否切片
		}
		@Override
			public List<InputSplit> getSplits(JobContext job) throws IOException {
				// 默认Math.max(minsize,Math.min(maxsize,blocksize))
				return super.getSplits(job);
			}
	}
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
			String [] values=valueStr.split("	");
			int r=(values[0].hashCode()&Integer.MAX_VALUE)%3;
			context.write(new Text(String.valueOf(key)), new Text("<"+values[0].replace("\"", "")+","+r+">"));
			
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
			for(Text text:values){
				if (text.toString().endsWith("*"))
				{
					
//					try {
//						((JobContext) context.getCurrentKey()).getPartitionerClass();
//					} catch (ClassNotFoundException e) {
//						e.printStackTrace();
//					}
				}
				else{
					context.write(key, new Text(text));
				}
				
			
			}		
		}
		
	}
	static class MyCombiner extends Reducer<Text,Text,Text,Text>{
		
		@Override
		protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {

			for(Text t:values){
				String valuestr=t.toString();
				String realKeyStr=valuestr.substring(valuestr.indexOf(",")+1, valuestr.indexOf(">"));
				int realKeyInt=Integer.parseInt(realKeyStr);
				if(Integer.parseInt(key.toString())!=realKeyInt){
					context.write(new Text(String.valueOf(realKeyInt)), new Text(t+"*"));
				}
				else{
					context.write(key, new Text(t));
				}
			
				}

			
		}
	}
	static class MyPartitioner extends HashPartitioner<Text,Text>{
		

		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			String keystr=key.toString();
			return Integer.parseInt(keystr);
		}
		
	}
    public static void main(String[] args) throws IOException,URISyntaxException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//获取配置对象信息
    	Configuration conf = new Configuration();
    	Job job =Job.getInstance(conf,"mr");
    	//设置job的运行主类
    	job.setJarByClass(Partition.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	job.setInputFormatClass(MyInputFormat.class);
    	//对map阶段进行设置
    	job.setMapperClass(MyMapper.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(Text.class);
    	//Combiner Partition
    	job.setCombinerClass(MyCombiner.class);
    	job.setPartitionerClass(MyPartitioner.class);
    	//对reduce阶段设置、
    	job.setReducerClass(ShuffleReduce.class);
    	job.setOutputKeyClass(Text.class);
    	job.setNumReduceTasks(3);//reduce 数量设定
    	job.setOutputValueClass(Text.class);
    	//提交job
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));
    	int isok = job.waitForCompletion(true)? 0 : 1;
    	//退出
    	long endTime=System.currentTimeMillis();
    	System.out.println("运行时间："+(endTime-startTime)+"ms");
    	System.exit(isok);
    }
}

	
