package com.example.picoclient.testclient;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Daniel on 18/11/2014.
 */
public class NetworkAsyncTask extends AsyncTask<Void, Void, String>{

    static final String serverAddr = "dlow.me";
    static final int serverPort = 1234;
    // will be modified when instantiated
    public AsyncResponse<String> delegate = null;

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Socket ss = new Socket(serverAddr, serverPort);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    ss.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(ss.getOutputStream())));
            // welcome message ignore
            br.readLine();
            out.print("key");
            out.flush();
            String secretShareFromServer = br.readLine();
            return secretShareFromServer;
        } catch (IOException e) {
            Log.getStackTraceString(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i("Async task", "finished, going to call delegate now");
        delegate.processFinish(s);
    }
}
