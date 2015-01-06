package com.example.picoclient.testclient;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Daniel on 03/12/2014.
 */
public class AsyncTaskRetrieveKey extends AsyncTask<Void, Void, String> {
    static final String serverAddr = "dlow.me";
    static final int serverPort = 1234;
    // will be modified when instantiated
    public AsyncResponse<String> delegate = null;
    private Context ctx;
    public AsyncTaskRetrieveKey(Context context){
        ctx = context;
    }
    @Override
    protected String doInBackground(Void... voids) {
        try {
            InetSocketAddress addr = new InetSocketAddress(serverAddr, serverPort);
            Socket ss = NaiveSocketFactory.getSocketFactory().createSocket();
            ss.connect(addr, 100);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    ss.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(ss.getOutputStream())));
            // welcome message ignore
            br.readLine();
            out.print("get]" + Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID));
            out.flush();
            String secretShareFromServer = br.readLine();
            return secretShareFromServer;
        } catch (UnknownHostException e) {
            Log.getStackTraceString(e);
            return "Connection failed";
        } catch (IOException e) {
            Log.getStackTraceString(e);
            return "Connection failed";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i("Async retrieve key", "finished, going to call delegate now");
        delegate.processFinish(s);
    }
}
