package com.example.picoclient.testclient;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Daniel on 03/12/2014.
 */
public class AsyncTaskCreateKey extends AsyncTask<String, Void, String> {
    static final String serverAddr = "dlow.me";
    static final int serverPort = 1234;
    // will be modified when instantiated
    public AsyncResponse<String> delegate = null;
    private Context ctx;

    public AsyncTaskCreateKey(Context context) {
        ctx = context;
    }

    @Override
    protected String doInBackground(String... s) {
        try {
            InetSocketAddress addr = new InetSocketAddress(serverAddr, serverPort);
            Socket ss = new Socket();
            ss.connect(addr, 100);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    ss.getInputStream()));
            OutputStream outputStream = ss.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(ss.getOutputStream()));
            PrintWriter out = new PrintWriter(bufferedWriter);
            // welcome message ignore
            br.readLine();
            String temp = "add]" + Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID) + "]" + s[0];
            Log.e("Async", s[0]);
            out.print(temp);
//            bufferedWriter.write("add]daniel]");
//            outputStream.write(s.toString());
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
        Log.i("Async task", "finished, going to call delegate now");
        delegate.processFinish(s);
    }
}
