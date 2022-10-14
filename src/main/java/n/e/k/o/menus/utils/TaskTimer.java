package n.e.k.o.menus.utils;

public class TaskTimer {

    private final Runnable task;
    private final int delay;
    private final int ticks;
    private int state;

    public int currentDelay = 0;
    public int currentTick = 0;

    public TaskTimer(Runnable task, int delay, int ticks) {
        this.task = task;
        this.delay = delay;
        this.ticks = ticks;
        if (delay == 0 && ticks == 0)
            this.state = 0;
        else if (delay > 0 && ticks == 0)
            this.state = 1;
        else
            this.state = 3;
    }

    public void tick() {
        final int s = state;
        if (s == 0) {
            task.run();
        } else if (s == 1) {
            if (currentDelay == delay) {
                task.run();
            } else {
                currentDelay++;
            }
        } else if (s == 2) {
            if (currentTick == ticks) {
                task.run();
                currentTick = 0;
            } else {
                currentTick++;
            }
        } else if (s == 3) {
            if (currentDelay == delay) {
                task.run();
                state = 2;
            } else {
                currentDelay++;
            }
        }
    }

}
