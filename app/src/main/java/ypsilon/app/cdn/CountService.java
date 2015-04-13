package ypsilon.app.cdn;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class CountService extends Service {

	private int remainTime;
	private int remainPreTime;

	private boolean counting;

	private Caller caller;
	private TickTick ticktick;

	private int refCount;

	public void onCreate () {
		remainTime = 0;
		counting = false;

		Log.d( "HLGT CS", "CountService create()");

		caller = new Caller(this);

		ticktick = new TickTick() {
			public void onTick () {
				countDown();
			}
		};
		
		refCount = 0;
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d( "HLGT CS", "CountService onStartCommand()");
		
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {

		Log.d( "HLGT CS", "CountService onBind()");

		refCount++;
		
		return csifImplement;
	}
	
	@Override
	public boolean onUnbind(Intent arg0) {
		Log.d( "HLGT CS", " onUnbind()");
		
		if (refCount > 0) {
			refCount--;
		}
		
		return false;
	}

	
	public CounterSvcIF.Stub csifImplement = new CounterSvcIF.Stub() {

		public boolean setTime(int time, int pretime) throws RemoteException {
			return CountService.this.setTime(time, pretime);
		}

		public void start(int time, int pretime) throws RemoteException {
			CountService.this.start(time, pretime);
		}

		public void stop() throws RemoteException {
			CountService.this.stop();
		}

		public void end() throws RemoteException {
			CountService.this.end();
		}

		public Bundle getState() throws RemoteException {
			return CountService.this.getState();
		}
	};


	// Control functions
	
	private boolean setTime(int time, int pretime) {
		Log.d( "HLGT CS", "CountService setTime()");

		if (!counting) {
			remainTime = time;
			remainPreTime = pretime;
			return true;
		} else {
			return false;
		}
	}


	private void start(int time, int pretime) {
		Log.d( "HLGT CS", "CountService start()");
		this.setTime(time, pretime);
		counting = true;
		ticktick.start();
	}

	private void stop () {

		Log.d( "HLGT CS", "CountService stop()");

		ticktick.cancel();

		remainPreTime = -1;
		remainTime = -1;

		counting = false;
	}

	private void end () {

		Log.d( "HLGT CS", "CountService end()");

		if (ticktick != null) {
			ticktick.cancel();
		}

		this.stopSelf();
	}

	private Bundle getState () {

		Log.d( "HLGT CS", "CountService getState()");

		Bundle res = new Bundle();

		res.putBoolean("BUSY", counting);

		if (remainPreTime>0){
			res.putInt("REMAIN", remainPreTime);
		} else {
			res.putInt("REMAIN", remainTime);
		}

		return res;
	}

	public void countDown () {
		Intent message = new Intent ("YP_CDT_TIMECHANGE");

    	if (remainPreTime > 0) {
//    		Log.d("CountService", "cd 01");
    		caller.say(remainPreTime);
    		// *** call controler
    		message.putExtra("STATE", counting);
    		message.putExtra("TIME", remainPreTime);
    		remainPreTime--;
    	} else if ( remainTime > 0){
//    		Log.d("CountService", "cd 02");
        	caller.say(remainTime);
    		// *** call controler
    		message.putExtra("STATE", counting);
    		message.putExtra("TIME", remainTime);
       		remainTime--;
    	} else if ( remainTime <= 0) {
    		Log.d("CountService", "cd finish");
			caller.say("finished");
			ticktick.cancel();
			counting = false;
    		// *** call controler
    		message.putExtra("STATE", counting);

    		if (refCount <= 0) {
    			Log.d( "HLGT CS", "CountService Calling stopSerf()");
    			this.stopSelf();
    		}
    	} else {
//    		Log.d("CountService", "cd error");
        	caller.say(remainTime);
    		// *** call controler
    		message.putExtra("STATE", counting);
    		message.putExtra("TIME", remainTime);
    		remainTime--;
    	}

//    	Log.d( "CountService", "CountService " + counting + " : rem=" + remainPreTime +  " rem=" + remainTime);

    	// send broadcast message
    	sendBroadcast(message);

    }


}
