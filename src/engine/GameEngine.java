package engine;

public class GameEngine implements Runnable {

    //Static Data
    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 30;
    public static final float FPS_UPDATE = 1.0f; //in seconds
    public static float CURRENT_FPS = 0;

    //Instance Data
    private final Window window;
    private final Thread gameLoopThread;
    private final Timer timer;
    private final IGameLogic gameLogic;
    private final MouseInput mouseInput;

    //Constructor
    public GameEngine(String windowTitle, int width, int height, boolean vSync, IGameLogic gameLogic) throws Exception {
        this.gameLoopThread = new Thread(this, "GAME_LOOP_THREAD"); //bind the thread to this class's run method
        this.window = new Window(windowTitle, width, height, vSync);
        this.mouseInput = new MouseInput();
        this.gameLogic = gameLogic;
        this.timer = new Timer();

    }

    //Methods
    public void start() {
        String os = System.getProperty("os.name");
        if (os.contains("Mac")) {
            System.setProperty("java.awt.headless", "true");
            this.gameLoopThread.run();
        }
        else this.gameLoopThread.start();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        this.window.init();
        this.timer.init();
        this.mouseInput.init(this.window);
        this.gameLogic.init(this.window);
    }

    protected void gameLoop() {

        //Loop Data
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;
        boolean running = true;
        float fpsUpdateCount = 0;

        //Loop
        while (running && !window.shouldClose()) {

            //Timekeeping
            elapsedTime = this.timer.getElapsedTime();
            accumulator += elapsedTime;
            fpsUpdateCount += elapsedTime;

            //Input
            this.input();

            //Update
            while(accumulator >= interval) {
                this.update(interval);
                accumulator -= interval;
            }

            //Render
            this.render();
            if (fpsUpdateCount > GameEngine.FPS_UPDATE) {
                GameEngine.CURRENT_FPS = 1 / elapsedTime;
                fpsUpdateCount -= GameEngine.FPS_UPDATE;
            }

            //Sync
            if (!window.isvSync()) this.sync(); //manual sync if no V-Sync
        }
    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = this.timer.getLastLoopTime() + loopSlot;
        while (this.timer.getTime() < endTime) {
            try { Thread.sleep(1); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    protected void cleanup() {
        this.gameLogic.cleanup();
    }

    //Input, Update, Render
    protected void input() { this.mouseInput.input(window); this.gameLogic.input(this.window); }
    protected void update(float interval) { this.gameLogic.update(interval, this.mouseInput); }
    protected void render() { this.gameLogic.render(this.window); this.window.update(); }
}
