package ypsilon.app.cdn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.PreferenceChangeEvent;

import static java.util.Arrays.asList;

/**
 * Main window for the timer.
 * @author Ypsilon
 * @version 0.51
 */

public class Control extends Activity implements ServiceConnection{

	// Time display
	private TextView tvTimeView;

	private ViewFlipper vfSelector;

	// Many button layout
	private Button btStartStop;
    private Button bt00;
    private Button bt01;
    private Button bt02;
    private Button bt10;
    private Button bt11;
    private Button bt12;
    private List<Button> buttonList;

    private ToggleButton tgbImmediate;
    private ToggleButton tgbPrecall;

    // Preset button time values
    private short[] btTimesecList;



	// One button layout
    private Button btStartStopBig;

    // Dialog ids
    static final int DIALOG_SET_TIME = 0;

    /**
     * Set time value.
     * Not change during coutdown.
     * (Second)
      */
    private int setTimeVal;

    /**
     * Set time value in seconds for '3,2,1' call
     * (Second)
     */
    private int preTimeVal;

    /**
     * Flag for timer is running or not.
     */
    private boolean timerRunning;


    /**
     * Flick params.
     */
    // start pos holder
    private float startXPos;

    // flag : true while flicking
    private boolean flicked;

    /**
     * Bound to the timer service or not
     */
    private boolean bindToService;

    /**
     * Broadcast receiver for countdown service
     */
    private BroadcastReceiver bcReceiver = null;

    /**
     * I/F to countdown service
     */
    private CounterSvcIF counterService = null;


