package ac.uk.abdn.t3.bboxsim;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

 public class MainActivity extends Activity {
static TextView output;
static TextView x;
static TextView y;
static TextView z;
private final String SERVER_URL="http://t3.abdn.ac.uk:8080/bboxserver/upload";

Button start;
LocationManager locationManager;
SensorManager sensorManager;

private SensorEventListener sensorListener;

private static String PROVIDER=LocationManager.GPS_PROVIDER ;
String deviceid="bboxSimulatorV1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Memory.previousSend=System.currentTimeMillis();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		output=(TextView)findViewById(R.id.output);
		x=(TextView)findViewById(R.id.x);
		y=(TextView)findViewById(R.id.y);
		z=(TextView)findViewById(R.id.z);
		start=(Button)findViewById(R.id.button_sim);
		Memory.locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager=Memory.locationManager;
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		
		
		sensorListener=new SensorEventListener() {
		    @Override
		    public void onAccuracyChanged(Sensor arg0, int arg1) {
		    }

		    @Override
		    public void onSensorChanged(SensorEvent event) {
		        Sensor sensor = event.sensor;
		        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		        	
		        float i=sensor.getMaximumRange();
		           Memory.getACCReadLoop(event.values[0], event.values[1], event.values[2],i);
		           if(System.currentTimeMillis()-Memory.previousSend > Memory.LOOP_TIME &&!Memory.sending){
		        	   Log.e("LOG","GETTING JSON DATA AFTER 20 seconds");
		        	  //sendData
		        	 
		        	   
		        	 
		        	   try {
						Memory.jsonBody.put("batt", getBatteryLevel());
						 String jsonData=Memory.getJsonData();
						  output.setText(jsonData);
						  Memory.sending=true;
			        	  new SendTask(jsonData,SERVER_URL).execute();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	 
		        //
		        	   
		        	   
		        	
		           }
		      
		        }
		        else if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
		           Memory.temp=event.values[0];
		           Log.e("TEMP", ""+event.values[0]);
		        	
		        }
		        
		    }
		};
		
		Memory.gpsListener=new LocationListener() { public void onLocationChanged(Location location) {
		    // ignore...for now
		}
		public void onProviderDisabled(String provider) { // required for interface, not used
		}
		public void onProviderEnabled(String provider) { // required for interface, not used
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {
		    // required for interface, not used
		}};
		
		start.setOnClickListener(new OnClickListener(){
			@SuppressLint("InlinedApi")
			public void onClick(View v){
				
			//register event
				
				//start getting accelerometer data
				Log.e("LOG", "Activating GPS signals");
				locationManager.requestLocationUpdates(PROVIDER, 100, 10.0f, Memory.gpsListener);
				sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
				sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
			
				
				
				
			}
		});
	}
		
		public void onPause(){
			super.onPause();
			locationManager.removeUpdates(Memory.gpsListener);
			sensorManager.unregisterListener(sensorListener);
		}
		public void onResume(){
			super.onResume();
			
		}
	

		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public float getBatteryLevel(){
	   Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	    // Error checking that probably isn't needed but I added just in case.
	    if(level == -1 || scale == -1) {
	        return 50.0f;
	    }

	    return ((float)level / (float)scale) * 100.0f; 
	}
	
	
	public class SendTask extends AsyncTask<String, String, String>{

	    private String body;
	    private String url;
	

	    /**
	     * Creates a new instance of GetTask with the specified URL and callback.
	     * 
	     * @param restUrl The URL for the REST API.
	     * @param callback The callback to be invoked when the HTTP request
	     *            completes.
	     * 
	     */
	    public SendTask(String json,String url){
	     body=json;
	     this.url=url;
	    }

	    @Override
	    protected String doInBackground(String... params) {
	
	       
	      try{
	    	  HttpPost httpPost = new HttpPost(url);
	          httpPost.setEntity(new StringEntity(body));
	       
	          httpPost.setHeader("Content-type", "application/json");
	         HttpResponse responseHttp= new DefaultHttpClient().execute(httpPost);
	    	
	    	return EntityUtils.toString(responseHttp.getEntity());
	    
	      }
	      catch(Exception e){
	    	  e.printStackTrace();
	    	  return "exception:"+e.getMessage();
	      }
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	Memory.sending=false;
	    	output.setText(result);
	    	  Memory.previousSend=System.currentTimeMillis();
	    	
	}
	}

}
