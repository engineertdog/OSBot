package com.mmaengineer.engineerShopBuyer;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import javax.swing.*;

@ScriptManifest(author = "Tyler", info = "Shop Buyer by Tyler", name = "Engineer Shop Buyer", version = 1.0, logo = "")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerShopBuyer";
	private long startTime, itemInvCount;
	private int itemsBought = 0;
	private Position bankPosition1 = new Position(2450, 3482, 1);
	private Position bankPosition2 = new Position(2448, 3482, 1);
	private Area bankArea = new Area(bankPosition1, bankPosition2);
	private Position hudoPosition1 = new Position(2448, 3508, 1);
	private Position hudoPosition2 = new Position(2450, 3511, 1);
	private Area shopHudo = new Area(hudoPosition1, hudoPosition2);
	private Position heckelFunchPosition1 = new Position(2489, 3489, 1);
	private Position heckelFunchPosition2 = new Position(2493, 3487, 1);
	private Area shopHeckelFunch = new Area(heckelFunchPosition1, heckelFunchPosition2);
	private Area shopArea = shopHudo;
	private String username, status;
	private String mode = "Single";
	private boolean banking = false;
	private boolean worldHop = false;
	private String shopNPC = "Hudo";
	private int shopCount = 0;
	private JFrame gui;
	private boolean started = false;
	private Timer updateOnlineUser;

	/***********************
	 *
	 * Initialize functions
	 *
	 ***********************/
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

		JComboBox<String> modeSelector = new JComboBox<>(new String[]{"Hudo", "Heckel Funch", "Both"});
		panel.add(modeSelector);

		JButton startButton = new JButton("Start");
		panel.add(startButton);
		gui.setVisible(true);

		startButton.addActionListener(e -> {
			setMode(modeSelector.getSelectedItem().toString());
			started  = true;
			gui.setVisible(false);
		});
	}

	private void setMode(String selectedMode) {
		switch (selectedMode) {
			case "Hudo":
				shopNPC = "Hudo";
				shopArea = shopHudo;
				break;
			case "Heckel Funch":
				shopNPC = "Heckel Funch";
				shopArea = shopHeckelFunch;
				break;
			case "Both":
				shopNPC = "Hudo";
				shopArea = shopHudo;
				mode = "Dual";
				break;
		}
	}

	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() throws InterruptedException {
		log("Welcome to Engineer Shop Buyer by Tyler.");
		createGUI();

		new ConditionalSleep(25000) {
			public boolean condition() throws InterruptedException {
				return started;
			}
		}.sleep();

		startTime = (System.currentTimeMillis() / 1000);
	    status = "Script Starting";
	    username = client.getBot().getUsername();
		itemInvCount = getInventory().getAmount("Pineapple");

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);

		if (!shopArea.contains(myPosition())) {
			walkToShop();
		}
	}

	private enum State {
		BANK, WALK_TO_SHOP, BUY, WALK_TO_BANK, WORLD_HOP
	};

	private State getState() throws InterruptedException {
		updateInvCounter();

		if (bankArea.contains(myPosition())) {
			if (banking) {
				return State.BANK;
			}

			if (inventory.isFull()) {
				return State.BANK;
			} else {
				return State.WALK_TO_SHOP;
			}
		} else {
			if (!worldHop) {
				if (!inventory.isFull()) {
					return State.BUY;
				} else {
					return State.WALK_TO_BANK;
				}
			} else {
				return State.WORLD_HOP;
			}
		}
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (started) {
			switch (getState()) {
				case BANK:
					bank();
					break;
				case WALK_TO_SHOP:
					walkToShop();
					break;
				case BUY:
					buy();
					break;
				case WALK_TO_BANK:
					walkToBank();
					break;
				case WORLD_HOP:
					worldHop();
					break;
			}
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Shop Buyer!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
				itemsBought);

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
	public void onPaint(final Graphics2D g) {
		if (started) {
			// Set Defaults
			g.setColor(Color.BLACK);
			g.drawRect(5, 5, 150, 50);
			g.fillRect(5, 5, 150, 50);

			g.setColor(Color.WHITE);
			Font fontText = new Font("Arial", 0, 10);
			g.setFont(fontText);

			// Pizzas / Hr
			g.drawString(status, 15, 15);
			g.drawString("Pineapples per Hour", 15, 30);

			if (itemsBought > 0) {
				long timePassed = ((System.currentTimeMillis() / 1000) - startTime);
				long pineapplesPerHour = ((itemsBought * 3600) / timePassed);
				g.drawString(NumberFormat.getInstance().format(pineapplesPerHour), 15, 45);
			} else {
				g.drawString(NumberFormat.getInstance().format(0), 15, 45);
			}
		}
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
			getBank().depositAllExcept("Coins");

			if (inventory.isEmptyExcept("Coins")) {
				banking = false;
			}
		}
	}

	private void walkToShop() throws InterruptedException {
		status = "Walking to Shop";
		getWalking().webWalk(shopArea);
	}

	private void buy() throws InterruptedException {
		status = "Buying Pineapples";
		String[] storeItems = new String[] {"Pineapple"};
		RS2Widget pineappleMenu;

		if (Objects.equals(shopNPC, "Hudo")) {
			pineappleMenu = getWidgets().get(300, 2, 15);
		} else {
			pineappleMenu = getWidgets().get(300, 2, 5);
		}

		if (!myPlayer().isAnimating()) {
			NPC shop = getNpcs().closest(i -> i != null && i.getName().contains(shopNPC));

			if (shop != null) {
				if (pineappleMenu == null) {
					if (shop.interact("Trade")) {
						new ConditionalSleep(5000) {
							public boolean condition() throws InterruptedException {
								return getStore().isOpen();
							}
						}.sleep();
					}
				}

				if (pineappleMenu != null) {
					if (getStore().getAmount(storeItems) >= 1) {
						pineappleMenu.interact("Buy 10");
						sleep(random(200, 250));
					} else {
						if (Objects.equals(mode, "Dual")) {
							if (shopCount == 0) {
								if (Objects.equals(shopNPC, "Hudo")) {
									shopNPC = "Heckel Funch";
									shopArea = shopHeckelFunch;
								} else {
									shopNPC = "Hudo";
									shopArea = shopHudo;
								}

								shopCount = 1;
								walkToShop();
							} else {
								closeAndHop();
							}
						} else {
							closeAndHop();
						}
					}
				}
			}
		}
	}

	private void closeAndHop() throws InterruptedException {
		getStore().close();
		worldHop = true;
		worldHop();
	}

	private void walkToBank() throws InterruptedException {
		status = "Walking to Bank";
		getWalking().webWalk(bankArea);
	}

	private void worldHop() throws InterruptedException {
		status = "Hopping Worlds";
		Integer[] worlds = new Integer[] {302, 303, 304, 305, 306, 307, 309, 310, 311, 312, 313, 314, 315, 317, 319, 320, 321, 322, 323, 324, 327, 328, 329,
				330, 331, 332, 333, 334, 336, 338, 339, 340, 341, 342, 343, 344, 346, 347, 348, 350, 351, 352, 354, 355, 356, 357, 358, 359, 360, 362, 367,
				368, 369, 370, 374, 375, 376, 377, 378, 386};
		Integer currentWorld = getWorlds().getCurrentWorld();
		Integer currentWorldIndex = Arrays.asList(worlds).indexOf(currentWorld);
		Integer newWorld;

		if (currentWorld == 386) {
			newWorld = 0;
		} else {
			newWorld = currentWorldIndex + 1;
		}

		if (!getTabs().getOpen().equals(Tab.LOGOUT)) {
			getTabs().open(Tab.LOGOUT);
		} else {
			if (getTabs().getOpen().equals(Tab.LOGOUT)) {
				while (getWorlds().getCurrentWorld() != worlds[newWorld]) {
					getWorlds().hop(worlds[newWorld]);

					new ConditionalSleep(5000) {
						public boolean condition() throws InterruptedException {
							return getWorlds().getCurrentWorld() == worlds[newWorld];
						}
					}.sleep();
				}

				worldHop = false;
				shopCount = 0;
			}
		}
	}

	/***********************
	 *
	 * Core functions
	 *
	 ***********************/
	private void updateInvCounter() throws InterruptedException {
		long curInvCount = getInventory().getAmount("Pineapple");

		if (curInvCount > itemInvCount) {
			itemsBought += (curInvCount - itemInvCount);
		}

		itemInvCount = curInvCount;
	}
}