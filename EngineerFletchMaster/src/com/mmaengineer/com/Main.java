package com.mmaengineer.com;

import com.mmaengineer.com.tasks.DropTask;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;

@ScriptManifest(author = "Tyler", info = "FletchMaster script by Tyler", name = "Engineer FletchMaster",
        version = 1.0, logo = "http://i.imgur.com/xjBdQBi.png")
public class Main extends Script {
    private String accessKey = "";
    private String authToken = "";
    private String scriptName = "engineerFishing";
    private ArrayList<Task> tasks = new ArrayList<>();
    private Timer updateOnlineUser;
    private long startTime, fishCaught, profit;
    private String username;

    @Override
    public void onStart(){
        startTime = (System.currentTimeMillis() / 1000);
        username = client.getBot().getUsername();

        tasks.add(new DropTask(this));

        log("Adding you to the list of online users.");
        new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

        log("Setting automatic bot online status update");
        updateOnlineUser = new Timer();
        updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);
    }

    @Override
    public void onExit() {
        log("Thanks for running Engineer Fishing!");
        updateOnlineUser.cancel();
        updateOnlineUser.purge();

        log("Saving your runtime data.");
        String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
                getExperienceTracker().getGainedXP(Skill.FISHING), getExperienceTracker().getGainedLevels(Skill.FISHING),
                fishCaught, profit);

        if (updateUser.equals("success")) {
            log("We have successfully updated your account statistics online.");
        } else {
            log(updateUser);
        }

        log("Removing you from the list of online users.");
        new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "offline");
    }

    @Override
    public int onLoop() throws InterruptedException {
        tasks.forEach(tasks -> tasks.run());
        return 700;
    }

    @Override
    public void onPaint(Graphics2D g) {}
}
