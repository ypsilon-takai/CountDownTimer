package ypsilon.app.cdn;

import java.util.HashMap;

import android.util.Log;


public class Converter {

    public HashMap<Integer, Integer> numToResid;
    public HashMap<String, Integer> wordToResid;

	public Converter() {

    	wordToResid = new HashMap<String, Integer>();
    	numToResid = new HashMap<Integer, Integer>();

    	initSoundData();

	}

    private void initSoundData () {

    	wordToResid.put("start", R.raw.voice_start);
    	wordToResid.put("finished", R.raw.horn_02);

    	numToResid.put(0, R.raw.voice_000);
    	numToResid.put(1, R.raw.voice_001);
    	numToResid.put(2, R.raw.voice_002);
    	numToResid.put(3, R.raw.voice_003);
    	numToResid.put(4, R.raw.voice_004);
    	numToResid.put(5, R.raw.voice_005);
    	numToResid.put(6, R.raw.voice_006);
    	numToResid.put(7, R.raw.voice_007);
    	numToResid.put(8, R.raw.voice_008);
    	numToResid.put(9, R.raw.voice_009);
    	numToResid.put(10, R.raw.voice_010);
    	numToResid.put(11, R.raw.voice_011);
    	numToResid.put(12, R.raw.voice_012);
    	numToResid.put(13, R.raw.voice_013);
    	numToResid.put(14, R.raw.voice_014);
    	numToResid.put(15, R.raw.voice_015);
    	numToResid.put(16, R.raw.voice_016);
    	numToResid.put(17, R.raw.voice_017);
    	numToResid.put(18, R.raw.voice_018);
    	numToResid.put(19, R.raw.voice_019);
    	numToResid.put(20, R.raw.voice_020);
    	numToResid.put(25, R.raw.voice_025);
    	numToResid.put(30, R.raw.voice_030);
    	numToResid.put(35, R.raw.voice_035);
    	numToResid.put(40, R.raw.voice_040);
    	numToResid.put(45, R.raw.voice_045);
    	numToResid.put(50, R.raw.voice_050);
    	numToResid.put(55, R.raw.voice_055);
    	numToResid.put(60, R.raw.voice_01_00);
    	numToResid.put(90, R.raw.voice_01_30);
    	numToResid.put(120, R.raw.voice_02_00);
    	numToResid.put(150, R.raw.voice_02_30);
    	numToResid.put(180, R.raw.voice_03_00);
    	numToResid.put(210, R.raw.voice_03_30);
    	numToResid.put(240, R.raw.voice_04_00);
    	numToResid.put(270, R.raw.voice_04_30);
    	numToResid.put(300, R.raw.voice_05_00);
    	numToResid.put(330, R.raw.voice_05_30);
    	numToResid.put(360, R.raw.voice_06_00);
    	numToResid.put(390, R.raw.voice_06_30);
    	numToResid.put(420, R.raw.voice_07_00);
    	numToResid.put(450, R.raw.voice_07_30);
    	numToResid.put(480, R.raw.voice_08_00);
    	numToResid.put(510, R.raw.voice_08_30);
    	numToResid.put(540, R.raw.voice_09_00);
    	numToResid.put(570, R.raw.voice_09_30);
    	numToResid.put(600, R.raw.voice_10_00);


    }

    public Integer getResId (Integer num) {
    	if (! numToResid.containsKey(num)) {
    		return -1;
    	} else {
    		return (Integer)numToResid.get(num);
    	}
    }

    public Integer getResId(String word) {
    	if (! wordToResid.containsKey(word)) {
    		return -1;
    	} else {
    		return (Integer)numToResid.get(word);
    	}
    }

    static public String formatTimeSec(int seconds) {
    	String output = "00:00";

//    	Log.d( "HLGT Debug", "seconds = " + seconds );

       	long minutes = seconds / 60;

    	seconds = seconds % 60;
    	minutes = minutes % 60;

    	String secondsD = String.valueOf(seconds);
    	String minutesD = String.valueOf(minutes);

    	if (seconds < 10)
    		secondsD = "0" + seconds;
    	if (minutes < 10)
    	    minutesD = "0" + minutes;

    	output = minutesD + ":" + secondsD ;
    	return output;
    }

}
