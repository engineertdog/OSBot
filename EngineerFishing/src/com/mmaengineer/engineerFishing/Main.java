package com.mmaengineer.engineerFishing;

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
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

@ScriptManifest(author = "Tyler", info = "Powerfishing script by Tyler", name = "Engineer Fishing",
version = 1.0, logo = "http://i.imgur.com/xjBdQBi.png")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerFishing";
	private String[] dropListExcludes = {"Small fishing net", "Fishing rod", "Fishing Bait", "Fly fishing rod", "Feather",
			"Harpoon", "Lobster pot", "Barbarian rod", "Coins"};
	private long startTime;
	private int beginningLevel, fishCaught, profit;
	private HashMap<String, Integer> fishIndex = new HashMap<>();
	private String username, status;
	private Timer updateOnlineUser;
	private JFrame gui;
	private boolean started = false;
	private boolean fleeing = false;
	private Position storedFishingSpot;
	private String selectedFishingMethod = "Lure";
	private String fishToCatch = "Trout/Salmon";
	private String foodToEat = "Trout";
	private final Image bg = getImage("http://i.imgur.com/5QSTcvh.png");

	/***********************
	 *
	 * Initialize functions
	 *
	 ***********************/
	private void createGUI() {
		gui = new JFrame("Engineer Fishing");

		final int GUI_WIDTH = 400, GUI_HEIGHT = 125;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		final int gX = (int) (screenSize.getWidth() / 2) - (GUI_WIDTH / 2);
		final int gY = (int) (screenSize.getHeight() / 2) - (GUI_HEIGHT / 2);

		gui.setBounds(gX, gY, GUI_WIDTH, GUI_HEIGHT);
		gui.setResizable(false);

		JPanel panel = new JPanel();
		gui.add(panel);

		JLabel fishToCatchLabel = new JLabel("What would you like to fish?");
		fishToCatchLabel.setForeground(Color.white);
		panel.add(fishToCatchLabel);

		JComboBox<String> fishSelecter = new JComboBox<>(new String[]{"Shrimp/Anchovies", "Sardine/Herring", "Trout/Salmon", "Pike", "Lobster", "Tuna/Swordfish",
		 	"Shark", "Monkfish", "Bare-Handed", "Barbarian Leaping Fish"});
		panel.add(fishSelecter);

		JLabel foodToEatLabel = new JLabel("What would you like to eat if you have low health?");
		foodToEatLabel.setForeground(Color.white);
		panel.add(foodToEatLabel);

		JComboBox<String> foodSelecter = new JComboBox<>(new String[]{"Shark", "Lobster", "Trout", "Salmon", "Shrimp", "Anchovies", "Pike", "Tuna", "Swordfish",
			"Monkfish"});
		panel.add(foodSelecter);

		JButton startButton = new JButton("Start");
		panel.add(startButton);
		gui.setVisible(true);

		startButton.addActionListener(e -> {
			fishToCatch = fishSelecter.getSelectedItem().toString();
			foodToEat = foodSelecter.getSelectedItem().toString();
			started  = true;

			setupVariables();
			gui.setVisible(false);
		});
	}

	private void setupVariables(){
		switch (fishToCatch) {
			case "Shrimp/Anchovies":
			case "Monkfish":
				selectedFishingMethod = "Net";
				break;
			case "Trout/Salmon":
			case "Sardine/Herring":
				selectedFishingMethod = "Lure";
				break;
			case "Pike":
				selectedFishingMethod = "Bait";
				break;
			case "Lobster":
				selectedFishingMethod = "Cage";
				break;
			case "Tuna/Swordfish":
			case "Shark":
			case "Bare-Handed":
				selectedFishingMethod = "Harpoon";
				break;
			case "Barbarian Leaping Fish":
				selectedFishingMethod = "Use-rod";
				break;
		}

		checkInventory();
	}

	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() throws InterruptedException {
		log("Welcome to Engineer Fishing by Tyler.");
		createGUI();
		startTime = (System.currentTimeMillis() / 1000);
	    username = client.getBot().getUsername();
	    getExperienceTracker().start(Skill.FISHING);
	    beginningLevel = getSkills().getStatic(Skill.FISHING);

		log("Getting exchange prices of fish.");
		fishIndex.put("shrimps", GrandExchange.getPrice(317));
		fishIndex.put("sardine", GrandExchange.getPrice(325));
		fishIndex.put("herring", GrandExchange.getPrice(325));
		fishIndex.put("shrimps", GrandExchange.getPrice(345));
		fishIndex.put("anchovies", GrandExchange.getPrice(321));
		fishIndex.put("trout", GrandExchange.getPrice(335));
		fishIndex.put("salmon", GrandExchange.getPrice(331));
		fishIndex.put("pike", GrandExchange.getPrice(349));
		fishIndex.put("tuna", GrandExchange.getPrice(359));
		fishIndex.put("lobster", GrandExchange.getPrice(377));
		fishIndex.put("swordfish", GrandExchange.getPrice(371));
		fishIndex.put("monkfish", GrandExchange.getPrice(7944));
		fishIndex.put("shark", GrandExchange.getPrice(383));
		fishIndex.put("leapingTrout", GrandExchange.getPrice(11328));
		fishIndex.put("leapingSalmon", GrandExchange.getPrice(11330));
		fishIndex.put("leapingSturgeon", GrandExchange.getPrice(11332));

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

		log("Setting automatic bot online status update");
	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);
	}

	private enum State {
		DROP, FISH, FLEE
	};

	private State getState() {
		if (!fleeing) {
			if (getSkills().getDynamic(Skill.HITPOINTS) < 10) {
				return State.FLEE;
			}

			checkInventory();

			if (getInventory().isFull()) {
				return State.DROP;
			} else {
				return State.FISH;
			}
		}

		return State.FLEE;
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (started) {
			switch (getState()) {
				case DROP:
					drop();
					break;
				case FISH:
					fish();
					break;
				case FLEE:
					flee();
					break;
			}
		}

		return 250;
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

		if (gui != null) {
            gui.setVisible(false);
            gui.dispose();
        }

		log("Removing you from the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "offline");
	}

	@Override
	public void onMessage(Message message) throws java.lang.InterruptedException {
		String txt = message.getMessage().toLowerCase();

		if (txt.contains("you catch")) {
			fishCaught++;
		}

		for (String fish : fishIndex.keySet()) {
			if (txt.contains(fish)) {
				profit += fishIndex.get(fish);
			}
		}
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
			long timePassed =  ((System.currentTimeMillis() / 1000) - startTime);
			g.drawString(ft(timePassed), 49, 36);

			// XP Gained
			int currentXP = skills.getExperience(Skill.FISHING);
			int xpGained = getExperienceTracker().getGainedXP(Skill.FISHING);
			g.drawString(NumberFormat.getInstance().format(xpGained), 57, 61);

            // XP Per Hour
			if (xpGained >= 1) {
				long xpPerHour = ((xpGained * 3600) / timePassed);
				g.drawString(NumberFormat.getInstance().format(xpPerHour), 65, 76);
			} else {
				g.drawString("0", 65, 76);
			}

			// Levels Gained
			int currentLevel = skills.getStatic(Skill.FISHING);
			int levelsGained = getExperienceTracker().getGainedLevels(Skill.FISHING);

			g.drawString(String.valueOf(beginningLevel), 80, 101);
			g.drawString(String.valueOf(currentLevel), 71, 116);
			g.drawString(String.valueOf(levelsGained), 71, 132);

			// Percent to Next Level
			int currentLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.FISHING));
			int nextLevelXP = skills.getExperienceForLevel(getSkills().getStatic(Skill.FISHING) + 1);
			double percentTNL = ((float)((currentXP - currentLevelXP) * 100) / (float)(nextLevelXP - currentLevelXP));

			DecimalFormat df = new DecimalFormat("#.#");
			g.drawString(df.format(percentTNL), 64, 156);

			// Time to Next Level
			if (xpGained >= 1) {
				int xpTillNextLevel = nextLevelXP - currentXP;
				long xpPerHour = ((xpGained * 3600) / timePassed);
				long timeTNL = ((xpTillNextLevel * 3600) / xpPerHour);
				g.drawString(ft(timeTNL), 52, 170);
			} else {
				g.drawString("0", 52, 170);
			}

			// System Status
			g.drawString(status, 210, 37);

			// Fish Caught
			g.drawString(String.valueOf(NumberFormat.getInstance().format(fishCaught)), 227, 116);

			// Profit
			g.drawString(String.valueOf(NumberFormat.getInstance().format(profit)), 200, 132);

			// Version
			g.drawString("1.0", 208, 157);
		}
	}

	/***********************
	 *
	 * State functions
	 *
	 ***********************/
	private void drop() throws InterruptedException {
		status = "Dropping Fish";
		inventory.dropAllExcept(dropListExcludes);
	}

	private void fish() throws InterruptedException {
		status = "Fishing";
		NPC fishing_spot = getNpcs().closest(o -> o != null && o.getName().contains("Fishing spot") && getMap().canReach(o));

		if (getDialogues().isPendingContinuation()) {
			sleep(random(1000, 6000));
			getDialogues().clickContinue();
		}

		if (!myPlayer().isAnimating()) {
			if (fishing_spot != null) {
				if (fishing_spot.interact(selectedFishingMethod)) {
				    new ConditionalSleep(5000) {
				        public boolean condition() {
				            return myPlayer().isAnimating();
				        }
				     }.sleep();
				}
			}
		}
	}

	private void flee() throws InterruptedException {
		status = "Heading to bank for food";
		storedFishingSpot = myPosition();
		Area closestBank = ClosestBank.getClosestBank(myPosition());
		int numFood;
		HashMap<String, Integer> healedHP = new HashMap<>();
		healedHP.put("Shark", 20);
		healedHP.put("Lobster", 12);
		healedHP.put("Trout", 7);
		healedHP.put("Salmon", 9);
		healedHP.put("Shrimp", 3);
		healedHP.put("Anchovies", 1);
		healedHP.put("Pike", 8);
		healedHP.put("Tuna", 10);
		healedHP.put("Swordfish", 14);
		healedHP.put("Monkfish", 16);

		int healthToHeal = getSkills().getStatic(Skill.HITPOINTS) - getSkills().getDynamic(Skill.HITPOINTS);
		numFood = (int) Math.ceil((double) healthToHeal / healedHP.get(foodToEat));

		if (closestBank.contains(myPosition())) {
			if (!getBank().isOpen()) {
				if (!myPlayer().isAnimating()) {
					new ConditionalSleep(5000) {
						public boolean condition() throws InterruptedException {
							return getBank().open();
						}
					}.sleep();
				}
			} else {
				while (!readyToEat(numFood)) {
					getBank().withdraw(foodToEat, numFood);
				}

				getBank().close();
				returnFishing(numFood);
			}
		} else {
			getWalking().webWalk(closestBank);
		}
	}

	private void returnFishing(int numFood) {
		if (!getBank().isOpen()) {
			for (int i = 0; i < numFood; i++){
				if (inventory.contains(foodToEat)) {
					inventory.interact("Eat", foodToEat);
				}
			}

			fleeing = false;
			getWalking().webWalk(storedFishingSpot);
		} else {
			getBank().close();
			returnFishing(numFood);
		}
	}

	/***********************
	 *
	 * Core functions
	 *
	 ***********************/
	private boolean readyToEat(int numFood) {
		return inventory.getAmount(foodToEat) == numFood;
	}

	private void checkInventory() {
		switch (fishToCatch) {
			case "Shrimp/Anchovies":
				if (!inventory.contains("Small fishing net")) {
					log("A small fishing net is required to powerfish Shrimp/Anchovies.");
					this.stop();
				}
				break;
			case "Trout/Salmon":
				if (!inventory.contains("Fly fishing rod") || !inventory.contains("Feather")) {
					log("A Fly fishing rod and Feathers are required to powerfish Trout/Salmon.");
					this.stop();
				}
				break;
			case "Sardine/Herring":
			case "Pike":
				if (!inventory.contains("Fishing rod") || !inventory.contains("Fishing bait")) {
					log("A Fishing rod and Fishing bait are required to powerfish Sardine/Herring or Pike.");
					this.stop();
				}
				break;
			case "Lobster":
				if (!inventory.contains("Lobster pot")) {
					log("A Lobster Pot is required to powerfish Lobster.");
					this.stop();
				}
				break;
			case "Tuna/Swordfish":
			case "Shark":
				if (!inventory.contains("Harpoon")) {
					log("A harpoon is required to powerfish Shrimp/Anchovies or Shark.");
					this.stop();
				}
				break;
			case "Monkfish":
				if (!inventory.contains("Small fishing net")) {
					log("A Small fishing net is required to powerfish Monkfish.");
					this.stop();
				}
				break;
			case "Barbarian Fishing":
				if (!inventory.contains("Barbarian rod")) {
					log("A Barbarian rod is required to powerfish the Barbarian Leaping fish.");
					this.stop();
				}
				break;
		}
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