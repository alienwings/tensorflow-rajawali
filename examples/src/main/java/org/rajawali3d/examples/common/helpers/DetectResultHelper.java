package org.rajawali3d.examples.common.helpers;

import android.graphics.Point;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DetectResultHelper {

    private BlockingQueue<Point> queuedResults = new ArrayBlockingQueue<Point>(16);

    public Point poll() {
        return queuedResults.poll();
    }

    public void offer(Point p) {
        queuedResults.offer(p);
    }
}
