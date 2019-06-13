package shuffle_balabce_solution;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class Offline_Phase{
	 static String password="xidian320";
	 static byte[] encryptC=JAES.encrypt("1", password);
	 static int numReduceTasks =10;
	 static String staticStr = "";
	 static int MapSum = 0;
	 static Map<String,Integer> map=new HashMap<String, Integer>();
	 static int CountAll = 0;
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
			context.write(new Text(valuesV), new Text(new String(JAES.parseByte2HexStr(encryptC))));
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
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
			
			for(Text i:values){
				MapSum++;
				staticStr = String.valueOf(MapSum);			
				context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptC))));
			    }
			MapSum=0;
			byte[] Key=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String KeyStr=new String(Key).trim();
			map.put(KeyStr,Integer.valueOf(staticStr));
			}
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
            for (Map.Entry<String, Integer> entry : map.entrySet()){
            	CountAll = CountAll + entry.getValue();
            }
            for (Map.Entry<String, Integer> pram : map.entrySet()){
            	float f = ((float)pram.getValue()/CountAll);
            	String fStr = String.valueOf(f);
            	//context.write(new Text(pram.getKey()), new Text(fStr+"_"+String.valueOf(pram.getValue())));
            }
            CountAll = 0;
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
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//获取配置对象信息
    	Configuration conf = new Configuration();
    	Job job =new Job();
    	//设置job的运行主类
    	job.setJarByClass(Offline_Phase.class);
    	//对map阶段进行设置
    	job.setMapperClass(MyMapper.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(Text.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	//对reduce阶段设置、
    	job.setReducerClass(ShuffleReduce.class);
    	job.setPartitionerClass(MyPartitioner.class);
    	job.setOutputKeyClass(Text.class);
    	job.setNumReduceTasks(10);//reduce 数量设定
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

	
