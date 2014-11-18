package com.example.picoclient.testclient;

/**
 * Created by Daniel on 18/11/2014.
 */
public interface AsyncResponse<T> {
    void processFinish(T output);
}
