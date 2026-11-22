package q1_atm_simulation;

import java.io.IOException;

/**
 * Checked exception representing card reader hardware/file system failures,
 * such as a missing hardware profile or unreadable smart-chip driver file.
 */
public class CardReaderException extends IOException {
    private final String readerConfigFile;

    public CardReaderException(String message, String readerConfigFile) {
        super(message);
        this.readerConfigFile = readerConfigFile;
    }

    public CardReaderException(String message, String readerConfigFile, Throwable cause) {
        super(message, cause);
        this.readerConfigFile = readerConfigFile;
    }

    public String getReaderConfigFile() {
        return readerConfigFile;
    }
}
