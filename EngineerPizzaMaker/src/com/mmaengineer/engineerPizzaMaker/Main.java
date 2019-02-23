package com.mmaengineer.engineerPizzaMaker;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;

import javax.swing.*;

@ScriptManifest(author = "Tyler", info = "Pizza maker by Tyler", name = "Engineer Pizza Maker", version = 1.0, logo = "")
public class Main extends Script {
	private String accessKey = "";
	private String authToken = "";
	private String scriptName = "engineerPizzaMaker";
	private long startTime, profit, pizzaCookedPrice;
	private long cookedPizzaInv = 0;
	private int pizzasCooked;
	private int pizzasMade = 0;
	private int makingIncompletePizzaCounter = 0;
	private int makingUncookedPizzaCounter = 0;
	private int makingPizzaBasesCounter = 0;
	private boolean makePizzas = false;
	private boolean makingIncompletePizzas = false;
	private boolean makingUncookedPizzas = false;
	private boolean makingPizzaBases = false;
	private boolean makePizzaUncookedGroup = false;
	private boolean makeIncompletePizzas = false;
	private boolean makeUncookedPizzas = false;
	private boolean makePizzaDough = false;
	private boolean makePizzaBase = false;
	private boolean makingIncompletePizzasSkip = true;
	private boolean makingUncookedPizzasSkip = true;
	private boolean makingPizzaBaseSkip = true;
	private JFrame gui;
	private boolean started = false;
	private Area bankToUse;
	private Area rangeAreaCatherby = new Area(2815, 3444, 2818, 3442);
	private Area rangeAreaAlKharid = new Area(3275, 3179, 3272, 3182);
	private Area rangeArea;
	private String username, status;
	private Timer updateOnlineUser;
	private String mode = "Make";

	/***********************
	 *
	 * Initialize functions
	 *
	 ***********************/
	private void createGUI() {
		gui = new JFrame("Engineer Pizza Maker");

		final int GUI_WIDTH = 350, GUI_HEIGHT = 100;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		final int gX = (int) (screenSize.getWidth() / 2) - (GUI_WIDTH / 2);
		final int gY = (int) (screenSize.getHeight() / 2) - (GUI_HEIGHT / 2);

		gui.setBounds(gX, gY, GUI_WIDTH, GUI_HEIGHT);
		gui.setResizable(false);

		JPanel panel = new JPanel();
		gui.add(panel);

		JLabel fishToCatchLabel = new JLabel("Choose range location");
		fishToCatchLabel.setForeground(Color.white);
		panel.add(fishToCatchLabel);

		JComboBox<String> fishSelecter = new JComboBox<>(new String[]{"Catherby", "Al Kharid"});
		panel.add(fishSelecter);

		JButton startButton = new JButton("Start");
		panel.add(startButton);
		gui.setVisible(true);

		startButton.addActionListener(e -> {
			setLocations(fishSelecter.getSelectedItem().toString());
			started  = true;
			gui.setVisible(false);
		});
	}

	private void setLocations(String rangeAreaChosen) {
		switch (rangeAreaChosen) {
			case "Catherby":
				rangeArea = rangeAreaCatherby;
				bankToUse = Banks.CATHERBY;
				break;
			case "Al Kharid":
				rangeArea = rangeAreaAlKharid;
				bankToUse = Banks.AL_KHARID;
				break;
		}
	}

	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() {
		log("Welcome to Engineer Pizza Maker by Tyler.");
		createGUI();
		startTime = (new Date().getTime() / 1000);
	    profit = 0;
	    status = "Script Starting";
	    username = client.getBot().getUsername();
	    getExperienceTracker().start(Skill.COOKING);

		log("Getting exchange prices of cooked fish.");
		pizzaCookedPrice = GrandExchange.getPrice(2289);

		log("Adding you to the list of online users.");
		new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online");

	    updateOnlineUser = new Timer();
	    updateOnlineUser.schedule(new UpdateOnlineStatus(accessKey, authToken, scriptName, username, startTime, "online"),0, 120000);
	}

	private enum State {
		WALK_TO_RANGE, BANK, WITHDRAW_COOKING, COOK, WALK_TO_BANK, MAKE_INCOMPLETE_PIZZAS, MAKE_UNCOOKED_PIZZAS,
		WITHDRAW_MAKING_PIZZAS, MAKE_PIZZA_BASE, WITHDRAW_MAKE_PIZZA_BASE, WAIT
	};

