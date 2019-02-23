package com.mmaengineer.com.tasks;

import com.mmaengineer.com.Task;
import org.osbot.rs07.script.MethodProvider;

public class DropTask extends Task {
    public DropTask(MethodProvider api) {
        super(api);
    }

    @Override
    public boolean verify() {
        return api.getInventory().isFull();
    }

    @Override
    public void execute() {
        api.getInventory().dropAll();
    }

    @Override
    public String describe() {
        return "Dropping stuff.";
    }
}
