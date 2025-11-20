package memorygame;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialPortHandler {

    private final SerialPort sp;
    private final String path;

    public SerialPortHandler(String path) {
        this.path = path;
        this.sp = new SerialPort(path);
        try {
            sp.openPort();
            // 9600 baud, 8 data bits, 1 stop bit, no parity
            sp.setParams(9600, 8, 1, 0);

            // Flush any garbage already in the buffer
            while (sp.getInputBufferBytesCount() > 0) {
                sp.readBytes();
            }

        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    // -------- read a full line of text (for debugging) --------
    public String readLine() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            try {
                byte[] buffer = sp.readBytes(1); // blocking
                if (buffer == null || buffer.length == 0) continue;

                char c = (char) buffer[0];

                if (c == '\n' || c == '\r') {
                    if (sb.length() == 0) {
                        // skip empty lines
                        continue;
                    } else {
                        break;  // return non-empty line
                    }
                }

                sb.append(c);
            } catch (SerialPortException e) {
                e.printStackTrace();
                break;
            }
        }
        return sb.toString().trim();
    }

    // -------- write a whole line --------
    public void printLine(String s) {
        byte[] bytes = s.getBytes();
        try {
            sp.writeBytes(bytes);
            sp.writeByte((byte) '\n');
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    // -------- write a single byte --------
    public void writeByte(byte b) throws SerialPortException {
        sp.writeBytes(new byte[]{ b });
    }

    // -------- read a single byte (blocking) --------
    public byte readByte() throws SerialPortException {
        byte[] buffer = sp.readBytes(1); // blocking
        if (buffer != null && buffer.length > 0) {
            return buffer[0];
        } else {
            throw new SerialPortException(path, "readByte", "No data received");
        }
    }

    // -------- how many bytes are waiting in the input buffer? --------
    public int available() throws SerialPortException {
        return sp.getInputBufferBytesCount();
    }

    // -------- close port --------
    public void close() {
        try {
            if (sp.isOpened()) {
                sp.closePort();
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }
}

