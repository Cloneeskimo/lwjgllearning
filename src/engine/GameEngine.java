package engine;

public class GameEngine implements Runnable {

    //Data
    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;
    private final Window window;
    private final Thread gameLoopThread;
    private final Timer timer;
    private final IGameLogic gameLogic;
    private final MouseInput mouseInput;

    //Constructor
    public GameEngine(String windowTitle, int width, int height, boolean vSync, IGameLogic gameLogic) throws Exception {
        this.gameLoopThread = new Thread(this, "GAME_LOOP_THREAD"); //bind the thread to this class's run method
        this.window = new Window(windowTitle, width, height, vSync);
        this.gameLogic = gameLogic;
        this.timer = new Timer();
        this.mouseInput = new MouseInput();
    }

    //Methods
    public void start() {
        String os = System.getProperty("os.name");
        if (os.contains("Mac")) this.gameLoopThread.run();
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
        this.gameLogic.init(this.window);
        this.mouseInput.init(this.window);
    }

    protected void gameLoop() {

        //Loop Data
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;
        boolean running = true;

        //Loop
        while (running && !window.shouldClose()) {

            //Timekeeping
            elapsedTime = this.timer.getElapsedTime();
            accumulator += elapsedTime;

            //Input
            this.input();

            //Update
            while(accumulator >= interval) {
                this.update(interval);
                accumulator -= interval;
            }

            //Render
            this.render();

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
    protected void input() { this.gameLogic.input(this.window, this.mouseInput); }
    protected void update(float interval) { this.gameLogic.update(interval, this.mouseInput); }
    protected void render() { this.gameLogic.render(this.window); this.window.update(); }
}
