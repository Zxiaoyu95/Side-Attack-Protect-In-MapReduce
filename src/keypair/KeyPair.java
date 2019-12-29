package keypair;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class KeyPair implements WritableComparable<KeyPair> {
	private int pickuptime;
	private int passenN;
	public int getPickuptime(){
		return pickuptime;
	}
	public void setPickuptime(int pickuptime){
		this.pickuptime=pickuptime;
	}
	public int getPassnN(){
		return passenN;
	}
	public void setPassenN(int passenN){
		this.passenN=passenN;
	}
	public void readFields(DataInput in) throws IOException {
		this.pickuptime=in.readInt();
		this.passenN=in.readInt();	
	}
	public void write(DataOutput out) throws IOException {
		out.writeInt(pickuptime);
		out.writeInt(passenN);
		
	}
	public int compareTo(KeyPair o) {
		int result =Integer.compare(pickuptime, o.getPickuptime());
		if(result!=0){
			return result;
		}
		return Integer.compare(passenN, o.passenN);
	}
	@Override
	public String toString(){
		return pickuptime+"\t"+passenN;
	}
	@Override
	public int hashCode() {
	
		return new Integer(pickuptime+passenN).hashCode();
	}
}
