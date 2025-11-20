package memorygame;

import java.util.Random;

public class SequenceManager {

    private final int[] seq = new int[32];
    private int len = 0;
    private final Random rng = new Random(4219);

    public void reset() {
        len = 0;
    }

    // Add one random color (0–3) to the end of the sequence
    public void extendSequence() {
        if (len < seq.length) {
            seq[len++] = rng.nextInt(4);   // 0,1,2,3 in random order
        }
    }

    public int getLength() {
        return len;
    }

    public int getAt(int i) {
        return seq[i];
    }

    // Play the whole sequence, BLOCKING, very simple timing
    public void playSequenceBlocking(FeedbackManager fb, long onMs, long offMs) {

        System.out.println("Sequence length now: " + len);

        for (int i = 0; i < len; i++) {

            int color = seq[i];

            // Debug print so you can see in the console what should flash
            System.out.println("PC -> Arduino: '" + color + "'");

            fb.showColor(color);                   // tell Arduino which LED
            try { Thread.sleep(onMs); } catch (InterruptedException ignored) {}

            fb.clearLeds();                        // send 'X'
            try { Thread.sleep(offMs); } catch (InterruptedException ignored) {}
        }
    }
}