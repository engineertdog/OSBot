package com.mmaengineer.engineerBowMaster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

@ScriptManifest(author = "Tyler", info = "Bow making genius by Tyler", name = "Engineer Bow Master",
version = 1.0, logo = "apiserver/app/osb/engineerFishing/image/engineerFishingLogo.png")
public class Main extends Script {
	// Tracking & core variables
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerBowMaster";
	private long startTime, timeTNL, xpPerHour;
	private int currentFletchingXP, beginningFletchingXP, fletchingXPGained, currentFletchingLevel, beginningFletchingLevel,
		levelsGainedFletching, currentWoodcuttingXP, beginningWoodcuttingXP, woodcuttingXPGained, currentWoodcuttingLevel,
		beginningWoodcuttingLevel, levelsGainedWoodcutting, currentCraftingXP, beginningCraftingXP, craftingXPGained,
		currentCraftingLevel, beginningCraftingLevel, levelsGainedCrafting, fishCaught, profit;
	private double currentLevelXP, nextLevelXP, percentTNL, xpTillNextLevel;
	private String username, status, xpGainedString, xpPerHourString;
	private Timer updateOnlineUser;
	private boolean started = true;

	// Regular tree areas
	private Area treeArea1 = new Area(2722, 3476, 2725, 3482);
	private Area treeArea2 = new Area(2730, 3476, 2734, 3482);
	private Area treeArea3 = new Area(2726, 3472, 2723, 3464);
	private Area treeArea4 = new Area(2731, 3462, 2723, 3456);
	private Area treeArea5 = new Area(2709, 3454, 2728, 3435);
	// Oak tree areas
	private Area oakTreeArea1 = new Area(2723, 3484, 2718, 3477);
	private Area oakTreeArea2 = new Area(2737, 3484, 2731, 3494);
	// Willow tree areas
	private Area willowTreeArea1 = new Area(2715, 3507, 2707, 3515);
	// Maple tree areas
	private Area mapleTreeArea1 = new Area(2720, 3498, 2734, 3504);
	// Yew tree areas
	private Area yewTreeArea1 = new Area(2717, 3467, 2704, 3457);
	// Magic tree areas
	private Area magicTreeArea1 = new Area(2689, 3422, 2698, 3429);

	// Crafting / Smithing areas
	private Area flaxArea = new Area(2737, 3441, 2749, 3444);
	private Area spinningArea = new Area(2714, 3471, 2715, 3470);
	private Area smithingArea = new Area(2711, 3495, 2713, 3494);

	// Set up main variables
	private String treeAction = "Chop down";
	private String requiredTree = "Oak";
	private Area chopArea = oakTreeArea1;
	private Entity treeToCut, birdNest;
	private int choppingPosition = 10;
	private List<Integer> endChoppingPositions = Arrays.asList(5, 11, 20, 30, 40, 50);
	private boolean areaEmpty = true;
	private String[] nonDepositItems = {"Bronze axe", "Iron axe", "Steel axe", "Mithril axe", "Adamant axe", "Rune axe",
			"Dragon axe", "Inferno axe"};

	// Position based variables
	private Position myCurrentPosition, nextTreePosition, newTreePosition;
	private int nextTreeDistance, newTreeDistance;
	private List<String> badTrees = Arrays.asList("[x=2725, y=3447, z=0]");

	private final Image bg = getImage("apiserver/app/osb/engineerFishing/image/engineerFishingPowerFish.png");
	private final int[] XP_TABLE = {
				  0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154,
		          1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018,
		          5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
		          16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224,
		          41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721,
		          101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254,
		          224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428,
		          496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
		          1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068,
		          2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294,
		          4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614,
		          8771558, 9684577, 10692629, 11805606, 13034431, 200000000
	};


	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() throws InterruptedException {
		log("Welcome to Engineer Bow Master by Tyler.");
		startTime = (new Date().getTime() / 1000);
	    timeTNL = 0;
	    username = client.getBot().getUsername();
	    beginningFletchingXP = skills.getExperience(Skill.FLETCHING);
	    beginningFletchingLevel = skills.getStatic(Skill.FLETCHING);
	    beginningWoodcuttingXP = skills.getExperience(Skill.WOODCUTTING);
	    beginningWoodcuttingLevel = skills.getStatic(Skill.WOODCUTTING);
	    beginningCraftingXP = skills.getExperience(Skill.CRAFTING);
	    beginningCraftingLevel = skills.getStatic(Skill.CRAFTING);
	}

