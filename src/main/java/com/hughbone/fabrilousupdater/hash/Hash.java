package com.hughbone.fabrilousupdater.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hash {

    public static long getMurmurHash(File file) throws IOException {
        final int m = 0x5bd1e995;
        final int r = 24;
        long k = 0x0L;
        int seed = 1;
        int shift = 0x0;

        // get file size
        long flength = file.length();

        // convert file to byte array
        byte[] byteFile = Files.readAllBytes(Paths.get(file.getPath()));

        long length = 0;
        char b;
        // get good bytes from file
        for(int i = 0; i < flength; i++) {

            b = (char) byteFile[i];

            if (b == 0x9 || b == 0xa || b == 0xd || b == 0x20)
            {
                continue;
            }

            length += 1;

        }
        long h = (seed ^ length);

        for(int i = 0; i < flength; i++) {
            b = (char) byteFile[i];

            if (b == 0x9 || b == 0xa || b == 0xd || b == 0x20)
            {
                continue;
            }

            if (b > 255) {
                while (b > 255) {
                    b -= 255;
                }
            }

            k = k | ((long) b << shift);

            shift = shift + 0x8;

            if (shift == 0x20) {
                h = 0x00000000FFFFFFFFL & h;

                k = k * m;
                k = 0x00000000FFFFFFFFL & k;

                k = k ^ (k >> r);
                k = 0x00000000FFFFFFFFL & k;

                k = k * m;
                k = 0x00000000FFFFFFFFL & k;

                h = h * m;
                h = 0x00000000FFFFFFFFL & h;

                h = h ^ k;
                h = 0x00000000FFFFFFFFL & h;

                k = 0x0;
                shift = 0x0;
            }
        }

        if (shift > 0)
        {
            h = h ^ k;
            h = 0x00000000FFFFFFFFL & h;

            h = h * m;
            h = 0x00000000FFFFFFFFL & h;
        }

        h = h ^ (h >> 13);
        h = 0x00000000FFFFFFFFL & h;

        h = h * m;
        h = 0x00000000FFFFFFFFL & h;

        h = h ^ (h >> 15);
        h = 0x00000000FFFFFFFFL & h;

        return h;
    }


    public static String getSH1(File file) {

        // ex. --> String shString = getSHA1(new File("config/renammd.jar"));

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