	/**
	 * Called when the activity is first created.
	 * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d( "HLGT Debug", "Control onCreate()");


        setTimeVal = 60;
        preTimeVal = 5;

        flicked = false;

        bindToService = false;

        // set volumecontrol to stream while running
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // set entire layout
        setContentView(R.layout.controler);

        // Setup timer display area
        tvTimeView = (TextView)findViewById(R.id.TimeView);

        // -- font
        Typeface dispfont = Typeface.createFromAsset(getAssets(), "fonts/Seg12Modern.ttf");
        tvTimeView.setTypeface(dispfont);

        // flipper
        vfSelector = (ViewFlipper)findViewById(R.id.VF_switcher);

		// buttons
        btStartStop = (Button)findViewById(R.id.Bt_Main);
        bt00 = (Button)findViewById(R.id.Bt_00);
        bt01 = (Button)findViewById(R.id.Bt_01);
        bt02 = (Button)findViewById(R.id.Bt_02);
        bt10 = (Button)findViewById(R.id.Bt_10);
        bt11 = (Button)findViewById(R.id.Bt_11);
        bt12 = (Button)findViewById(R.id.Bt_12);
        buttonList = asList(bt00, bt01, bt02, bt10, bt11, bt12);

        tgbImmediate = (ToggleButton)findViewById(R.id.TG_immediate);
        tgbPrecall = (ToggleButton)findViewById(R.id.TG_countdown);

        btStartStopBig = (Button)findViewById(R.id.Bt_Sub_Main);



        // Converter class provides format exchange functionality.
        tvTimeView.setText(Converter.formatTimeSec(setTimeVal));
        btStartStop.setText(getResources().getString(R.string.text_init));
		btStartStopBig.setText(getResources().getString(R.string.text_init));

		// ********
		// * Small start button

        // Start or stop countdown.
		btStartStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!flicked) {
					startOrStop();
				}
			}
		});
		// flick function
        btStartStop.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				flipTemplate(event);
				return false;
			}
		});
        btStartStop.setEnabled(false);

		// ********
        // * Flip
        //
        // Flip button window.
        vfSelector.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				flipTemplate(event);
				return true;
			}
		});

		// ********
        // * Big start button
        //
        // Big button NOT act with short click.
        btStartStopBig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do nothing
            }
        });
        // Big button act with long click.
        btStartStopBig.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                startOrStop();
                return true;
            }
        });
        // Flip button window when user wipe on big button.
        btStartStopBig.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                flipTemplate(event);
                return false;
            }
        });
        btStartStopBig.setEnabled(false);


        // restore state
        if (savedInstanceState != null) {
            // restore button value
            short[] strdBtValues = savedInstanceState.getShortArray("buttonValue");
            if (strdBtValues != null) {
                btTimesecList = strdBtValues;
            } else {
                btTimesecList = new short[]{600, 300, 180, 120, 60, 30};
            }
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            btTimesecList = new short[6];
            btTimesecList[0] = (short)(prefs.getInt("bt0", 600));
            btTimesecList[1] = (short)(prefs.getInt("bt1", 300));
            btTimesecList[2] = (short)(prefs.getInt("bt2", 180));
            btTimesecList[3] = (short)(prefs.getInt("bt3", 120));
            btTimesecList[4] = (short)(prefs.getInt("bt4", 60));
            btTimesecList[5] = (short)(prefs.getInt("bt5", 30));
        }

        // ********
        // * Number buttons
        //
        // Functions for number button clicked.
        for (ListIterator<Button> it = buttonList.listIterator(); it.hasNext();) {
            final int idx = it.nextIndex();
            Button bt = it.next();
            bt.setText(Converter.buttonTimeSec(btTimesecList[idx], this));
            bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!timerRunning) {
                        setTimeOnButtonPush(btTimesecList[idx]);
                    }
                }
            });
            bt.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showTimeInputDialog(idx);
                    return true;
                }
            });
        }

    }

    private void setStartButtonsColor (boolean running) {
    	if (running) {
    		btStartStop.setBackgroundResource(R.drawable.plastic_red_button);
    		btStartStopBig.setBackgroundResource(R.drawable.plastic_red_button);
    	} else {
    		btStartStop.setBackgroundResource(R.drawable.plastic_button);
    		btStartStopBig.setBackgroundResource(R.drawable.plastic_button);
    	}
    }

    private void setStartButtonsText (String s) {
    	btStartStop.setText(s);
    	btStartStopBig.setText(s);
    }

    private void setPresetButtonText (Button bt, String s) {
        bt.setText(s + getString(R.string.text_min));
    }
    @Override
    public void onStart () {
    	super.onStart();

		Log.d("HLGT Debug", "Control onStart()");

        timerRunning = false;

    	// **************
    	//  Broadcast receiver
    	if (bcReceiver == null) {
    		bcReceiver = new BroadcastReceiver() {

    			@Override
    			public void onReceive(Context context, Intent message) {

    				boolean csstate = message.getExtras().getBoolean("STATE");
    				if (csstate) {

//        				Log.d( "HLGT Debug", "Control onReceive()" + csstate + " " + time);

    					if (!timerRunning) {
    		        		timerRunning = true;
    		        		setStartButtonsColor(true);
    		        		setStartButtonsText(Converter.formatTimeSec(setTimeVal));
    					}

                        setTimeVal = message.getExtras().getInt("INIT", 0);
                        setStartButtonsText(Converter.formatTimeSec(setTimeVal));

    					int time = message.getExtras().getInt("TIME", 0);
    					tvTimeView.setText(Converter.formatTimeSec(time));

    				} else {
        				Log.d( "HLGT Debug", "Control onReceive()" + csstate);
    					// countdown finished
			    		timerRunning = false;
						resetDisp();
    				}
    			}
    		};

    		IntentFilter filter = new IntentFilter("YP_CDT_TIMECHANGE");
    		registerReceiver(bcReceiver, filter);

    		// connect or create timer service here
    		// service calls back when ready

    		
    		if (!bindToService ) {
    			Log.d( "HLGT Debug", "Binding to service.");
    			Intent intent = new Intent(this, CountService.class);
        		startService(intent);
        		bindService(intent, this, Context.BIND_AUTO_CREATE);
    		}
    	}

    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState) {
        savedInstanceState.putShortArray("buttonValue", btTimesecList);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // restore button value
        short[] strdBtValues = savedInstanceState.getShortArray("buttonValue");
        if (strdBtValues != null) {
            btTimesecList = strdBtValues;
        } else {
            btTimesecList = new short[]{600, 300, 180, 120, 60, 30};
        }

        for (ListIterator<Button> it = buttonList.listIterator(); it.hasNext();) {
            final int idx = it.nextIndex();
            Button bt = it.next();
            bt.setText(Converter.buttonTimeSec(btTimesecList[idx], this));
        }
    }

    @Override
    public void onStop () {

		Log.d( "HLGT Debug", "Control onStop()");

    	// disconnecttimer service here
    	if (bindToService) {
    		unbindService(this);
    		bindToService = false;
            counterService = null;

            unregisterReceiver(bcReceiver);
    		bcReceiver = null;
    	}

        // save button values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("bt0", btTimesecList[0]);
        edit.putInt("bt1", btTimesecList[1]);
        edit.putInt("bt2", btTimesecList[2]);
        edit.putInt("bt3", btTimesecList[3]);
        edit.putInt("bt4", btTimesecList[4]);
        edit.putInt("bt5", btTimesecList[5]);
        edit.commit();

    	super.onStop();
    }

    @Override
    public void onDestroy () {

		Log.d( "HLGT Debug", "Control onDestroy()");

    	Bundle sstate = getCounterServiceState();
		if (sstate != null) {
			if (!sstate.getBoolean("BUSY")) {
				try {

					Log.d( "HLGT Debug", "CountService Calling stopService()");

					counterService.end();
	    			Intent intent = new Intent(this, CountService.class);
					stopService(intent);
				} catch (Exception e) {
					// do nothing
				}
			}
		}

		super.onDestroy();
    }

    /**
     * Gereric function to setup number buttons.
     * @param timesec Setting time.
     */
    private void setTimeOnButtonPush (int timesec) {
		setTimeVal = timesec;
		setTimeDisp(setTimeVal);
        if (tgbImmediate.isChecked()) {
        	startOrStop();
        }

    }