	private State getState() throws InterruptedException {
		if (Objects.equals(mode, "Cook")) {
			updateCookedPizzasCounter();

			if (bankToUse.contains(myPosition())) {
				if (inventory.contains("Uncooked pizza")) {
					return State.WALK_TO_RANGE;
				} else {
					if (inventory.contains("Plain pizza")) {
						return State.BANK;
					} else {
						return State.WITHDRAW_COOKING;
					}
				}
			} else if (rangeArea.contains(myPosition())) {
				if (inventory.contains("Uncooked pizza")) {
					return State.COOK;
				} else {
					return State.WALK_TO_BANK;
				}
			} else {
				return State.WALK_TO_BANK;
			}
		} else {
			if (bankToUse.contains(myPosition())) {
				if (makeUncookedPizzas) {
					if (inventory.getAmount("Incomplete pizza") == 0 || inventory.getAmount("Cheese") == 0) {
						pizzasMade += getInventory().getAmount("Uncooked pizza");
						makingUncookedPizzaCounter = 0;
						makingUncookedPizzas = false;
						makeIncompletePizzas = false;
						makeUncookedPizzas = false;
						makePizzaUncookedGroup = false;
					}
				}

				if (makeIncompletePizzas) {
					if (inventory.getAmount("Pizza base") == 0 || inventory.getAmount("Tomato") == 0) {
						makingIncompletePizzaCounter = 0;
						makingIncompletePizzas = false;
						makeIncompletePizzas = false;
						makeUncookedPizzas = true;
					}
				}

				if (makePizzaDough) {
					if (inventory.getAmount("Bucket of water") == 0 || inventory.getAmount("Pot of flour") == 0) {
						makingPizzaBasesCounter = 0;
						makingPizzaBases = false;
						makePizzaBase = false;
						makePizzaUncookedGroup = false;
						makePizzaDough = false;
					}
				}

				if (makePizzas) {
					if (makePizzaUncookedGroup) {
						if (makeIncompletePizzas) {
							return State.MAKE_INCOMPLETE_PIZZAS;
						} else if (makeUncookedPizzas) {
							return State.MAKE_UNCOOKED_PIZZAS;
						}
					} else {
						return State.WITHDRAW_MAKING_PIZZAS;
					}
				} else {
					if (makePizzaDough) {
						if (makePizzaBase) {
							return State.MAKE_PIZZA_BASE;
						}
					} else {
						return State.WITHDRAW_MAKE_PIZZA_BASE;
					}
				}
			} else {
				return State.WALK_TO_BANK;
			}
		}

		return State.WAIT;
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (started) {
			switch (getState()) {
				case WALK_TO_RANGE:
					walkToRange();
					break;
				case BANK:
					bank();
					break;
				case WITHDRAW_COOKING:
					withdrawCooking();
					break;
				case COOK:
					cook();
					break;
				case WALK_TO_BANK:
					walkToBank();
					break;
				case MAKE_INCOMPLETE_PIZZAS:
					makeIncompletePizzas();
					break;
				case MAKE_UNCOOKED_PIZZAS:
					makeUncookedPizzas();
					break;
				case WITHDRAW_MAKING_PIZZAS:
					withdrawMakingPizzas();
					break;
				case MAKE_PIZZA_BASE:
					makePizzaBase();
					break;
				case WITHDRAW_MAKE_PIZZA_BASE:
					withdrawMakePizzaBase();
					break;
				case WAIT:
					random(200, 300);
					break;
			}
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Pizza Maker!");
		updateOnlineUser.cancel();
		updateOnlineUser.purge();

		log("Saving your runtime data.");
		String updateUser = SaveUserData.saveUserDataMethod(accessKey, authToken, scriptName, username, startTime,
				getExperienceTracker().getGainedXP(Skill.COOKING), getExperienceTracker().getGainedLevels(Skill.COOKING),
				pizzasMade, pizzasCooked, profit);

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
			g.drawRect(10, 10, 150, 50);
			g.fillRect(10, 10, 150, 50);

			g.setColor(Color.WHITE);
			Font fontText = new Font("Arial", 0, 10);
			g.setFont(fontText);

			// Pizzas / Hr
			g.drawString(status, 15, 15);
			g.drawString("Pizzas per Hour", 15, 30);

			if (pizzasCooked > 0) {
				long timePassed = (new Date().getTime() / 1000) - startTime;
				int pizzasPerHour = (int) (pizzasCooked / (timePassed / 3600));
				g.drawString(NumberFormat.getInstance().format(pizzasPerHour), 15, 45);
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
	private void walkToRange() throws InterruptedException {
		status = "Walking to Range";
		getWalking().webWalk(rangeArea);
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

	private void withdrawCooking() throws InterruptedException {
		status = "Withdrawing uncooked pizzas";

		if (!getBank().isOpen()) {
			if (!myPlayer().isAnimating()) {
				new ConditionalSleep(5000) {
					public boolean condition() throws InterruptedException {
						return getBank().open();
					}
				}.sleep();
			}
		} else {
			int uncookedPizza = (int) getBank().getAmount("Uncooked pizza");

			if (uncookedPizza != 0) {
				getBank().withdrawAll("Uncooked pizza");
			} else {
				log("No more pizzas left to cook.");
				this.stop();
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
			RS2Object range = getObjects().closest(i -> i != null && i.getName().contains("Range"));

			if (range != null) {
				if (!getInventory().isItemSelected() && optionMenu == null) {
					if (getInventory().interact("Use", "Uncooked pizza")) {
						new ConditionalSleep(3000) {
							@Override
							public boolean condition() throws InterruptedException {
								return getInventory().isItemSelected();
							}
						}.sleep();
					}
				}

				if (optionMenu == null) {
					if (range.interact("Use")) {
						sleep(random(200, 400));
					}
				}

				if (optionMenu != null) {
					if (optionMenu.isVisible()) {
						if (optionMenu.interact("Cook All")) {
							new ConditionalSleep(25000) {
								@Override
								public boolean condition() throws InterruptedException {
									return (inventory.getAmount("Uncooked pizza") == 0);
								}
							}.sleep();
						}
					}
				}
			}
		}

		sleep(650);
	}

	private void walkToBank() throws InterruptedException {
		status = "Walking to Bank";
		getWalking().webWalk(bankToUse);
	}

	private void makeIncompletePizzas() throws InterruptedException {
		status = "Making Incomplete Pizzas";
		RS2Widget incompletePizzaMenu = getWidgets().getWidgetContainingText("Incomplete pizza");

		if (!myPlayer().isAnimating()) {
			makingIncompletePizzaCounter += 1;
		}

		if (makingIncompletePizzaCounter == 100) {
			makingIncompletePizzaCounter = 0;
			makingIncompletePizzas = false;
		}

		if (!makingIncompletePizzas) {
			if (makingIncompletePizzasSkip) {
				if (!getInventory().isItemSelected() && incompletePizzaMenu == null) {
					if (getInventory().interact("Use", "Pizza base")) {
						new ConditionalSleep(3000) {
							@Override
							public boolean condition() throws InterruptedException {
								return getInventory().isItemSelected();
							}
						}.sleep();
					}
				}

				if (incompletePizzaMenu == null) {
					if (inventory.getItem("Tomato").interact("Use")) {
						sleep(random(200, 400));
						makingIncompletePizzasSkip = false;
					}
				}
			}

			if (incompletePizzaMenu != null) {
				if (incompletePizzaMenu.isVisible()) {
					if (incompletePizzaMenu.interact("Make All")) {
						makingIncompletePizzaCounter = 0;
						makingIncompletePizzas = true;
						makingIncompletePizzasSkip = true;

						new ConditionalSleep(15000) {
							public boolean condition() {
								return (inventory.getAmount("Pizza base") == 0 || inventory.getAmount("Tomato") == 0);
							}
						}.sleep();
					}
				}
			}
		}
	}
	private void makeUncookedPizzas() throws InterruptedException {
		status = "Making Uncooked Pizzas";
		RS2Widget uncookedPizzaMenu = getWidgets().getWidgetContainingText("Uncooked pizza");

		if (!myPlayer().isAnimating()) {
			makingUncookedPizzaCounter += 1;
		}

		if (makingUncookedPizzaCounter == 100) {
			makingUncookedPizzaCounter = 0;
			makingUncookedPizzas = false;
		}

		if (!makingUncookedPizzas) {
			if (makingUncookedPizzasSkip) {
				if (!getInventory().isItemSelected() && uncookedPizzaMenu == null) {
					if (getInventory().interact("Use", "Incomplete pizza")) {
						new ConditionalSleep(3000) {
							@Override
							public boolean condition() throws InterruptedException {
								return getInventory().isItemSelected();
							}
						}.sleep();
					}
				}

				if (uncookedPizzaMenu == null) {
					if (inventory.getItem("Cheese").interact("Use")) {
						sleep(random(200, 400));
						makingUncookedPizzasSkip = false;
					}
				}
			}

			if (uncookedPizzaMenu != null) {
				if (uncookedPizzaMenu.isVisible()) {
					if (uncookedPizzaMenu.interact("Make All")) {
						makingUncookedPizzas = true;
						makingUncookedPizzasSkip = true;

						new ConditionalSleep(15000) {
							public boolean condition() {
								return (inventory.getAmount("Incomplete pizza") == 0 || inventory.getAmount("Cheese") == 0);
							}
						}.sleep();
					}
				}
			}
		}
	}

	private void withdrawMakingPizzas() throws InterruptedException {
		status = "Withdrawing items for pizza";
		String[] requiredItems = {"Pizza base", "Tomato", "Cheese"};

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

			if (inventory.isEmpty()) {
				int pizzaBase = (int) getBank().getAmount("Pizza base");
				int tomato = (int) getBank().getAmount("Tomato");
				int cheese = (int) getBank().getAmount("Cheese");

				if ((pizzaBase != 0) && (tomato != 0) && (cheese != 0)) {
					while (!readyToMakeUncookedPizzas(requiredItems)) {
						for (String name : requiredItems) {
							int bankAmount = (int) getBank().getAmount(name);

							if (bankAmount < 9) {
								if (bankAmount != 0) {
									getBank().withdrawAll(name);
								}
							} else {
								int invCount = (int) inventory.getAmount(name);

								if (invCount != 9) {
									getBank().withdraw(name, (9 - invCount));
								}
							}
						}
					}

					makePizzaUncookedGroup = true;
					makeIncompletePizzas = true;
					getBank().close();
				} else {
					log("No more items to make uncooked pizzas. Moving to cooking stage.");
					bank();
					mode = "Cook";
				}
			}
		}
	}

	private void makePizzaBase() throws InterruptedException {
		status = "Making Pizza Base";
		RS2Widget pastryMenu = getWidgets().get(219, 0, 3);
		RS2Widget pizzaBaseMenu = getWidgets().get(309,2);

		if (!myPlayer().isAnimating()) {
			makingPizzaBasesCounter += 1;
		}

		if (makingPizzaBasesCounter == 50) {
			makingPizzaBasesCounter = 0;
			makingPizzaBases = false;
		}

		if (!makingPizzaBases) {
			if (pizzaBaseMenu == null) {
				if (makingPizzaBaseSkip) {
					if (!getInventory().isItemSelected() && pastryMenu == null) {
						if (getInventory().interact("Use", "Bucket of water")) {
							new ConditionalSleep(3000) {
								@Override
								public boolean condition() throws InterruptedException {
									return getInventory().isItemSelected();
								}
							}.sleep();
						}
					}

					if (pastryMenu == null) {
						if (inventory.getItem("Pot of flour").interact("Use")) {
							sleep(random(200, 400));
						}
					}

					if (pastryMenu != null) {
						if (pastryMenu.isVisible()) {
							if (pastryMenu.interact()) {
								sleep(random(200, 400));
								makingPizzaBaseSkip = false;
							}
						}
					}
				}
			}

			if (pizzaBaseMenu != null) {
				if (pizzaBaseMenu.isVisible()) {
					if (pizzaBaseMenu.interact("Make All")) {
						makingPizzaBasesCounter = 0;
						makingPizzaBases = true;
						makingPizzaBaseSkip = true;

						new ConditionalSleep(15000) {
							public boolean condition() {
								return (inventory.getAmount("Bucket of water") == 0 || inventory.getAmount("Pot of flour") == 0);
							}
						}.sleep();
					}
				}
			}
		}
	}

	private void withdrawMakePizzaBase() throws InterruptedException {
		status = "Withdrawing items for pizza base";
		String[] requiredItems = {"Bucket of Water", "Pot of flour"};

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

			if (inventory.isEmpty()) {
				int bucketOfWater = (int) getBank().getAmount(requiredItems[0]);
				int potOfFlour = (int) getBank().getAmount(requiredItems[1]);

				if ((bucketOfWater != 0) && (potOfFlour != 0)) {
					while (!readyToMakePizzaBases(requiredItems)) {
						for (String name : requiredItems) {
							int bankAmount = (int) getBank().getAmount(name);

							if (bankAmount < 9) {
								if (bankAmount != 0) {
									getBank().withdrawAll(name);
								}
							} else {
								int invCount = (int) inventory.getAmount(name);

								if (invCount != 9) {
									getBank().withdraw(name, (9 - invCount));
								}
							}
						}
					}

					makePizzaDough = true;
					makePizzaBase = true;
					getBank().close();
				} else {
					log("No more items to make pizza bases. Moving to making uncooked pizza stage.");
					bank();
					makePizzas = true;
				}
			}
		}
	}

	/***********************
	 *
	 * Core functions
	 *
	 ***********************/
	private boolean readyToMakeUncookedPizzas(String[] requiredItems) {
		return inventory.getAmount(requiredItems[0]) > 0 && inventory.getAmount(requiredItems[1]) > 0 && inventory.getAmount(requiredItems[2]) > 0;
	}

	private boolean readyToMakePizzaBases(String[] requiredItems) {
		return inventory.getAmount(requiredItems[0]) > 0 && inventory.getAmount(requiredItems[1]) > 0;
	}

	private void updateCookedPizzasCounter() throws InterruptedException {
		long curInvCount = getInventory().getAmount("Plain pizza");

		if (curInvCount > cookedPizzaInv) {
			long difference = (curInvCount - cookedPizzaInv);
			profit += difference * pizzaCookedPrice;
			pizzasCooked += difference;
		}

		cookedPizzaInv = curInvCount;
	}
}