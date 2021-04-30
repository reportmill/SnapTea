package snaptea;
import snap.view.View;
import snap.view.WindowView;
import snap.viewx.DevPane;

import java.util.Arrays;

/**
 * A Thread subclass to run event queue runs.
 */
class TVEventThread extends Thread {

    // The runs array and start/end
    private static Runnable  _theRuns[] = new Runnable[100];

    // The start/end index of scheduled runs
    private static int  _runStart, _runEnd;

    /**
     * Gets a run from event queue and runs it.
     */
    public synchronized void run()
    {
        // Queue runs forever
        while (true) {

            // Get next run, if found, just run
            Runnable run = getNextEventQueueRun();
            if (run != null) {
                runEvent(run);
                if (TVEnv._appThread != this)
                    break;
            }

            // Otherwise, wait till new run added to queue
            else {
                try {
                    wait();
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Runs an event.
     */
    private void runEvent(Runnable aRun)
    {
        // Run event
        try {
            aRun.run();
        }

        // If exception is thrown, send to dev console
        catch (Exception e) {
            WindowView[] winViews = TVScreen.get().getWindows().toArray(new WindowView[0]);
            if (winViews.length < 1) {
                System.err.println("TVEventThread: Uncaught exception, no windows");
                e.printStackTrace();
                return;
            }

            // Get last window and show exception
            WindowView lastWin = winViews[winViews.length-1];
            View rootView = lastWin.getRootView();
            DevPane.showException(rootView, e);
        }
    }

    /**
     * Wake up called when event is added to empty queue.
     */
    public synchronized void wakeUp()
    {
        notify();
    }

    /**
     * Returns the next run from event queue.
     */
    private static synchronized Runnable getNextEventQueueRun()
    {
        // Get next run - if none, reset array start/end vars
        Runnable run = _runEnd > _runStart ? _theRuns[_runStart++] : null;
        if (run == null)
            _runStart = _runEnd = 0;
        return run;
    }

    /**
     * Adds given run to the event queue.
     */
    public static synchronized void runOnAppThread(Runnable aRun)
    {
        // Add to Runs array
        _theRuns[_runEnd++] = aRun;

        // If array was empty, wake it up
        if (_runEnd == 1)
            TVEnv._appThread.wakeUp();

        // Otherwise if at end of Runs array, increase length
        else if (_runEnd >= _theRuns.length) {

            // If beyond Max length, complain and return
            if (_theRuns.length > 500) {
                System.err.println("TVEnv.addToEventQueue: To many events in queue - something is broken");
                _runStart = _runEnd = 0;
                return;
            }

            // Double size of Runs array
            _theRuns = Arrays.copyOf(_theRuns, _theRuns.length*2);

            // Notify anyway, this probably shouldn't really happen
            System.out.println("TVEnv.addToEventQueue: Increasing runs array to len " + _theRuns.length*2);
        }
    }
}
