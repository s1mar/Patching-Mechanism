package helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class Checksum {


    public static void main(String args[]){

        try {
            String rootDir = Paths.get("").toAbsolutePath().toString();
            Path pathCase1 = Paths.get(rootDir,"patch cases\\case 1- Warm Handover\\patch.zip");
            Path pathCase2 = Paths.get(rootDir,"patch cases\\case 2 - Cold Reboot\\patch.zip");
            Path pathCase3 = Paths.get(rootDir,"patch cases\\case 3 - Region Doesn't match\\patch.zip");
            Path pathCase4 = Paths.get(rootDir,"patch cases\\case 4 - Build Greater\\patch.zip");

            //Let's calculate the checksums
            System.out.println("Checksum of 1: "+getChecksumCRC32(pathCase1,1024));
            System.out.println("Checksum of 2: "+getChecksumCRC32(pathCase2,1024));
            System.out.println("Checksum of 3: "+getChecksumCRC32(pathCase3,1024));
            System.out.println("Checksum of 4: "+getChecksumCRC32(pathCase4,1024));



        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


    public static long getChecksumCRC32(Path filePath, int bufferSize)
            throws IOException {
        InputStream stream = Files.newInputStream(filePath);
        CheckedInputStream checkedInputStream = new CheckedInputStream(stream, new CRC32());
        byte[] buffer = new byte[bufferSize];
        while (checkedInputStream.read(buffer, 0, buffer.length) >= 0) {}
        return checkedInputStream.getChecksum().getValue();
    }
}
