package com.mmaengineer.com;

import org.osbot.rs07.script.MethodProvider;

public abstract class Task {
    protected MethodProvider api;
    public static final int FinalFishLevel = 30;
    public static final int FinalWCLevel = 30;

    public Task(MethodProvider api) {
        this.api = api;
    }

    /**
     * @return if this Task should execute.
     */
    public abstract boolean verify();

    /**
     * Executes this Task if allowed to do so.
     *
     * @return sleep time after this task ends.
     */
    public abstract void execute();

    /**
     * Runs the task.
     *
     * @return sleep time after this task ends.
     */
    public void run() {
        if (verify()) {
            execute();
        }
    }

    /**
     * @return a description of the current Task.
     */
    public abstract String describe();
}