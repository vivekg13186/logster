package com.logster.search;

import java.util.concurrent.atomic.AtomicBoolean;

public class SearchController {
    public final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    public void reset(){
        cancelRequested.set(false);
    }
    public void cancel() {
        cancelRequested.set(true);
    }

    public boolean isCancelled() {
        return cancelRequested.get();
    }
}
