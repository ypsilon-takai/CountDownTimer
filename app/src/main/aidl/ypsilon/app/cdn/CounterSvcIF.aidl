package ypsilon.app.cdn;

interface CounterSvcIF {
	boolean setTime (int time, int pretime);
	void start (int time, int pretime);
	void pause ();
	void restart ();
	void stop ();
	void end ();
	Bundle getState();
}
