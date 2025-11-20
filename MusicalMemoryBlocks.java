package memorygame;

public class MusicalMemoryBlocks {

    public static void main(String[] args) {

        // You can pass COM port as command-line arg, else default:
        String port = (args.length > 0) ? args[0] : "COM6";

        CommunicationManager comm = new CommunicationManager(port);
        FeedbackManager feedback = new FeedbackManager(comm);
        SequenceManager sequence = new SequenceManager();
        InputHandler input = new InputHandler(comm);

        int round = 1;

        System.out.println("Starting Musical Memory Blocks on port " + port);

        while (true) {
            System.out.println("\n=== Round " + round + " ===");

            // 1) Extend sequence by ONE random color
            sequence.extendSequence();
            System.out.println("Sequence length now: " + sequence.getLength());

            // 2) Play sequence to user (simple blocking timing)
            sequence.playSequenceBlocking(feedback, 600, 250);  // 600ms on, 250ms off

            // 3) IMPORTANT: clear any leftover button presses from previous round
            comm.clearInputBuffer();

            // 4) Player repeats sequence
            boolean failure = false;
            for (int i = 0; i < sequence.getLength(); i++) {
                int expected = sequence.getAt(i);
                int pressed = input.readOneOfFour();   // waits for Arduino button

                System.out.println("Pressed: " + pressed + "  Expected: " + expected);

                feedback.flashColor(pressed);          // flash what they pressed

                if (pressed != expected) {
                    feedback.errorBeep();
                    System.out.println("You pressed " + pressed +
                            " but expected " + expected + ". Game over!");
                    failure = true;
                    break;
                }
            }

            if (failure) {
                break;
            }

            // 5) Round success
            feedback.okBeep();
            System.out.println("Correct! Moving to next round.");
            round++;

            try {
                Thread.sleep(1000); // small pause between rounds
            } catch (InterruptedException ignored) {}
        }

        comm.close();
        System.out.println("Game finished.");
    }
}
