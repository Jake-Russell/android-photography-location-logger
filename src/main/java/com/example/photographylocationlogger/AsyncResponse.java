package com.example.photographylocationlogger;

/**
 * AsyncResponse is an interface to help with the implementation of What3WordsTask AsyncTask.
 * Any classes requiring the use of What3WordsTask should implement processFinish, where the
 * calculated What3Words address can then be used.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public interface AsyncResponse {
    void processFinish(String what3words);
}