    /**
     * Count down start/stop function.
     */
    public void startOrStop () {
    	if (timerRunning) {
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    		timerRunning = false;
    		resetDisp();

    		try {
    			counterService.stop();
    		} catch (Exception e) {
    			Log.d( "HLGT Debug", e.toString());
			}
    	} else {
    		try {
    			if (tgbPrecall.isChecked()) {
    				counterService.start(setTimeVal, preTimeVal);
    			} else {
    				counterService.start(setTimeVal, 0);
    			}

        		timerRunning = true;
        		setStartButtonsColor(true);
        		setStartButtonsText(Converter.formatTimeSec(setTimeVal));
        		
    		} catch (Exception e) {
    			tvTimeView.setText("ERROR!");
			}
    		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	}

    }

    /**
     * Set Time Dispplay.
     * @param time : time as seconds.
     */
    public void setTimeDisp (int time) {
    	String st = Converter.formatTimeSec(time);
		tvTimeView.setText(st);
    }

    /**
     * Reset display and button text.
     */
    private void resetDisp () {
		tvTimeView.setText(Converter.formatTimeSec(setTimeVal));
		setStartButtonsColor(false);
		setStartButtonsText(getResources().getString(R.string.text_start));
    }


    /**
     * Flip button pane with motion event
     * @param event : motion event
     */
    private void flipTemplate (MotionEvent event) {
    	float endXPos;
    	switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			flicked = false;
			startXPos = event.getX();
			break;
		case MotionEvent.ACTION_UP:
			endXPos = event.getX();
			if (startXPos - endXPos > 30) {
				flicked = true;
				vfSelector.showNext();
			} else if (startXPos - endXPos < -30){
				flicked = true;
				vfSelector.showPrevious();
			}
			break;
		}
    }

    //**********************
    // Service related funcs

    public void onServiceConnected(ComponentName name, IBinder service) {

		Log.d( "HLGT Debug", "Control onServiceConnected()");

    	counterService = CounterSvcIF.Stub.asInterface(service);
    	bindToService = true;

    	btStartStop.setEnabled(true);
    	btStartStopBig.setEnabled(true);

    	setStartButtonsText(getResources().getString(R.string.text_start));
        setStartButtonsColor(false);

    }

	public void onServiceDisconnected(ComponentName name) {

		Log.d( "HLGT Debug", "Control onServiceDisconnected()");

		bindToService = false;
		counterService = null;
	}

	private Bundle getCounterServiceState () {
		if (counterService != null) {
			try {
				return counterService.getState();
			} catch (RemoteException e) {
				return null;
			}
		} else {
			return null;
		}
	}

    // time input dialog
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
            case DIALOG_SET_TIME:
                // do the work to define the pause Dialog
                dialog = null;
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    protected void showTimeInputDialog (final int buttonIdx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        //レイアウトファイルからビューを取得
        final View dialog_view = inflater.inflate(R.layout.numberinput, null);

        NumberPicker numPicker = (NumberPicker)dialog_view.findViewById(R.id.numberPicker);
        numPicker.setMaxValue(10);
        numPicker.setMinValue(0);

        TextView buttonName = (TextView)dialog_view.findViewById(R.id.txButtunName);
        buttonName.setText(getString(R.string.dialog_button_name, buttonIdx + 1));

        //レイアウト、題名、OKボタンとキャンセルボタンをつけてダイアログ作成
        builder.setView(dialog_view)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
						NumberPicker numPicker = (NumberPicker)dialog_view.findViewById(R.id.numberPicker);
                        short timesec = (short)(numPicker.getValue() * 60);
                        if (timesec == 0) {
                            timesec = 30;
                        }
                        //if (cb30sec.isChecked() && numPicker.getValue() < 10) {
                        //    timesec += 30;
                        //}
                        btTimesecList[buttonIdx] = timesec;

                        buttonList.get(buttonIdx).setText(Converter.buttonTimeSec(timesec, getApplicationContext()));

                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    /*キャンセルされたときの処理*/
                    }
                });

        AlertDialog myDialog = builder.create();

        //ダイアログ画面外をタッチされても消えないようにする。
        myDialog.setCanceledOnTouchOutside(false);

        //ダイアログ表示
        myDialog.show();
    }

}