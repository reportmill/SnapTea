package snaptea;
import snap.view.ClipboardData;

/**
 * A ClipboardData subclass to read JS File bytes asynchronously.
 */
class TVClipboardData extends ClipboardData {

    /**
     * Creates ClipboardData for given JS File and starts loading.
     */
    public TVClipboardData(File aFile)
    {
        super(aFile.getType(), null);
        setName(aFile.getName());
        setLoaded(false);
        FileReader fr = new FileReader();
        fr.readBytesAndRunLater(aFile, () -> fileReaderDidLoad(fr));
    }

    /**
     * Called when FileReader finishes reading bytes.
     */
    void fileReaderDidLoad(FileReader aFR)
    {
        byte bytes[] = aFR.getResultBytes();
        setBytes(bytes);
    }
}
