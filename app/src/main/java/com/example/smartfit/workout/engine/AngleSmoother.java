package com.example.smartfit.workout.engine;

import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
public class AngleSmoother {

    private final int windowSize;
    private final Deque<Double> values = new ArrayDeque<>();
    private double sum = 0.0;

    public AngleSmoother(int windowSize){
        this.windowSize = Math.max(1, windowSize);
    }

    public double smooth(double newValue){
        values.addLast(newValue);
        sum += newValue;

        if (values.size() > windowSize){
            sum -= values.removeFirst();
        }

        return sum / values.size();
    }

    public void reset(){
        values.clear();
        sum = 0.0;
    }

}
