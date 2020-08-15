package snaptea;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * This is an interface to work with JavaScript DataTransfer and DataTransferItems
 */
public interface DataTransfer {

    /**
     * Returns the types.
     */
    String[] getTypes();

    /**
     * Returns the data for a given type.
     */
    String getData(String aType);

    /**
     * Set the data for a given type.
     */
    void setData(String aType, String theData);

    /**
     * Returns the number of files.
     */
    int getFileCount();

    /**
     * Returns the files.
     */
    File[] getFiles();

    /**
     * Sets the image Element element to use for the drag feedback image.
     */
    void setDragImage(HTMLElement aImg, double xOffset, double yOffset);

    /**
     * Returns whether DataTransfer has given type.
     */
    default boolean hasType(String aType)
    {
        String types[] = getTypes();
        for(String type : types)
            if(type.equals(aType))
                return true;
        return false;
    }

    /**
     * Returns a DataTransfer for JSDataTransfer.
     */
    static DataTransfer getDataTrasferForJSDataTransfer(JSDataTransfer jsDT)
    {
        return new DataTransferJS(jsDT);
    }

    /**
     * A DataTransfer implementation for JSDataTransfer.
     */
    class DataTransferJS implements DataTransfer {

        // The JSDataTransfer
        private JSDataTransfer _jsDT;

        /** Constructor. */
        DataTransferJS(JSDataTransfer jsDT)  { _jsDT = jsDT; }

        @Override
        public String[] getTypes()
        {
            JSArray<JSString> typesJS = _jsDT.getTypes();
            int len = typesJS.getLength();
            String types[] = new String[len];
            for (int i=0; i<len; i++)
                types[i] = typesJS.get(i).stringValue();
            return types;
        }

        @Override
        public String getData(String aType)
        {
            return _jsDT.getData(aType);
        }

        @Override
        public void setData(String aType, String theData)
        {
            _jsDT.setData(aType, theData);
        }

        @Override
        public int getFileCount()
        {
            return _jsDT.getFiles().getLength();
        }

        @Override
        public File[] getFiles()
        {
            // Get files and length
            JSArrayReader<File> filesAR = _jsDT.getFiles();
            int len = filesAR.getLength();
            System.out.println("DataTransfer: Getting files: " + len);

            // Load in File array and return
            File files[] = new File[len];
            for (int i=0;i<len;i++) files[i] = filesAR.get(i);
            return files;
        }

        @Override
        public void setDragImage(HTMLElement aImg, double xOffset, double yOffset)
        {
            _jsDT.setDragImage(aImg, xOffset, yOffset);
        }
    }

    /**
     * Returns a DataTransfer for String.
     */
    static DataTransfer getDataTrasferForString(String aStr)
    {
        return new DataTransferString(aStr);
    }

    /**
     * A DataTransfer implementation for String.
     */
    class DataTransferString implements DataTransfer {

        // The String
        private String  _str;

        /** Constructor. */
        DataTransferString(String aStr)  { _str = aStr; }

        @Override
        public String[] getTypes()
        {
            return new String[] { "text/plain" };
        }

        @Override
        public String getData(String aType)
        {
            return _str;
        }

        @Override
        public void setData(String aType, String theData)
        {
            throw new RuntimeException("DataTransfer.DataTransferString.setData: Not implemented");
        }

        @Override
        public int getFileCount()  { return 0; }

        @Override
        public File[] getFiles()  { return new File[0]; }

        @Override
        public void setDragImage(HTMLElement aImg, double xOffset, double yOffset)
        {
            throw new RuntimeException("DataTransfer.DataTransferString.setDragImage: Not implemented");
        }
    }

    /**
     * Returns a DataTransfer for JSDataTransfer.
     */
    //static DataTransfer getDataTrasferForDataTransferItemArray(JSArray<JSDataTransferItem> theDTIs) {
    //    return new DataTransferDTIs(theDTIs); }

//    /**
//     * A DataTransfer implementation for JSDataTransfer.
//     */
//    class DataTransferDTIs implements DataTransfer {
//
//        // The array of JSDataTransferItems
//        private JSArray<JSDataTransferItem> _dtis;
//
//        /** Constructor. */
//        DataTransferDTIs(JSArray<JSDataTransferItem> theDTIs)  { _dtis = theDTIs; }
//
//        @Override
//        public String[] getTypes()
//        {
//            int len = _dtis.getLength();
//            String types[] = new String[len];
//            for (int i=0; i<len; i++) {
//                JSDataTransferItem dti = _dtis.get(i);
//                types[i] = dti.getType();
//            }
//            return types;
//        }
//
//        @Override
//        public String getData(String aType)
//        {
//            int len = _dtis.getLength();
//            for (int i=0; i<len; i++) {
//                JSDataTransferItem dti = _dtis.get(i);
//                if (dti.getType().equals(aType))
//                    return dti.getAsString();
//            }
//
//            System.err.println("DataTransfer.DataTransferDTIs.getData: Data not found for type: " + aType);
//            return null;
//        }
//
//        @Override
//        public void setData(String aType, String theData)
//        {
//            throw new RuntimeException("DataTransfer.DataTransferDTIs.setData: Not implemented");
//        }
//
//        @Override
//        public int getFileCount()
//        {
//            int len = _dtis.getLength();
//            for (int i=0; i<len; i++) {
//                JSDataTransferItem dti = _dtis.get(i);
//                String kind = dti.getKind();
//                if (kind.equals("file"))
//                    return 1;
//            }
//            return 0;
//        }
//
//        @Override
//        public File[] getFiles()
//        {
//            int len = _dtis.getLength();
//            for (int i=0; i<len; i++) {
//                JSDataTransferItem dti = _dtis.get(i);
//                String kind = dti.getKind();
//                if (kind.equals("file"))
//                    return new File[] { dti.getAsFile() };
//            }
//            return new File[0];
//        }
//
//        @Override
//        public void setDragImage(HTMLElement aImg, double xOffset, double yOffset)
//        {
//            throw new RuntimeException("DataTransfer.DataTransferDTIs.setDragImage: Not implemented");
//        }
//    }
}
