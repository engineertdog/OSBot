package com.mmaengineer.engineerEssenceMiner;

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

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;

@ScriptManifest(author = "Tyler", info = "Essence Miner by Tyler", name = "Engineer Essence Miner", version = 1.0,
		logo = "apiserver/app/osb/engineerEssenceMiner/image/engineerEssenceMinerLogo.png")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerEssenceMiner";
	private long startTime, prevInvCount;
	private int essenceMined, profit, essencePrice, beginningLevel;
	private String username, status;
	private Timer updateOnlineUser;
	private Area teleportArea = new Area(3252, 3404, 3255, 3399);
	private String[] nonDepositItems = {"Bronze pickaxe", "Iron pickaxe", "Steel pickaxe", "Mithril pickaxe",
			"Adamant pickaxe", "Rune pickaxe", "Dragon pickaxe", "Inferno axe"};
	private final Image bg = getImage("apiserver/app/osb/engineerEssenceMiner/image/engineerEssenceMiner.png");


	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() throws InterruptedException {
		log("Welcome to Engineer Essence Miner by Tyler.");
		startTime = (new Date().getTime() / 1000);
	    username = client.getBot().getUsername();

		log("Getting exchange prices of essence.");
		essencePrice = GrandExchange.getPrice(7936);

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

		log("Setting automatic bot online status update");
	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);

	    prevInvCount = getInventory().getAmount("Rune essence", "Pure essence");

	    getExperienceTracker().start(Skill.MINING);
	    beginningLevel = getSkills().getStatic(Skill.MINING);
	}

	private enum State {
		WALK_BANK, WALK_TO_AUBURY, TELEPORT_TO_MINE, TELEPORT_FROM_MINE, BANK, MINE, FLEE
	};

	private State getState() throws InterruptedException {
		if (getSkills().getDynamic(Skill.HITPOINTS) < 10) {
			return State.FLEE;
		}

		updateEssenceMined();

		if (getInventory().isFull()) {
			if (Banks.VARROCK_EAST.contains(myPosition())) {
				return State.BANK;
			} else if (teleportArea.contains(myPosition())) {
				return State.WALK_BANK;
			} else {
				return State.TELEPORT_FROM_MINE;
			}
		} else {
			if (Banks.VARROCK_EAST.contains(myPosition())) {
				return State.WALK_TO_AUBURY;
			} else if (teleportArea.contains(myPosition())){
				return State.TELEPORT_TO_MINE;
			} else {
				return State.MINE;
			}
		}
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
			case WALK_BANK:
				walkBank();
				break;
			case WALK_TO_AUBURY:
				walkToAubury();
				break;
			case TELEPORT_TO_MINE:
				teleportToMine();
				break;
			case TELEPORT_FROM_MINE:
				teleportFromMine();
				break;
			case BANK:
				bank();
				break;
			case MINE:
				mine();
				break;
			case FLEE:
				flee();
				break;
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Essence Miner!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime, String.valueOf(getExperienceTracker().getGainedXP(Skill.MINING)),
				String.valueOf(getExperienceTracker().getGainedLevels(Skill.MINING)), String.valueOf(essenceMined), String.valueOf(profit));

		if (updateUser.equals("success")) {
			log("We have successfully updated your account statistics online.");
		} else {
			log(updateUser);
		}

		log("Removing you from the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "offline");
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
		int currentXP = skills.getExperience(Skill.MINING);
		int xpGained = getExperienceTracker().getGainedXP(Skill.MINING);
		g.drawString(NumberFormat.getInstance().format(xpGained), 57, 61);

		// XP Per Hour
		int xpPerHour = (int)(xpGained / (timePassed / 3600));
		g.drawString(NumberFormat.getInstance().format(xpPerHour), 65, 76);

		// Levels Gained
	    int currentLevel = skills.getStatic(Skill.MINING);
		int levelsGained = getExperienceTracker().getGainedLevels(Skill.MINING);

		g.drawString(String.valueOf(beginningLevel), 80, 101);
		g.drawString(String.valueOf(currentLevel), 71, 116);
		g.drawString(String.valueOf(levelsGained), 71, 132);

		// Percent to Next Level
		int currentLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.MINING));
		int nextLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.MINING) + 1);
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

		// Essence Mined
		g.drawString(String.valueOf(NumberFormat.getInstance().format(essenceMined)), 240, 116);

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
	private void walkBank() throws InterruptedException {
		status = "Walking to the bank";

		if (teleportArea.contains(myPosition())) {
			getWalking().webWalk(Banks.VARROCK_EAST);
		}
	}

	private void walkToAubury() throws InterruptedException {
		status = "Walking to the mine";
		getWalking().webWalk(teleportArea);
	}

	private void teleportToMine() throws InterruptedException {
		status = "Teleporting to mine";
		NPC aubury = npcs.closest("Aubury");

		if (aubury != null) {
			if (!myPlayer().isAnimating()) {
				aubury.interact("Teleport");

		    	 new ConditionalSleep(5000) {
			        public boolean condition() throws InterruptedException {
			        	return myPlayer().isAnimating();
			        }
			     }.sleep();
		    }
		}
	}

	private void teleportFromMine() throws InterruptedException {
		status = "Teleporting from mine";

		Stream.concat(getNpcs().getAll().stream(), getObjects().getAll().stream())
	      	.filter(entity -> entity.getName().contains("Portal"))
	      	.min((portal1, portal2) -> Integer.compare(myPosition().distance(portal1.getPosition()), myPosition().distance(portal2.getPosition())))
	      	.ifPresent(portal -> {
	      		if (portal.interact("Exit", "Use", "Enter")) {
	      			new ConditionalSleep(10000) {
	      				@Override
	      				public boolean condition() throws InterruptedException {
	      					return teleportArea.contains(myPosition());
	      				}
	      			}.sleep();
	      		} else {
	      			log("Could not find the exit portal.");
	      		}
	      	});
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
    		getBank().depositAllExcept(nonDepositItems);
		}
	}

	private void mine() throws InterruptedException {
		status = "Mining";
		Entity closestMiningSpot = getObjects().closest("Rune Essence");

		if (getDialogues().isPendingContinuation()) {
			sleep(random(1000, 6000));
			getDialogues().clickContinue();
		}

		if (!myPlayer().isAnimating()) {
			if (closestMiningSpot != null) {
				if (closestMiningSpot.interact("Mine")) {
					mouse.moveVerySlightly();

				    new ConditionalSleep(10000) {
				        public boolean condition() {
				            return myPlayer().isAnimating();
				        }
				    }.sleep();
				}
			}
		}
	}

	private void flee() throws InterruptedException {
		log("Under combat, fleeing by logging out.");
		this.stop();
	}


	/***********************
	 *
	 * Core functions
	 *
	 ***********************/
	private void updateEssenceMined() throws InterruptedException {
		long curInvCount = getInventory().getAmount("Rune essence", "Pure essence");

		if (curInvCount > prevInvCount) {
			essenceMined += curInvCount - prevInvCount;
		}

		prevInvCount = curInvCount;
		profit = (essenceMined * essencePrice);
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