	private enum State {
		WALK_BANK, BANK, CHOP, FLEE, ONHOLD
	};

	@SuppressWarnings("unchecked")
	private State getState() throws InterruptedException {
		if (getSkills().getDynamic(Skill.HITPOINTS) < 10) {
			return State.FLEE;
		}

		if (getInventory().isFull()) {
			if (Banks.CAMELOT.contains(myPosition())) {
				return State.BANK;
			} else {
				return State.WALK_BANK;
			}
		} else {
			if (areaEmpty) {
				walkToTrees();
			} else {
				return State.CHOP;
			}
		}

		return State.ONHOLD;
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (started) {
			switch (getState()) {
				case WALK_BANK:
					walkBank();
					break;
				case BANK:
					bank();
					break;
				case CHOP:
					chop();
					break;
				case FLEE:
					flee();
					break;
				case ONHOLD:
					onHold();
					break;
			}
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Bow Master!");


		log("Removing you from the list of online users.");
	}

	@Override
	public void onMessage(Message message) throws java.lang.InterruptedException {
		String txt = message.getMessage().toLowerCase();
	}

	@Override
	public void onPaint(Graphics2D g) {
		if (started) {
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
			/*
			// XP Gained
			currentXP = skills.getExperience(Skill.FISHING);
			xpGained = currentXP - beginningXP;
			xpGainedString = NumberFormat.getInstance().format(xpGained);
			g.drawString(xpGainedString, 57, 61);

			// XP Per Hour
			xpPerHour = (int)(xpGained / (timePassed / 3600000.0D));
			xpPerHourString = NumberFormat.getInstance().format(xpPerHour);
			g.drawString(xpPerHourString, 65, 76);

			// Levels Gained
		    currentLevel = skills.getStatic(Skill.FISHING);
			levelsGained = currentLevel - beginningLevel;

			g.drawString(String.valueOf(beginningLevel), 80, 101);
			g.drawString(String.valueOf(currentLevel), 71, 116);
			g.drawString(String.valueOf(levelsGained), 71, 132);

			// Percent to Next Level
			currentLevelXP = XP_TABLE[currentLevel];
			nextLevelXP = XP_TABLE[currentLevel + 1];
			percentTNL = ((currentXP - currentLevelXP) / (nextLevelXP - currentLevelXP) * 100);

			DecimalFormat df = new DecimalFormat("#.#");
			g.drawString(df.format(percentTNL), 64, 156);

			// Time to Next Level
			xpTillNextLevel = nextLevelXP - currentXP;

			if (xpGained >= 1) {
			    timeTNL = (long) ((xpTillNextLevel / xpPerHour) * 3600000);
			}

			g.drawString(ft(timeTNL), 52, 170);

			// System Status
			g.drawString(status, 210, 37);

			// Fish Caught
			g.drawString(String.valueOf(NumberFormat.getInstance().format(fishCaught)), 225, 114);

			// Profit
			g.drawString(String.valueOf(NumberFormat.getInstance().format(profit)), 200, 130);

			// Version
			g.drawString("1.0", 208, 157);
			*/
		}
	}


	/***********************
	 *
	 * State functions
	 *
	 ***********************/
	private void walkBank() throws InterruptedException {
		status = "Walking to Bank";
		getWalking().webWalk(Banks.CAMELOT);
	}

	private void bank() throws InterruptedException {
		status = "Banking";

		if (!getBank().isOpen()) {
		    if (getBank().open()) {
		    	 new ConditionalSleep(5000) {
			        public boolean condition() throws InterruptedException {
			            return getBank().open();
			        }
			     }.sleep();

		    	if (getBank().isOpen()) {
		    		getBank().depositAllExcept(nonDepositItems);

					status = "Walking";
					getChoppingPosition();
					getTreeArea();
					log("Moving to Tree area #1.");
					getWalking().webWalk(chopArea);
		    	}
		    }
		}
	}

	@SuppressWarnings("unchecked")
	private void chop() throws InterruptedException {
		status = "Chopping";
		treeToCut = getObjects().closest(o -> o.getName().equals(requiredTree) && !badTrees.contains(o.getPosition().toString()));

		if (getDialogues().isPendingContinuation()) {
			sleep(random(1000, 6000));
			getDialogues().clickContinue();
		}

		birdNest = getGroundItems().closest("Bird nest");
		if (birdNest != null) {
			birdNest.interact("Take");
		}

		if (!myPlayer().isAnimating()) {
			if (chopArea.contains(treeToCut)) {
				if (treeToCut != null) {
					if (treeToCut.interact(treeAction)) {
						mouse.moveVerySlightly();

					    new ConditionalSleep(7000) {
					        public boolean condition() {
					            return myPlayer().isAnimating();
					        }
					    }.sleep();
					}
				}
			} else {
				if (!endChoppingPositions.contains(choppingPosition)) {
					areaEmpty = true;
					choppingPosition++;
				} else {
					if (choppingPosition == 11) {
						areaEmpty = true;
						choppingPosition = 10;
					} else {
						if (treeToCut != null) {
							if (treeToCut.interact(treeAction)) {
								mouse.moveVerySlightly();

							    new ConditionalSleep(7000) {
							        public boolean condition() {
							            return myPlayer().isAnimating();
							        }
							    }.sleep();
							}
						}
					}
				}
			}
		}
	}

	private void flee() throws InterruptedException {
		log("Under combat, fleeing by logging out.");
		this.stop();
	}

	private void onHold() throws InterruptedException {
		sleep(50);
	}


	/***********************
	 *
	 * Core functions
	 *
	 ***********************/
	private boolean walkToTrees() throws InterruptedException {
		status = "Walking";

		getTreeArea();

		if (!chopArea.contains(myPosition())) {
			log("Moving to tree area #" + choppingPosition + " for " + requiredTree + ".");
			getWalking().webWalk(chopArea);
			return false;
		} else {
			areaEmpty = false;
		}

		return true;
	}

	private void getTreeArea() throws InterruptedException {
		switch (choppingPosition) {
			case 1:
				chopArea = treeArea1;
				break;
			case 2:
				chopArea = treeArea2;
				break;
			case 3:
				chopArea = treeArea3;
				break;
			case 4:
				chopArea = treeArea4;
				break;
			case 5:
				chopArea = treeArea5;
				break;
			case 10:
				chopArea = oakTreeArea1;
				break;
			case 11:
				chopArea = oakTreeArea2;
				break;
			case 20:
				chopArea = willowTreeArea1;
				break;
			case 30:
				chopArea = mapleTreeArea1;
				break;
			case 40:
				chopArea = yewTreeArea1;
				break;
			case 50:
				chopArea = magicTreeArea1;
				break;
		}
	}

	private void getChoppingPosition() throws InterruptedException {
		if (requiredTree == null) {

		}

		switch (requiredTree) {
			case "Tree":
				choppingPosition = 1;
				break;
			case "Oak":
				choppingPosition = 10;
				break;
			case "Willow":
				choppingPosition = 20;
				break;
			case "Maple":
				choppingPosition = 30;
				break;
			case "Yew":
				choppingPosition = 40;
				break;
			case "Magic":
				choppingPosition = 50;
				break;
		}
	}


	/***********************
	 *
	 * Ancillary functions
	 *
	 ***********************/
	private String ft(long duration) {
		return LocalTime.ofSecondOfDay(TimeUnit.MILLISECONDS.toSeconds(duration)).toString();
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {}

		return null;
	}
}