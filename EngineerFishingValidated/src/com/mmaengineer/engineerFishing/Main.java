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
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

@ScriptManifest(author = "Tyler", info = "Powerfishing script by Tyler", name = "Engineer Fishing",
	version = 1.0, logo = "apiserver/app/osb/engineerFishing/image/engineerFishingLogo.png")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String userKey = "";
	private String scriptName = "engineerFishing";
	private String[] dropListExcludes = {"Small fishing net", "Fishing rod", "Fishing Bait", "Fly fishing rod", "Feather",
			"Harpoon", "Lobster Cage", "Barbarian rod", "Fish offcuts", "Roe", "Caviar", "Coins"};
	private long startTime, timePassed, timeTNL, xpPerHour;
	private int beginningXP, currentXP, xpGained, currentLevel, beginningLevel, levelsGained, fishCaught, profit,
		shrimpPrice, sardinePrice, herringPrice, anchoviesPrice, troutPrice, salmonPrice, pikePrice, tunaPrice,
		lobsterPrice, swordfishPrice, monkfishPrice, sharkPrice, leapingTroutPrice, leapingSalmonPrice,
		leapingSturgeonPrice;
	private double currentLevelXp, nextLevelXP, percentTNL, xpTillNextLevel;
	private String username, status, xpGainedString, xpPerHourString;
	private Timer updateOnlineUser;
	private JFrame gui;
	private boolean started = false;

	//All set by the GUI;
	private String selectedFishingMethod = "Salmon/Trout";
    //private boolean powerFishingMode = true;

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

	private void createGUI() {
    	 gui = new JFrame("Engineer Fishing");

		 final int GUI_WIDTH = 350, GUI_HEIGHT = 100;
		 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		 final int gX = (int) (screenSize.getWidth() / 2) - (GUI_WIDTH / 2);
		 final int gY = (int) (screenSize.getHeight() / 2) - (GUI_HEIGHT / 2);

		 gui.setBounds(gX, gY, GUI_WIDTH, GUI_HEIGHT);
		 gui.setResizable(false);

		 JPanel panel = new JPanel();
		 gui.add(panel);

		 JLabel label_1 = new JLabel("What would you like to fish?:");
		 label_1.setForeground(Color.white);
		 panel.add(label_1);

		 JComboBox<String> fishSelecter = new JComboBox<>(new String[]{"Shrimp/Anchovies", "Sardine/Herring",
				 "Salmon/Trout", "Pike", "Lobster", "Swordfish/Tuna", "Shark", "Monkfish"});
		 panel.add(fishSelecter);

		 JCheckBox powerFishingCheckBox = new JCheckBox("Power Fishing");
		 powerFishingCheckBox.setSelected(true);
		 powerFishingCheckBox.setEnabled(false);
		 panel.add(powerFishingCheckBox);

		 JButton startButton = new JButton("Start");
		 panel.add(startButton);
		 gui.setVisible(true);

         startButton.addActionListener(e -> {
        	 selectedFishingMethod = fishSelecter.getSelectedItem().toString();
             started  = true;

             /*if (!powerFishingCheckBox.isSelected()) {
            	 powerFishingMode = false;
             }*/

             setFishingMethod();
             gui.setVisible(false);
         });
	}

	public void setFishingMethod(){
		if (selectedFishingMethod.equals("Shrimp/Anchovies")) {
			selectedFishingMethod = "Net";

			if (!inventory.contains("Small fishing net")) {
				log("A small fishing net is required to powerfish Shrimp/Anvhovies.");
				this.stop();
			}
		} else if (selectedFishingMethod.equals("Salmon/Trout")) {
			selectedFishingMethod = "Lure";

			if (!inventory.contains("Fly fishing rod")) {
				log("A Fly fishing rod is required to powerfish Salmon/Trout.");
				this.stop();
			}

			if (!inventory.contains("Feather")) {
				log("Feathers are required to powerfish Salmon/Trout.");
				this.stop();
			}
		} else if (selectedFishingMethod.equals("Sardine/Herring") || selectedFishingMethod.equals("Pike")) {
			selectedFishingMethod = "Bait";

			if (!inventory.contains("Fishing rod")) {
				log("A Fishing rod is required to powerfish Sardine/Herring or Pike.");
				this.stop();
			}

			if (!inventory.contains("Fishing bait")) {
				log("Fishing bait is required to powerfish Sardine/Herring or Pike.");
				this.stop();
			}
		} else if (selectedFishingMethod.equals("Lobster")) {
			selectedFishingMethod = "Cage";

			if (!inventory.contains("Lobster pot")) {
				log("A Lobster Pot is required to powerfish Lobster.");
				this.stop();
			}
		} else if (selectedFishingMethod.equals("Swordfish/Tuna") || selectedFishingMethod.equals("Shark")) {
			selectedFishingMethod = "Harpoon";

			if (!inventory.contains("Harpoon")) {
				log("A harpoon is required to powerfish Shrimp/Anvhovies or Shark.");
				this.stop();
			}
		}
	}

	@Override
	public void onStart() throws InterruptedException {
		log("Validating Script.");
	    username = client.getBot().getUsername();
	    String validateScript = ValidateScript.validateScript(accessKey, authToken, userKey, username, scriptName);

	    if (!validateScript.equals("valid")) {
			log("We could not validate your use for this script.");
			log("Stopping.");
			log("Reason: " + validateScript);
			this.stop();
		}

		log("Welcome to Engineer Fishing by Tyler.");
		createGUI();
	    startTime = System.currentTimeMillis();
	    timeTNL = 0;
	    beginningXP = skills.getExperience(Skill.FISHING);
	    beginningLevel = skills.getStatic(Skill.FISHING);

		log("Getting exchange prices of fish.");
	    shrimpPrice = GrandExchange.getPrice(317);
		sardinePrice = GrandExchange.getPrice(325);
		herringPrice = GrandExchange.getPrice(345);
	    anchoviesPrice = GrandExchange.getPrice(321);
	    troutPrice = GrandExchange.getPrice(335);
		salmonPrice = GrandExchange.getPrice(331);
		pikePrice = GrandExchange.getPrice(349);
		tunaPrice = GrandExchange.getPrice(359);
		lobsterPrice = GrandExchange.getPrice(377);
		swordfishPrice = GrandExchange.getPrice(371);
		monkfishPrice = GrandExchange.getPrice(7944);
		sharkPrice = GrandExchange.getPrice(383);
		leapingTroutPrice = GrandExchange.getPrice(11328);
		leapingSalmonPrice = GrandExchange.getPrice(11330);
		leapingSturgeonPrice = GrandExchange.getPrice(11332);

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

		log("Setting automatic bot online status update");
	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),
	    		0, 120000);
	}

	private enum State {
		DROP, FISH
	};

	private State getState() {
		if (selectedFishingMethod.equals("Lure")) {
			if (!inventory.contains("Feather")) {
				log("Ran out of feathers. Stopping script.");
				this.stop();
			}
		} else if (selectedFishingMethod.equals("Bait")) {
			if (!inventory.contains("Fishing bait")) {
				log("Ran out of fishing bait. Stopping script.");
				this.stop();
			}
		}

		if (getInventory().isFull()) {
			return State.DROP;
		} else {
			return State.FISH;
		}
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
			default:
				sleep(random(500, 700));
				break;
			}
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Fishing!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
				String.valueOf(xpGained), String.valueOf(levelsGained), String.valueOf(fishCaught), String.valueOf(profit));

		if (updateUser.equals("success")) {
			log("We have successfully updated your account statistics online.");
		} else {
			log(updateUser);
		}

		log("Removing you from the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "offline");
	}

	private void drop() throws InterruptedException {
		status = "Dropping Fish";
		inventory.dropAllExcept(dropListExcludes);
	}

	private void fish() throws InterruptedException {
		status = "Fishing";
		NPC fishing_spot = getNpcs().closest("Fishing spot");

		if (getDialogues().isPendingContinuation()) {
			sleep(random(1000, 6000));
			getDialogues().clickContinue();
		}

		if (!myPlayer().isAnimating()) {
			if (fishing_spot != null) {
				if (fishing_spot.interact(selectedFishingMethod)) {
					mouse.moveVerySlightly();

				    new ConditionalSleep(5000) {
				        public boolean condition() {
				            return myPlayer().isAnimating();
				        }
				     }.sleep();
				}
			}
		}
	}

	public void onMessage(Message message) throws java.lang.InterruptedException {
		String txt = message.getMessage().toLowerCase();

		if (txt.contains("ou catch")) {
			fishCaught++;
		}

		if (txt.contains("shrimps")) {
			profit += shrimpPrice;
		} else if (txt.contains("sardine")) {
			profit += sardinePrice;
		} else if (txt.contains("herring")) {
			profit += herringPrice;
		} else if (txt.contains("anchovies")) {
			profit += anchoviesPrice;
		} else if (txt.contains("trout")) {
			profit += troutPrice;
		} else if (txt.contains("salmon")) {
			profit += salmonPrice;
		}else if (txt.contains("pike")) {
			profit += pikePrice;
		} else if (txt.contains("tuna")) {
			profit += tunaPrice;
		} else if (txt.contains("lobster")) {
			profit += lobsterPrice;
		} else if (txt.contains("swordfish")) {
			profit += swordfishPrice;
		} else if (txt.contains("monkfish")) {
			profit += monkfishPrice;
		} else if (txt.contains("shark")) {
			profit += sharkPrice;
		} else if (txt.contains("leapingTrout")) {
			profit += leapingTroutPrice;
		} else if (txt.contains("leapingSalmon")) {
			profit += leapingSalmonPrice;
		} else if (txt.contains("leapingSturgeon")) {
			profit += leapingSturgeonPrice;
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		if (started) {
			/**
			 * Set Defaults
			*/
			g.setColor(Color.WHITE);
			Font currentFont = g.getFont();
			Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.8F);
			g.setFont(newFont);

			/**
			 * Draw Background
			*/
			g.drawImage(bg, 1, 1, null);

			/**
			 * Time Ran
			*/
			timePassed = System.currentTimeMillis() - startTime;
			g.drawString(ft(timePassed), 49, 36);

			/**
			 * XP Gained
			*/
			currentXP = skills.getExperience(Skill.FISHING);
			xpGained = currentXP - beginningXP;
			xpGainedString = NumberFormat.getInstance().format(xpGained);
			g.drawString(xpGainedString, 55, 61);

			/**
			 * XP Per Hour
			*/
			xpPerHour = (int)(xpGained / (timePassed / 3600000.0D));
			xpPerHourString = NumberFormat.getInstance().format(xpPerHour);
			g.drawString(xpPerHourString, 65, 76);

			/**
			 * Levels Gained
			*/
		    currentLevel = skills.getStatic(Skill.FISHING);
			levelsGained = currentLevel - beginningLevel;

			g.drawString(String.valueOf(beginningLevel), 80, 101);
			g.drawString(String.valueOf(currentLevel), 71, 116);
			g.drawString(String.valueOf(levelsGained), 71, 132);

			/**
			 * Percent to Next Level
			*/
			currentLevelXp = XP_TABLE[currentLevel];
			nextLevelXP = XP_TABLE[currentLevel + 1];
			percentTNL = ((currentXP - currentLevelXp) / (nextLevelXP - currentLevelXp) * 100);

			DecimalFormat df = new DecimalFormat("#.#");
			g.drawString(df.format(percentTNL), 64, 156);

			/**
			 * Time to Next Level
			*/
			xpTillNextLevel = nextLevelXP - currentXP;

			if (xpGained >= 1) {
			    timeTNL = (long) ((xpTillNextLevel / xpPerHour) * 3600000);
			}

			g.drawString(ft(timeTNL), 52, 170);

			/**
			 * System Status
			*/
			g.drawString(status, 210, 37);

			/**
			 * Fish Caught
			*/
			g.drawString(String.valueOf(NumberFormat.getInstance().format(fishCaught)), 250, 116);

			/**
			 * Profit
			*/
			g.drawString(String.valueOf(NumberFormat.getInstance().format(profit)), 208, 132);

			/**
			 * Version
			*/
			g.drawString("1.0", 215, 155);
		}
	}

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