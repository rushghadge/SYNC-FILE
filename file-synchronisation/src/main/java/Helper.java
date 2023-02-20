import org.apache.log4j.Logger;

import java.io.File;

public class Helper {
    public static void deleteAllFiles(Logger logger, String... paths){
        for (String dir : paths) {
            File directory = new File(dir);
            try{
                assert directory.exists();
                File[] files = directory.listFiles();
                try {
                    assert files != null;
                    StringBuilder fileNames = new StringBuilder();
                    for (File file : files) {
                        logger.info("Deleting "+ file.toString());
                        file.delete();
                    }
                }catch (AssertionError | NullPointerException e){
                    logger.info("directory "+ dir +" is empty no need to delete");
                } catch (Exception e){
                    logger.error("Unknown error", e);
                }
            }catch (AssertionError e){
                logger.fatal("directory "+ dir +" doesn't exist");
            }catch (Exception e){
                logger.error("Unknown error", e);
            }
        }
    }


    public static void DeleteSingleFile(String localDir, String fileName)
    {

        File file
                = new File(  localDir+fileName);

        if (file.delete()) {
            System.out.println("File deleted successfully");
        }
        else {
            System.out.println("Failed to delete the file");
        }
    }

    public static void save4Bytes(byte[] bytes1, byte[] bytes2) {
        for (int i=0; i<4; i++) {
            bytes2[i]=bytes1[i];
        }
    }
    /**
     * get the first 4 bytes from bytes2
     * @param bytes2
     * @return
     */
    public static byte[] get4Bytes(byte[] bytes2) {
        byte[] get=new byte[4];
        for (int i=0; i<4; i++) {
            get[i]=bytes2[i];
        }
        return get;
    }
    /**
     * turn an integer a to byte array of size 4.
     * @param a
     * @return
     */
    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    /**
     * turn a byte array of size 4 to an integer
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }


    /**
     * takes file path as input and returns true if path leads to valid directory
     * @param path
     * @return
     */
    public static boolean checkPath(String path) {
        boolean isValid = false;
        try {
            File test = new File(path); // throws exception if invalid path
            if (test.isFile()) // false if it's file, not folder
                isValid = false;
            else if (test.isDirectory()) // return true only if path leads to valid directory
                isValid = true;
        } catch (Exception e) {
            isValid = false;
            e.printStackTrace();
        }
        return isValid;
    }
}
