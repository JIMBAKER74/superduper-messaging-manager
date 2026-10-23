package com.sdmmllc.superdupersmsmanager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class ParcelFileDescriptorUtil {
	public static final String TAG = "ParcelFileDescriptorUtil";

    public static ParcelFileDescriptor pipeTo(Context context, OutputStream outputStream, Uri uri) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        // start the transfer thread
        new WriteThread(context, new ParcelFileDescriptor.AutoCloseInputStream(readSide), 
        		outputStream, uri).start();

        return writeSide;
    }

    public static ParcelFileDescriptor pipeFrom(Context context, InputStream inputStream, Uri uri) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        // start the transfer thread
        //new ReadThread(context, inputStream, new ParcelFileDescriptor.AutoCloseOutputStream(writeSide), 
        //		uri).start();
        //Log.i(TAG, "called start() TransferThread on uri: " + uri.toString());

        //return readSide;
        
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File tempFile;
        InputStream input;
        OutputStream output;
        
        try {
			tempFile = File.createTempFile("sdmms_r", "tmp", outputDir);
            output = new FileOutputStream(tempFile);
		} catch (IOException e1) {
            Log.e(TAG, "WriteThread create file error on uri: " + uri.toString());
			e1.printStackTrace();
			return null;
		}
        byte[] buf = new byte[8192];
        int len;
        if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started ReadThread on uri: " + uri.toString());

        try {
            while ((len = inputStream.read(buf)) > 0) {
            	if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started ReadThread wrote buf length: " + len);
                output.write(buf, 0, len);
            }
            output.flush(); // just to be safe
            output.close();
            ParcelFileDescriptor temp = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            return temp;
        } catch (IOException e) {
            Log.e(TAG, "ReadThread on uri: " + uri.toString());
            e.printStackTrace();
        }
        finally {
            try {
            	inputStream.close();
            } catch (IOException e) {
            }
        }
        tempFile.delete();
        if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Finished ReadThread on uri: " + uri.toString());
        return null;
    }

    static class WriteThread extends Thread {
        final InputStream mIn;
        final OutputStream mOut;
        final Uri mUri;
        final Context mContext;

        WriteThread(Context context, InputStream in, OutputStream out,
        		final Uri uri) {
            super("ParcelFileDescriptor WriteThread id");
            mContext = context;
            mIn = in;
            mOut = out;
            mUri = uri; 
            setDaemon(true);
        }
        
        @Override
        public void run() {
            File outputDir = mContext.getCacheDir(); // context being the Activity pointer
            File tempFile;
            InputStream input;
            OutputStream output;
            
            try {
				tempFile = File.createTempFile("sdmms_w", "tmp", outputDir);
	            output = new FileOutputStream(tempFile);
			} catch (IOException e1) {
                Log.e(TAG, "WriteThread create file error on uri: " + mUri.toString());
				e1.printStackTrace();
				return;
			}
            
            byte[] buf = new byte[8192];
            int len;
            if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started WriteThread on uri: " + mUri.toString());

            //mIn.
            try {
                while ((len = mIn.read(buf)) > 0) {
                	if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started WriteThread wrote buf length: " + len);
                    output.write(buf, 0, len);
                }
                output.flush(); // just to be safe
                output.close();
	            input = new FileInputStream(tempFile);
                while((len = input.read(buf)) > 0) {
                	if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started WriteThread wrote buf length: " + len);
                	mOut.write(buf, 0, len);
                }
                mOut.flush();
                input.close();
                if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Finished WriteThread ");
            } catch (IOException e) {
                Log.e(TAG, "WriteThread on uri: " + mUri.toString());
                e.printStackTrace();
            }
            finally {
                try {
                    mIn.close();
                } catch (IOException e) {
                }
                try {
                    mOut.close();
                } catch (IOException e) {
                }
            }
            tempFile.delete();
            if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Finished WriteThread on uri: " + mUri.toString());
       }
    }
    
    static class ReadThread extends Thread {
        final InputStream mIn;
        final OutputStream mOut;
        final Uri mUri;
        final Context mContext;
 
        ReadThread(Context context, InputStream in, OutputStream out, final Uri uri) {
            super("ParcelFileDescriptor ReadThread");
            mIn = in;
            mOut = out;
            mUri = uri; 
            mContext = context;
            setDaemon(true);
        }

        @Override
        public void run() {
            File outputDir = mContext.getCacheDir(); // context being the Activity pointer
            File tempFile;
            InputStream input;
            OutputStream output;
            
            try {
				tempFile = File.createTempFile("sdmms_r", "tmp", outputDir);
	            output = new FileOutputStream(tempFile);
			} catch (IOException e1) {
                Log.e(TAG, "WriteThread create file error on uri: " + mUri.toString());
				e1.printStackTrace();
				return;
			}
            byte[] buf = new byte[8192];
            int len;
            if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started ReadThread on uri: " + mUri.toString());

            try {
                while ((len = mIn.read(buf)) > 0) {
                	if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started ReadThread wrote buf length: " + len);
                    output.write(buf, 0, len);
                }
                output.flush(); // just to be safe
                output.close();
	            input = new FileInputStream(tempFile);
                while((len = input.read(buf)) > 0) {
                	if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Started WriteThread wrote buf length: " + len);
                    mOut.write(buf, 0, len);
                }
                mOut.flush(); // just to be safe
            } catch (IOException e) {
                Log.e(TAG, "ReadThread on uri: " + mUri.toString());
                e.printStackTrace();
            }
            finally {
                try {
                    mIn.close();
                } catch (IOException e) {
                }
                try {
                    mOut.close();
                } catch (IOException e) {
                }
            }
            tempFile.delete();
            if (SdmmsConsts.DEBUG_TEMP_FILE) Log.i(TAG, "Finished ReadThread on uri: " + mUri.toString());
       }
    }
    
    public static boolean writeMode(String mode) {
       if ("w".equals(mode) || "wt".equals(mode)) {
            return true;
        } else {
        	// asssume read-only
            return false;
        }
    }
}