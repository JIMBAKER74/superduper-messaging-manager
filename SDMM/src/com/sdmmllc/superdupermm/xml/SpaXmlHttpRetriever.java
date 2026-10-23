package com.sdmmllc.superdupermm.xml;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import android.content.Context;
import android.net.Uri;

import com.sdmmllc.superdupermm.comms.CommManager;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class SpaXmlHttpRetriever {
    private final String TAG = getClass().getSimpleName();

    CommManager commManager;
    
    public SpaXmlHttpRetriever(Context context) {
    	commManager = CommManager.getCommManager(context);
    }

    public String retrieve(String url) {
        StringBuilder sb = new StringBuilder();

        HttpURLConnection urlConnection = null;
        try {

            Uri uri = Uri.parse(url);
            URL myUrl = new URL(String.format("http://%s%s", uri.getEncodedAuthority(), uri.getEncodedPath()));

            urlConnection = (HttpURLConnection) myUrl.openConnection();

            if(uri.getEncodedQuery() != null){
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setFixedLengthStreamingMode(uri.getEncodedQuery().getBytes().length);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(uri.getEncodedQuery());
                out.close();
            }
            Scanner inStream = new Scanner(urlConnection.getInputStream());
            while(inStream.hasNext()) {
                String str = inStream.nextLine();
                sb.append(str);
            }
            return sb.toString();

        } catch (IOException e) {
            ErrorReporting.onError(TAG, e);
            commManager.networkError();
            e.printStackTrace();
        }
        finally {
            if(urlConnection != null) urlConnection.disconnect();
        }
        return null;
    }

}
