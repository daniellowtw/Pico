package com.example.picoclient.testclient;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Daniel on 07/03/2015.
 */
public class UploadDBAsync extends AsyncTask<File, Long, String> {
    Context ctx = null;
    ProgressBar uploadProgressBar;
    TextView uploadProgressText;

    public UploadDBAsync(Context c, View a, View progressTextView) {
        super();
        ctx = c;
        uploadProgressBar = (ProgressBar) a;
        uploadProgressText = (TextView) progressTextView;
    }

    protected void onPreExecute() {
        uploadProgressBar.setVisibility(View.VISIBLE);
        uploadProgressText.setVisibility(View.VISIBLE);
        uploadProgressBar.setProgress(0);
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        super.onProgressUpdate(progress);
        uploadProgressText.setText(progress[0] + "/" + progress[1]);
        uploadProgressBar.setProgress((int) (progress[0]*100/progress[1]));
    }

    @Override
    protected String doInBackground(File... urls) {
        String fileName = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID) + DBHelper.DATABASE_NAME;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = urls[0];
        if (!sourceFile.isFile()) {
            return "Not a file";
        } else {
            int serverResponseCode = 0;
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL("http://dlow.me/test/upload.php");

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                long total = 0;
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    total += bytesRead;
                    publishProgress(total, sourceFile.length());
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                if (serverResponseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    return in.readLine();
                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.e("Upload file to server", "error: " + ex.getMessage());
                return ex.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Upload Exception", e.getMessage());
                return e.getMessage();
            }
            return "Error uploading.";

        }

    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
    }
}
