package memorygame;

public class FeedbackManager {

    private final CommunicationManager comm;

    public FeedbackManager(CommunicationManager comm) {
        this.comm = comm;
    }

    /** Show a color (0..3) on Arduino LEDs. */
    public void showColor(int idx) {
        if (idx < 0 || idx > 3) return;
        comm.sendCommand((char) ('0' + idx));
    }

    /** Turn all LEDs off. */
    public void clearLeds() {
        comm.sendCommand('X');
    }

    /** Short "correct" beep on Arduino. */
    public void okBeep() {
        comm.sendCommand('O');
        System.out.println("[OK] Correct sequence!");
    }

    /** Error beep + vibration on Arduino. */
    public void errorBeep() {
        comm.sendCommand('W');
        System.out.println("[ERR] Wrong button!");
    }

    /** Flash a color briefly (used when player presses a button). */
    public void flashColor(int idx) {
        showColor(idx);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}
        clearLeds();
    }
}
