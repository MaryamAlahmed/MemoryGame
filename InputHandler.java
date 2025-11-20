package memorygame;

public class InputHandler {

    private final CommunicationManager comm;

    public InputHandler(CommunicationManager comm) {
        this.comm = comm;
    }

    /** Block until Arduino sends one of four buttons (0..3). */
    public int readOneOfFour() {
        return comm.readButtonIndex();
    }
}