package memorygame;

import jssc.SerialPortException;

public class CommunicationManager {

    private final SerialPortHandler sph;

    public CommunicationManager(String portName) {
        this.sph = new SerialPortHandler(portName);
    }

    /** Send a single command byte to Arduino (e.g. '0'..'3', 'O', 'W', 'X'). */
    public void sendCommand(char c) {
        try {
            System.out.println("PC -> Arduino: '" + c + "'");
            sph.writeByte((byte) c);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    /**
     * Block until Arduino sends a button index '0'..'3'.
     * Returns 0–3 on success, or -1 on error.
     */
    public int readButtonIndex() {
        while (true) {
            try {
                byte b = sph.readByte();
                System.out.println("Arduino -> PC: '" + (char)b + "' (" + b + ")");
                if (b >= '0' && b <= '3') {
                    return b - '0';
                }
                // ignore other bytes (debug text, etc.)
            } catch (SerialPortException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /** Read a full debug line from Arduino, not required by the game. */
    public String readLine() {
        return sph.readLine();
    }

    /** Clear any OLD button presses left in the serial buffer. */
    public void clearInputBuffer() {
        try {
            while (sph.available() > 0) {
                sph.readByte();  // discard
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        sph.close();
    }
}

