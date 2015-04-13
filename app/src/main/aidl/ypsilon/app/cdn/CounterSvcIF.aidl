package ypsilon.app.cdn;

interface CounterSvcIF {
	boolean setTime (int time, int pretime);
	void start (int time, int pretime);
	void stop ();
	void end ();
	Bundle getState();
}
