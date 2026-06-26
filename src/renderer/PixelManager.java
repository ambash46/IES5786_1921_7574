package renderer;

/**
 * Manages pixel distribution across threads and prints a progress line
 * after every completed row.
 *
 * @author Dan Zilberstein
 */
class PixelManager {

    /**
     * Immutable pixel coordinate pair.
     *
     * @param col pixel column number
     * @param row pixel row number
     */
    record Pixel(int col, int row) {}

    private final int  maxRows;
    private final int  maxCols;

    private volatile int  cRow   = 0;
    private volatile int  cCol   = -1;
    private volatile long pixels = 0L;

    private final Object mutexNext   = new Object();
    private final Object mutexPixels = new Object();

    /**
     * @param maxRows the amount of pixel rows
     * @param maxCols the amount of pixel columns
     */
    PixelManager(int maxRows, int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
    }

    /**
     * Thread-safe allocation of the next pixel to process.
     *
     * @return the next {@link Pixel}, or {@code null} when all pixels are done
     */
    Pixel nextPixel() {
        synchronized (mutexNext) {
            if (cRow == maxRows) return null;
            ++cCol;
            if (cCol < maxCols) return new Pixel(cRow, cCol);
            cCol = 0;
            ++cRow;
            if (cRow < maxRows) return new Pixel(cRow, cCol);
        }
        return null;
    }

    /** Notify the manager that one pixel has been processed; prints after each completed row. */
    void pixelDone() {
        synchronized (mutexPixels) {
            ++pixels;
            if (pixels % maxCols == 0)
                System.out.printf("Row %d / %d%n", pixels / maxCols, maxRows);
        }
    }
}
