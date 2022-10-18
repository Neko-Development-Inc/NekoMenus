package n.e.k.o.menus.utils;

public class TaskTimer {

    private final Runnable task;
    private final int delay;
    private final int ticks;
    private final int finishAfterRounds;
    private int state;

    public int currentDelay = 0;
    public int currentTick = 0;
    public int currentRound = 0;
    public boolean checkFinished = false;
    public boolean isFinished = false;

    public TaskTimer(Runnable task, int delay, int ticks) {
        this(task, delay, ticks, -1);
    }

    public TaskTimer(Runnable task, int delay, int ticks, int finishAfterRounds) {
        this.task = task;
        this.delay = delay;
        this.ticks = ticks;
        this.finishAfterRounds = finishAfterRounds;
        this.checkFinished = finishAfterRounds != -1;
        if (delay == 0 && ticks == 0)
            this.state = 0;
        else if (delay > 0 && ticks == 0)
            this.state = 1;
        else
            this.state = 3;
    }

    public void tick() {
        if (isFinished)
            return;
        final int s = state;
        if (s == 0) {
            task.run();
            if (checkFinished && ++currentRound >= finishAfterRounds)
                isFinished = true;
        } else if (s == 1) {
            if (currentDelay == delay) {
                task.run();
                if (checkFinished && ++currentRound >= finishAfterRounds)
                    isFinished = true;
            } else {
                currentDelay++;
            }
        } else if (s == 2) {
            if (currentTick == ticks) {
                task.run();
                if (checkFinished && ++currentRound >= finishAfterRounds)
                    isFinished = true;
                currentTick = 0;
            } else {
                currentTick++;
            }
        } else if (s == 3) {
            if (currentDelay == delay) {
                task.run();
                if (checkFinished && ++currentRound >= finishAfterRounds)
                    isFinished = true;
                state = 2;
            } else {
                currentDelay++;
            }
        }
    }

    /**
     * @return a cloned task, starting from scratch (it does not continue where the original task left off)
     */
    public TaskTimer clone() {
        return new TaskTimer(task, delay, ticks);
    }

}
