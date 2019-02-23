package com.mmaengineer.engineerCooker;

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
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;

@ScriptManifest(author = "Tyler", info = "Picking those pockets", name = "Engineer Cooker", version = 1.0,
	logo = "")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerCooker";
	private long startTime, profit, troutPrice, salmonPrice;
	private int beginningLevel, itemsCooked;
	private int counter = 0;
	private Boolean cooking = false;
	private String status;
	private String username;
	private Timer updateOnlineUser;
	private final Image bg = getImage("apiserver/app/osb/engineerCooker/image/engineerCooker.png");

	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() {
		log("Welcome to Engineer Cooker by Tyler.");
		startTime = (new Date().getTime() / 1000);
	    profit = 0;
	    username = client.getBot().getUsername();
	    getExperienceTracker().start(Skill.COOKING);
	    beginningLevel = getSkills().getStatic(Skill.COOKING);

		log("Getting exchange prices of cooked fish.");
		troutPrice = GrandExchange.getPrice(333);
		salmonPrice = GrandExchange.getPrice(329);

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);
	}

	private enum State {
		BANK, COOK
	};

	private State getState() {
		if (inventory.contains("Raw trout", "Raw salmon")) {
			return State.COOK;
		} else {
			return State.BANK;
		}
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
			case BANK:
				bank();
				break;
			case COOK:
				cook();
				break;
		default:
			sleep(random(500, 700));
			break;
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Cooker!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
				getExperienceTracker().getGainedXP(Skill.COOKING), getExperienceTracker().getGainedLevels(Skill.COOKING),
				itemsCooked, profit);

		if (updateUser.equals("success")) {
			log("We have successfully updated your account statistics online.");
		} else {
			log(updateUser);
		}

		log("Removing you from the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "offline");
	}

	public void onMessage(Message message) {
		String txt = message.getMessage().toLowerCase();

		if (txt.contains("successfully cook a trout")) {
			itemsCooked++;
			profit += troutPrice;
		} else if (txt.contains("successfully cook a salmon")) {
			itemsCooked++;
			profit += salmonPrice;
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
		int currentXP = skills.getExperience(Skill.COOKING);
		int xpGained = getExperienceTracker().getGainedXP(Skill.COOKING);
		g.drawString(NumberFormat.getInstance().format(xpGained), 57, 61);

		// XP Per Hour
		int xpPerHour = (int)(xpGained / (timePassed / 3600));
		g.drawString(NumberFormat.getInstance().format(xpPerHour), 65, 76);

		// Levels Gained
	    int currentLevel = skills.getStatic(Skill.COOKING);
		int levelsGained = getExperienceTracker().getGainedLevels(Skill.COOKING);

		g.drawString(String.valueOf(beginningLevel), 80, 101);
		g.drawString(String.valueOf(currentLevel), 71, 116);
		g.drawString(String.valueOf(levelsGained), 71, 132);

		// Percent to Next Level
		int currentLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.COOKING));
		int nextLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.COOKING) + 1);
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

		// Fish Cooked
		g.drawString(String.valueOf(NumberFormat.getInstance().format(itemsCooked)), 230, 116);

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
	private void bank() throws InterruptedException {
		status = "Banking";

		if (!getBank().isOpen()) {
			if (!myPlayer().isAnimating()) {
		    	 new ConditionalSleep(5000) {
			        public boolean condition() throws InterruptedException {
			            return getBank().open();
			        }
			     }.sleep();
		    }
		} else {
			if (inventory.isEmpty()) {
				log("Withdrawing salmon");
				getBank().withdrawAll("Raw salmon");
				getBank().close();
				cooking = false;
				counter = 0;
			} else {
				log("Depositing items");
				getBank().depositAll();
			}
		}
	}

	private void cook() throws InterruptedException {
		status = "Cooking";
		RS2Widget optionMenu = getWidgets().get(307, 2);

		if (getDialogues().isPendingContinuation()) {
			sleep(random(1000, 5000));
			getDialogues().clickContinue();
		}

		if (!myPlayer().isAnimating()) {
			counter += 1;
		}

		if (counter == 25) {
			counter = 0;
			cooking = false;
		}

		if (!cooking) {
			if (!myPlayer().isAnimating()) {
				counter = 0;
				RS2Object fire = getObjects().closest("Fire");

				if (fire != null) {
					if (!getInventory().isItemSelected() && optionMenu == null) {
	                    if (getInventory().interact("Use", "Raw salmon")) {
	                        new ConditionalSleep(3000, random(600, 900)) {
	                            @Override
	                            public boolean condition() throws InterruptedException {
	                                return getInventory().isItemSelected();
	                            }
	                        }.sleep();
	                    }
	                } else {
	                	if (optionMenu == null) {
	                        if (fire.interact("Use")) {
	                            new ConditionalSleep(3000) {
	                                @Override
	                                public boolean condition() throws InterruptedException {
	                                    return optionMenu != null;
	                                }
	                            }.sleep();
	                        }
	                    }

	                	if (optionMenu != null) {
	                        if (optionMenu.isVisible()) {
	                            if (optionMenu.interact("Cook All")) {
	                                new ConditionalSleep(4000) {
	                                    @Override
	                                    public boolean condition() throws InterruptedException {
	                                        return myPlayer().isAnimating();
	                                    }
	                                }.sleep();

	                                if (myPlayer().isAnimating()) {
	                                	cooking = true;
	                                }
	                            }
	                        }
	                    }
	                }
				}
			}
		}

		sleep(650);
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