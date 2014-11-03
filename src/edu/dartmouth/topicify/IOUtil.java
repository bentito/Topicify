package edu.dartmouth.topicify;

import java.io.*;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class IOUtil {

    public static byte[] readFile(String filename) throws IOException {
        return readFile(new File(filename));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
    
    public static byte[] readBufferedFile(String filename) throws IOException {
    	File file = new File(filename);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
	
	    int read;
	    byte[] buff = new byte[1024];
	    while ((read = in.read(buff)) > 0)
	    {
	        out.write(buff, 0, read);
	    }
	    out.flush();
	    byte[] audioBytes = out.toByteArray();
	    in.close();
	    return audioBytes;
    }
//    public static byte[] getFileInformation(String filepath) {
//        MediaPlayer eSound = MediaPlayer.create(context, Uri.parse(filepath));
//        eSound.reset();
//        eSound.setAudioStreamType(AudioManager.STREAM_SYSTEM);
//        try {
//            eSound.setDataSource(filepath);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            eSound.prepare();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        int duration = eSound.getDuration() / 1000;
//
//        int height = 480;
//        int width = 640;
//        height = eSound.getVideoHeight();
//        width = eSound.getVideoWidth();
//
//        eSound.release();
//        File f = new File(filepath);
//        int size = (int) f.length();
//        byte[] b = new byte[16];
//        System.arraycopy(convertIntToByte(size), 0, b, 0, 4);
//        System.arraycopy(convertIntToByte(duration), 0, b, 4, 4);
//        System.arraycopy(convertIntToByte(width), 0, b, 8, 4);
//        System.arraycopy(convertIntToByte(height), 0, b, 12, 4);
//        return b;
//    }
}