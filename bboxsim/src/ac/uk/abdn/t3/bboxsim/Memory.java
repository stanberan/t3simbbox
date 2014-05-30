package ac.uk.abdn.t3.bboxsim;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class Memory {
	static LocationListener gpsListener;
	static LocationManager locationManager;
	static JSONObject jsonBody=new JSONObject();
	static String deviceid="bboxSimulatorV1";
	static boolean sending=false;
	
	float speed;
	double latitude;
	double longitude;
	double altitude;
	double course;
	long timeTaken;
	static long LOOP_TIME=30000;
	static long previousSend;
	
	static float temp=-99;
	static int ax_min;
	static int ax_max;
	static int ay_min;
	static int ay_max;
	static int az_min;
	static int az_max;


	static long ax_average;
	static long az_average;
	static long ay_average;

	static long acc_size;

	static long x_total;
	static long y_total;
	static long z_total;
	
	
	
	
	public static void getACCReadLoop(float xf,float yf, float zf,float range){
		
		int x=(int)xf;
		int y=(int)yf;
		int z=(int)zf;
		
		
		
		acc_size++;
		 x_total+=x;
		 y_total+=y;
		 z_total+=z;
		 
		 if(x< ax_min){
		   ax_min=x;
		 }
		 if(x>ax_max){
		   ax_max=x;
		 }
		 
		 if(y<ay_min){
		   ay_min=y;
		 }
		 
		 if(y>ay_max){
		   ay_max=y;
		 }
		 if(z<az_min){
		   az_min=z;
		 }
		 if(z>az_max){
		   az_max=z;
		 }

		
		MainActivity.x.setText("X: \t"+x+"\t"+ax_min+"\t"+ax_max+"\t"+x_total+"\t"+(double)(x_total/acc_size)+"\t"+temp);
		MainActivity.y.setText("Y: \t"+y+"\t"+ay_min+"\t"+ay_max+"\t"+y_total+"\t"+(double)(y_total/acc_size)+"\t"+range);
		MainActivity.z.setText("Z: \t"+z+"\t"+az_min+"\t"+az_max+"\t"+z_total+"\t"+(double)(z_total/acc_size)+"\t"+range);
		
	}
	
	
	public static String getJsonData(){
		Log.e("getData", "Waiting for previous sending to finish");
		while(sending){}
		Log.e("getData", "Waiting for previous sending to finish");
		
		
		 ax_average=x_total/acc_size;
		 az_average=z_total/acc_size;
		 ay_average=y_total/acc_size;
		 
		 try{
		 jsonBody.put("ax_min", ax_min);
		 jsonBody.put("ax_max", ax_max);
		 jsonBody.put("ax_avg", ax_average);
		 jsonBody.put("ay_min", ay_min);
		 jsonBody.put("ay_max", ay_max);
		 jsonBody.put("ay_avg", ay_average);
		 jsonBody.put("az_min", az_min);
		 jsonBody.put("az_max", az_max);
		 jsonBody.put("az_avg", az_average);
		
		 
		Location l= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		 jsonBody.put("lt", l.getLatitude());
		 jsonBody.put("ln", l.getLongitude());
		 jsonBody.put("al", l.getAltitude());
		 jsonBody.put("cs", l.getBearing());
		 jsonBody.put("sp", l.getSpeed());
		 jsonBody.put("temp", temp);
		 long time=l.getTime();
		 Date d=new Date(time);
		 String formattedDate = new SimpleDateFormat("dd-MM-yy hh:mm:ss").format(d);
		 jsonBody.put("tm",formattedDate);
		 Date sent=new Date();
		 String formattedsent = new SimpleDateFormat("dd-MM-yy hh:mm:ss").format(d);
		 jsonBody.put("time",formattedsent);
		 jsonBody.put("device_id", deviceid);
		 return jsonBody.toString();
		 }
		 catch(Exception e){
			 e.printStackTrace();	
				return "Something went wrong";
		 }
		 finally{
		 
			 //clear ax and start again
			 ax_min=0;
			   ax_max=0;
			  
			     ay_min=0;
			   ay_max=0;

			     az_min=0;
			   az_max=0;
			   acc_size=0;
			   x_total=0;
			   y_total=0;
			   z_total=0; 
			 
			 
		 }
		
		

		
	}

	 
	
}
