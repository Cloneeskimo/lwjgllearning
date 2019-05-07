package engine;

public class Timer {

    //data
    private double lastLoopTime;

    //methods
    public void init() { lastLoopTime = getTime(); }
    public double getTime() { return System.nanoTime() / 1000_000_000.0; }
    public double getLastLoopTime() { return this.lastLoopTime; }

    public float getElapsedTime() {
        double time = this.getTime();
        float elapsedTime = (float)(time - this.lastLoopTime);
        this.lastLoopTime = time;
        return elapsedTime;
    }
}
