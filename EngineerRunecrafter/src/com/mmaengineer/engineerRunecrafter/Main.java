package com.mmaengineer.engineerRunecrafter;

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
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "Tyler", info = "Runecrafting Script by Tyler", name = "Engineer Runecrafter",
version = 1.0, logo = "")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerRunecrafter";
	private long startTime;
	private Boolean craft = true;
	private int counter = 0;
	private int beginningLevel, runesCrafted, profit, fireRunePrice;
    private Area altarAreaEnter = new Area(3309, 3252, 3313, 3253);
    private Area altarAreaPortal = new Area(2571, 4853, 2577, 4846);
    private Area altarArea = new Area(2581, 4841, 2585, 4835);
	private String username, status;
	private Timer updateOnlineUser;
	private final Image bg = getImage("apiserver/app/osb/engineerRunecrafter/image/engineerRunecrafter.png");

	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() throws InterruptedException {
		log("Welcome to Engineer Runecrafter by Tyler.");
	    startTime = (new Date().getTime() / 1000);
	    username = client.getBot().getUsername();
	    getExperienceTracker().start(Skill.RUNECRAFTING);
	    beginningLevel = getSkills().getStatic(Skill.RUNECRAFTING);

		log("Getting exchange prices of runes.");
        fireRunePrice = GrandExchange.getPrice(554);

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

		log("Setting automatic bot online status update");
	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);
	}

	private enum State {
        FLEE, WALK_TO_ALTAR, BANK, WITHDRAW, ENTER_ALTAR, WALK_TO_BANK, WALK_TO_CRAFT, LEAVE_ALTAR, CRAFT, WALK_TO_LEAVE, WAIT
	};

	private State getState() {
		if (getSkills().getDynamic(Skill.HITPOINTS) < 10) {
			return State.FLEE;
		}

		if (Banks.AL_KHARID.contains(myPosition())) {
		    if (inventory.isFull() && inventory.contains("Pure essence")) {
		        return State.WALK_TO_ALTAR;
            } else {
		        if (inventory.contains("Fire rune")) {
		            return State.BANK;
                } else {
		            return State.WITHDRAW;
                }
            }
        } else if (altarAreaEnter.contains(myPosition())) {
            if (inventory.isFull() && inventory.contains("Pure essence")) {
                return State.ENTER_ALTAR;
            } else {
                return State.WALK_TO_BANK;
            }
        } else if (altarAreaPortal.contains(myPosition())) {
            if (inventory.isFull() && inventory.contains("Pure essence")) {
                return State.WALK_TO_CRAFT;
            } else {
                return State.LEAVE_ALTAR;
            }
        } else if (altarArea.contains(myPosition())) {
            if (inventory.isFull() && inventory.contains("Pure essence")) {
                return State.CRAFT;
            } else {
                return State.WALK_TO_LEAVE;
            }
        } else {
		    return State.WAIT;
        }
	}

	@Override
	public int onLoop() throws InterruptedException {
        switch (getState()) {
            case FLEE:
                flee();
                break;
            case WALK_TO_ALTAR:
                walkToAltar();
                break;
            case BANK:
                bank();
                break;
            case WITHDRAW:
                withdraw();
                break;
            case ENTER_ALTAR:
                enterAltar();
                break;
            case WALK_TO_BANK:
                walkToBank();
                break;
            case WALK_TO_CRAFT:
                walkToCraft();
                break;
            case LEAVE_ALTAR:
                leaveAltar();
                break;
            case CRAFT:
                craft();
                break;
            case WALK_TO_LEAVE:
                walkToLeave();
                break;
            case WAIT:
                sleep(random(500, 700));
                break;
        }

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Runecrafter!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
				getExperienceTracker().getGainedXP(Skill.RUNECRAFTING), getExperienceTracker().getGainedLevels(Skill.RUNECRAFTING),
                runesCrafted, profit);

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

		if (txt.contains("fire runes")) {
            runesCrafted += 28;
            profit += fireRunePrice * 28;
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
        int currentXP = skills.getExperience(Skill.RUNECRAFTING);
        int xpGained = getExperienceTracker().getGainedXP(Skill.RUNECRAFTING);
        g.drawString(NumberFormat.getInstance().format(xpGained), 57, 61);

        // XP Per Hour
        int xpPerHour = (int)(xpGained / (timePassed / 3600));
        g.drawString(NumberFormat.getInstance().format(xpPerHour), 65, 76);

        // Levels Gained
        int currentLevel = skills.getStatic(Skill.RUNECRAFTING);
        int levelsGained = getExperienceTracker().getGainedLevels(Skill.RUNECRAFTING);

        g.drawString(String.valueOf(beginningLevel), 80, 101);
        g.drawString(String.valueOf(currentLevel), 71, 116);
        g.drawString(String.valueOf(levelsGained), 71, 132);

        // Percent to Next Level
        int currentLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.RUNECRAFTING));
        int nextLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.RUNECRAFTING) + 1);
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

        // Runes Made
        g.drawString(String.valueOf(NumberFormat.getInstance().format(runesCrafted)), 225, 114);

        // Profit
        g.drawString(String.valueOf(NumberFormat.getInstance().format(profit)), 200, 130);

        // Version
        g.drawString("1.0", 208, 157);
	}

	/***********************
	 *
	 * State functions
	 *
	 ***********************/
    private void flee() throws InterruptedException {
        log("Under combat, fleeing by logging out.");
        this.stop();
    }

	private void walkToAltar() throws InterruptedException {
		status = "Walking to Altar";
		getWalking().webWalk(altarAreaEnter);
	}

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
            getBank().depositAll();
        }
    }

    private void withdraw() throws InterruptedException {
        status = "Withdrawing";

        if (!getBank().isOpen()) {
            if (!myPlayer().isAnimating()) {
                new ConditionalSleep(5000) {
                    public boolean condition() throws InterruptedException {
                        return getBank().open();
                    }
                }.sleep();
            }
        } else {
            getBank().withdrawAll("Pure essence");
        }
    }

    private void enterAltar() throws InterruptedException {
        status = "Entering Altar";

        if (!myPlayer().isAnimating()) {
            RS2Object altarEntrance = getObjects().closest(i -> i != null && i.getName().contains("Mysterious ruins"));
            altarEntrance.interact("Enter");

            new ConditionalSleep(5000) {
                public boolean condition() throws InterruptedException {
                    return altarAreaPortal.contains(myPosition());
                }
            }.sleep();
        }
    }

    private void walkToBank() throws InterruptedException {
        status = "Walking to Bank";
        getWalking().webWalk(Banks.AL_KHARID);
    }

    private void walkToCraft() throws InterruptedException {
        status = "Walking to Crafting Spot";
        getWalking().webWalk(altarArea);
    }

    private void leaveAltar() throws InterruptedException {
        status = "Leaving altar";
        counter = 0;
        craft = true;

        Stream.concat(getNpcs().getAll().stream(), getObjects().getAll().stream())
                .filter(entity -> entity.getName().contains("Portal"))
                .min((portal1, portal2) -> Integer.compare(myPosition().distance(portal1.getPosition()), myPosition().distance(portal2.getPosition())))
                .ifPresent(portal -> {
                    if (portal.interact("Exit", "Use", "Enter")) {
                        new ConditionalSleep(10000) {
                            @Override
                            public boolean condition() throws InterruptedException {
                                return altarAreaEnter.contains(myPosition());
                            }
                        }.sleep();
                    } else {
                        log("Could not find the exit portal.");
                    }
                });
    }

    private void craft() throws InterruptedException {
        status = "Crafting";

        if (!myPlayer().isAnimating()) {
            counter += 1;
        }

        if (counter == 25) {
            counter = 0;
            craft = true;
        }

        if (craft) {
            RS2Object altar = getObjects().closest(i -> i != null && i.getName().contains("Altar"));
            altar.interact("Craft-rune");
            craft = false;

            new ConditionalSleep(5000) {
                public boolean condition() throws InterruptedException {
                    return !myPlayer().isAnimating();
                }
            }.sleep();
        }
    }

    private void walkToLeave() throws InterruptedException {
        status = "Walking to Portal";
        getWalking().webWalk(altarAreaPortal);
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