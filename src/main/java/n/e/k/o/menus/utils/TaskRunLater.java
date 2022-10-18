package n.e.k.o.menus.utils;

public class TaskRunLater {

    private final Runnable task;
    private final int ticks;

    public int currentTick = 0;
    public boolean isFinished = false;

    public TaskRunLater(Runnable task, int ticks) {
        this.task = task;
        this.ticks = ticks;
    }

    public void tick() {
        if (isFinished)
            return;
        if (currentTick == ticks) {
            task.run();
            isFinished = true;
        } else {
            currentTick++;
        }
    }

    /**
     * @return a cloned task, starting from scratch (it does not continue where the original task left off)
     */
    public TaskRunLater clone() {
        return new TaskRunLater(task, ticks);
    }

}
