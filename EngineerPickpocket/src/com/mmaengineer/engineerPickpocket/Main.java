package com.mmaengineer.engineerPickpocket;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

@ScriptManifest(author = "Tyler", info = "Picking those pockets", name = "Engineer Pickpocket", version = 1.0,
	logo = "")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerPickpocket";
	private Area starting_area = new Area(2962, 3378, 2968, 3388);
	private Position death_area;
	private long startTime, profit, startingCoins, coinsPicked, inventoryCoins, lastCoins;
	private int beginningLevel, numDeaths, pocketsPicked, numStunned;
	private String status;
	private String username;
	private boolean dead = false;
	private boolean stunned = false;
	private boolean pickpocket  =  false;
	private Timer updateOnlineUser;
	private final Image bg = getImage("apiserver/app/osb/engineerPickpocket/image/engineerPickpocket.png");

	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() {
		log("Welcome to Engineer Pickpocket by Tyler.");
		startTime = (new Date().getTime() / 1000);
	    profit = 0;
	    startingCoins = inventory.getAmount("Coins");
	    lastCoins = startingCoins;
	    username = client.getBot().getUsername();
	    getExperienceTracker().start(Skill.THIEVING);
	    beginningLevel = getSkills().getStatic(Skill.THIEVING);

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"), 0, 120000);
	}

	private enum State {
		STUNNED, DEAD, PICKPOCKET, WALK_STARTING_AREA
	};

	private State getState() {
		if (getSkills().getDynamic(Skill.HITPOINTS) == 0) {
			death_area = myPlayer().getPosition();
		}

		if (!dead) {
			if (stunned) {
				return State.STUNNED;
			} else {
				if (pickpocket) {
					return State.PICKPOCKET;
				} else {
					if (starting_area.contains(myPosition())) {
						pickpocket = true;
						return State.PICKPOCKET;
					} else {
						return State.WALK_STARTING_AREA;
					}
				}
			}
		} else {
			return State.DEAD;
		}
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
			case STUNNED:
				stunned();
				break;
			case DEAD:
				dead();
				break;
			case WALK_STARTING_AREA:
				walkStartingArea();
				break;
			case PICKPOCKET:
				pickpocket();
				break;
		default:
			sleep(random(500, 700));
			break;
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Pickpocket!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
				getExperienceTracker().getGainedXP(Skill.THIEVING), getExperienceTracker().getGainedLevels(Skill.THIEVING),
				pocketsPicked, numStunned, numDeaths, profit);

		if (updateUser.equals("success")) {
			log("We have successfully updated your account statistics online.");
		} else {
			log(updateUser);
		}

		log("Removing you from the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "offline");
	}

	public void onMessage(Message message) throws java.lang.InterruptedException {
		String txt = message.getMessage().toLowerCase();

		if (txt.contains("ou pick")) {
			inventoryCoins = inventory.getAmount("Coins");

			if (profit == 0) {
				coinsPicked = inventoryCoins - startingCoins;
			} else {
				coinsPicked = inventoryCoins - lastCoins;
			}

			lastCoins = inventoryCoins;
			profit = profit + coinsPicked;
			pocketsPicked++;
		} else if (txt.contains("stunned")) {
			if (!stunned) {
				numStunned++;
				stunned = true;
			}
		} else if (txt.contains("you are dead")) {
			profit = 0;
			numDeaths++;
			dead = true;
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		// Set Defaults
		g.setColor(Color.WHITE);
		Font currentFont = g.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.8F);
		g.setFont(newFont);

		// Draw Background
		g.drawImage(bg, 1, 1, null);

		// Time Ran
		long timePassed = (new Date().getTime() / 1000) - startTime;
		g.drawString(ft(timePassed), 49, 36);

		// XP Gained
		int currentXP = skills.getExperience(Skill.THIEVING);
		int xpGained = getExperienceTracker().getGainedXP(Skill.THIEVING);
		g.drawString(NumberFormat.getInstance().format(xpGained), 57, 61);

		// XP Per Hour
		int xpPerHour = (int)(xpGained / (timePassed / 3600));
		g.drawString(NumberFormat.getInstance().format(xpPerHour), 65, 76);

		// Levels Gained
	    int currentLevel = skills.getStatic(Skill.THIEVING);
		int levelsGained = getExperienceTracker().getGainedLevels(Skill.THIEVING);

		g.drawString(String.valueOf(beginningLevel), 80, 101);
		g.drawString(String.valueOf(currentLevel), 71, 116);
		g.drawString(String.valueOf(levelsGained), 71, 132);

		// Percent to Next Level
		int currentLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.THIEVING));
		int nextLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.THIEVING) + 1);
		double percentTNL = ((float)((currentXP - currentLevelXP) * 100) / (float)(nextLevelXP - currentLevelXP));

		DecimalFormat df = new DecimalFormat("#.#");
		g.drawString(df.format(percentTNL), 64, 156);

		// Time to Next Level
		int xpTillNextLevel = nextLevelXP - currentXP;

		if (xpGained >= 1) {
			long timeTNL = (long) ((xpTillNextLevel * 3600) / xpPerHour);
			g.drawString(ft(timeTNL), 52, 170);
		}

		// System Status
		g.drawString(status, 210, 37);

		// Pockets Picked
		g.drawString(String.valueOf(NumberFormat.getInstance().format(numDeaths)), 215, 77);
		g.drawString(String.valueOf(NumberFormat.getInstance().format(pocketsPicked)), 250, 101);
		g.drawString(String.valueOf(NumberFormat.getInstance().format(numStunned)), 220, 116);

		// Profit
		g.drawString(String.valueOf(NumberFormat.getInstance().format(profit)), 208, 130);

		// Version
		g.drawString("1.0", 215, 157);
	}

	/***********************
	 *
	 * State functions
	 *
	 ***********************/
	private void stunned() throws InterruptedException {
		status = "Stunned";
		sleep(random(3300, 3500));
		stunned = false;
	}

	private void dead() throws InterruptedException {
		if (myPosition().distance(death_area) < 5) {
			GroundItem coins = getGroundItems().closest("Coins");

			if (coins != null) {
				coins.interact("take");
			}

			dead = false;
		} else {
			status = "Walking to death location";
			getWalking().webWalk(death_area);
		}
	}

	private void walkStartingArea() throws InterruptedException {
		status = "Walking to starting area";
		getWalking().webWalk(starting_area);
	}

	private void pickpocket() throws InterruptedException {
		status = "Picking Pockets";
		NPC man = getNpcs().closest("Guard");

		if (man != null) {
			if (man.interact("Pickpocket")) {
				mouse.moveVerySlightly();

			    new ConditionalSleep(5000) {
			        public boolean condition() {
			            return myPlayer().isAnimating();
			        }
			     }.sleep();
			}
		}

		sleep(random(250));
	}

	/***********************
	 *
	 * Ancillary functions
	 *
	 ***********************/
	private String ft(long duration) {
		return LocalTime.ofSecondOfDay(TimeUnit.SECONDS.toSeconds(duration)).toString();
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {}

		return null;
	}
}