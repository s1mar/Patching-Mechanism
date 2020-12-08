package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @s1mar
 * A helper zip and unzip class
 */
public class Zipper {

    /**
     *
     * @param dirPath
     * @param fileName
     * Unzips an archive in the @dirPath of the name @fileName. In case something goes wrong it throws an IOException
     * @throws IOException
     */
    public static void unzipClass(String dirPath,String fileName) throws IOException {

        Path pathToFile = Paths.get(dirPath,fileName);
        byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(pathToFile.toString()));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        Path destPath = Paths.get(dirPath,"/staging/");
        File destFile = new File(destPath.toAbsolutePath().toString());

        //If a staging area already exists, delete it so that we can replace its contents with the new ones
        deleteFolderAndItsContent(destPath);

        while (zipEntry!=null){
            File newFile = newFile(destFile, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zipInputStream.getNextEntry();
        }

    }

    /**
     * Deletes Folder with all of its content
     *
     * @param directory path to folder which should be deleted
     */
    public static void deleteFolderAndItsContent(final Path directory) throws IOException {
        if (Files.exists(directory))
        {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException
                {
                    Files.delete(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException
                {
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

        private static void deleteFolder(File file) {
            for (File subFile : file.listFiles()) {
                if (subFile.isDirectory()) {
                    deleteFolder(subFile);
                } else {
                    subFile.delete();
                }
            }
            file.delete();
        }


    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}
