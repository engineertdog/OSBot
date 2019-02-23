package com.mmaengineer.engineerWalker;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.map.constants.Banks;

@ScriptManifest(author = "Tyler", info = "Walking script by Tyler", name = "Engineer Walker",
version = 1.0, logo = "apiserver/app/osb/engineerFishing/image/engineerFishingLogo.png")
public class Main extends Script {
	/***********************
	 *
	 * API default functions
	 *
	 ***********************/
	@Override
	public void onStart() throws InterruptedException {
		log("Welcome to Engineer Walker by Tyler.");
	}

	private enum State {
		WALK
	};

	private State getState() throws InterruptedException {
		return State.WALK;
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
			case WALK:
				walk();
				break;
		}

		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Thanks for running Engineer Walker!");
	}


	/***********************
	 *
	 * State functions
	 *
	 ***********************/
	private void walk() throws InterruptedException {
		getWalking().webWalk(Banks.GNOME_STRONGHOLD);
	}
}