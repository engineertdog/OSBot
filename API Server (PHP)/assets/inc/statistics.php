<?php
if (count(get_included_files()) <= 1) {
	exit;
}

require_once dirname(__FILE__). "/config.php";
require_once dirname(__FILE__). "/database.php";
require_once dirname(__FILE__). "/encryption.php";

class statistics extends database {
	public $sitename	      	= SITENAME;
	public $baseURL      		= BASEURL;
	public $siteDesc 			= SITEDESC;
	
	public function grabTotalStats() {
		$this->query("SELECT * FROM `skills`");
		$totalXP = $totalLevels = $runtime = $profit = $usersOnline = 0;
		
		if ($this->execute()) {
			$skillInfo = $this->single();
			
			$totalXP = $skillInfo["agility_xp"] + $skillInfo["attack_xp"] + $skillInfo["construction_xp"] + $skillInfo["cooking_xp"] + $skillInfo["crafting_xp"] + $skillInfo["defense_xp"] + $skillInfo["farming_xp"] + $skillInfo["firemaking_xp"] 
				+ $skillInfo["fishing_xp"] + $skillInfo["fletching_xp"] + $skillInfo["herblore_xp"] + $skillInfo["hitpoints_xp"] + $skillInfo["hunter_xp"] + $skillInfo["magic_xp"] + $skillInfo["mining_xp"] + $skillInfo["prayer_xp"] 
				+ $skillInfo["ranged_xp"] + $skillInfo["runecrafting_xp"] + $skillInfo["slayer_xp"] + $skillInfo["smithing_xp"] + $skillInfo["strength_xp"] + $skillInfo["thieving_xp"] + $skillInfo["woodcutting_xp"];
			$totalLevels = $skillInfo["agility_levels"] + $skillInfo["attack_levels"] + $skillInfo["construction_levels"] + $skillInfo["cooking_levels"] + $skillInfo["crafting_levels"] + $skillInfo["defense_levels"] + $skillInfo["farming_levels"] 
				+ $skillInfo["firemaking_levels"] + $skillInfo["fishing_levels"] + $skillInfo["fletching_levels"] + $skillInfo["herblore_levels"] + $skillInfo["hitpoints_levels"] + $skillInfo["hunter_levels"] + $skillInfo["magic_levels"] 
				+ $skillInfo["mining_levels"] + $skillInfo["prayer_levels"] + $skillInfo["ranged_levels"] + $skillInfo["runecrafting_levels"] + $skillInfo["slayer_levels"] + $skillInfo["smithing_levels"] + $skillInfo["strength_levels"]
				+ $skillInfo["thieving_levels"] + $skillInfo["woodcutting_levels"];
				
			$this->query("SELECT SUM(`runtime`) AS 'runtime', SUM(`profit`) AS 'profit' FROM `userStats`");
			
			if ($this->execute()) {
				$userStats = $this->single();
				
				$runtime = $this->secondsToTime($userStats["runtime"]);
				$profit = number_format($userStats["profit"]);
				
				$this->query("SELECT COUNT(`id`) AS 'usersOnline' FROM `onlineUsers` WHERE `update_time` > ((UNIX_TIMESTAMP() - 300))");
			
				if ($this->execute()) {
					$onlineUsers = $this->single();
					
					$usersOnline = number_format($onlineUsers["usersOnline"]);
				}
			}
		}
		
		$returnArray["xpGained"] = number_format($totalXP);
		$returnArray["levelsGained"] = number_format($totalLevels);
		$returnArray["runtime"] = $runtime;
		$returnArray["profit"] = $profit;
		$returnArray["usersOnline"] = $usersOnline;
		$returnArray["usersOnlineText"] = $this->formatUsers($usersOnline);;
		
		return $returnArray;
	}
	
	public function grabAllScripts() {
		$engineerCooker = array(
			"scriptName"	=> "Engineer Cooker",
			"numUsers" 		=> 0,
			"numUsersText" 	=> "Users",
			"runtime" 		=> 0,
			"xpGained" 		=> 0,
			"levelsGained" 	=> 0,
			"itemsCooked" 	=> 0,
			"profit" 		=> 0
		);
		$engineerEssenceMiner = array(
			"scriptName"	=> "Engineer Essence Miner",
			"numUsers" 		=> 0,
			"numUsersText" 	=> "Users",
			"runtime" 		=> 0,
			"xpGained" 		=> 0,
			"levelsGained" 	=> 0,
			"essenceMined" 	=> 0,
			"profit" 		=> 0
		);
		$engineerFishing = array(
			"scriptName"	=> "Engineer Fishing",
			"numUsers" 		=> 0,
			"numUsersText" 	=> "Users",
			"runtime" 		=> 0,
			"xpGained" 		=> 0,
			"levelsGained"	=> 0,
			"fishCaught" 	=> 0,
			"profit" 		=> 0
		);
		$engineerPickpocket = array(
			"scriptName"		=> "Engineer Pickpocket",
			"numUsers" 			=> 0,
			"numUsersText" 		=> "Users",
			"runtime" 			=> 0,
			"xpGained" 			=> 0,
			"levelsGained" 		=> 0,
			"pocketsPicked" 	=> 0,
			"stunned" 			=> 0,
			"deaths" 			=> 0,
			"profit" 			=> 0
		);
		$engineerRunecrafter = array(
			"scriptName"	=> "Engineer Runecrafter",
			"numUsers" 		=> 0,
			"numUsersText" 	=> "Users",
			"runtime" 		=> 0,
			"xpGained" 		=> 0,
			"levelsGained"	=> 0,
			"runesCrafted" 	=> 0,
			"profit" 		=> 0
		);
		
		$this->query("SELECT COUNT(`id`) AS 'numUsers', SUM(`runtime`) AS 'runtime', SUM(`xp_gained`) AS 'xp_gained', SUM(`levels_gained`) AS 'levels_gained', SUM(`items_cooked`) AS 'items_cooked', SUM(`profit`) AS 'profit' FROM `engineerCooker`");
		
		if ($this->execute()) {
			$userStats = $this->single();
			
			$engineerCooker["numUsers"] = number_format($userStats["numUsers"]);
			$engineerCooker["numUsersText"] = $this->formatUsers($userStats["numUsers"]);
			$engineerCooker["runtime"] = $this->secondsToTime($userStats["runtime"]);
			$engineerCooker["xpGained"] = number_format($userStats["xp_gained"]);
			$engineerCooker["levelsGained"] = number_format($userStats["levels_gained"]);
			$engineerCooker["itemsCooked"] = number_format($userStats["items_cooked"]);
			$engineerCooker["profit"] = number_format($userStats["profit"]);
		}
		
		$this->query("SELECT COUNT(`id`) AS 'numUsers', SUM(`runtime`) AS 'runtime', SUM(`xp_gained`) AS 'xp_gained', SUM(`levels_gained`) AS 'levels_gained', SUM(`essence_mined`) AS 'essence_mined', SUM(`profit`) AS 'profit' FROM `engineerEssenceMiner`");
		
		if ($this->execute()) {
			$userStats = $this->single();
			
			$engineerEssenceMiner["numUsers"] = number_format($userStats["numUsers"]);
			$engineerEssenceMiner["numUsersText"] = $this->formatUsers($userStats["numUsers"]);
			$engineerEssenceMiner["runtime"] = $this->secondsToTime($userStats["runtime"]);
			$engineerEssenceMiner["xpGained"] = number_format($userStats["xp_gained"]);
			$engineerEssenceMiner["levelsGained"] = number_format($userStats["levels_gained"]);
			$engineerEssenceMiner["essenceMined"] = number_format($userStats["essence_mined"]);
			$engineerEssenceMiner["profit"] = number_format($userStats["profit"]);
		}
		
		$this->query("SELECT COUNT(`id`) AS 'numUsers', SUM(`runtime`) AS 'runtime', SUM(`xp_gained`) AS 'xp_gained', SUM(`levels_gained`) AS 'levels_gained', SUM(`fish_caught`) AS 'fish_caught', SUM(`profit`) AS 'profit' FROM `engineerFishing`");
		
		if ($this->execute()) {
			$userStats = $this->single();
			
			$engineerFishing["numUsers"] = number_format($userStats["numUsers"]);
			$engineerFishing["numUsersText"] = $this->formatUsers($userStats["numUsers"]);
			$engineerFishing["runtime"] = $this->secondsToTime($userStats["runtime"]);
			$engineerFishing["xpGained"] = number_format($userStats["xp_gained"]);
			$engineerFishing["levelsGained"] = number_format($userStats["levels_gained"]);
			$engineerFishing["fishCaught"] = number_format($userStats["fish_caught"]);
			$engineerFishing["profit"] = number_format($userStats["profit"]);
		}
		
		$this->query("SELECT COUNT(`id`) AS 'numUsers', SUM(`runtime`) AS 'runtime', SUM(`xp_gained`) AS 'xp_gained', SUM(`levels_gained`) AS 'levels_gained', SUM(`pockets_picked`) AS 'pockets_picked', SUM(`stunned`) AS 'stunned', SUM(`deaths`) AS 'deaths', SUM(`profit`) AS 'profit' FROM `engineerPickpocket`");
		
		if ($this->execute()) {
			$userStats = $this->single();
			
			$engineerPickpocket["numUsers"] = number_format($userStats["numUsers"]);
			$engineerPickpocket["numUsersText"] = $this->formatUsers($userStats["numUsers"]);
			$engineerPickpocket["runtime"] = $this->secondsToTime($userStats["runtime"]);
			$engineerPickpocket["xpGained"] = number_format($userStats["xp_gained"]);
			$engineerPickpocket["levelsGained"] = number_format($userStats["levels_gained"]);
			$engineerPickpocket["pocketsPicked"] = number_format($userStats["pockets_picked"]);
			$engineerPickpocket["stunned"] = number_format($userStats["stunned"]);
			$engineerPickpocket["deaths"] = number_format($userStats["deaths"]);
			$engineerPickpocket["profit"] = number_format($userStats["profit"]);
		}
		
		$this->query("SELECT COUNT(`id`) AS 'numUsers', SUM(`runtime`) AS 'runtime', SUM(`xp_gained`) AS 'xp_gained', SUM(`levels_gained`) AS 'levels_gained', SUM(`runes_crafted`) AS 'runes_crafted', SUM(`profit`) AS 'profit' FROM `engineerRunecrafter`");
		
		if ($this->execute()) {
			$userStats = $this->single();
			
			$engineerRunecrafter["numUsers"] = number_format($userStats["numUsers"]);
			$engineerRunecrafter["numUsersText"] = $this->formatUsers($userStats["numUsers"]);
			$engineerRunecrafter["runtime"] = $this->secondsToTime($userStats["runtime"]);
			$engineerRunecrafter["xpGained"] = number_format($userStats["xp_gained"]);
			$engineerRunecrafter["levelsGained"] = number_format($userStats["levels_gained"]);
			$engineerRunecrafter["runesCrafted"] = number_format($userStats["runes_crafted"]);
			$engineerRunecrafter["profit"] = number_format($userStats["profit"]);
		}
		
		$returnArray = array(
			"engineerCooker" 		=> $engineerCooker,
			"engineerEssenceMiner" 	=> $engineerEssenceMiner,
			"engineerFishing" 		=> $engineerFishing,
			"engineerPickpocket"	=> $engineerPickpocket,
			"engineerRunecrafter"	=> $engineerRunecrafter
		);
		
		return $returnArray;
	}
	
	private function formatUsers($numUsers) {
		if ($numUsers == 1) {
			return "User";
		} else {
			return "Users";
		}
	}
	
	private function secondsToTime($seconds) {
		$y = floor($seconds / (86400 * 365.25));
		$d = floor(($seconds - ($y * (86400 * 365.25))) / 86400);
		$h = gmdate('H', $seconds);
		$m = gmdate('i', $seconds);
		$s = gmdate('s', $seconds);

		$string = '';

		if ($y > 0) {
			$yw = $y > 1 ? " years, " : " year, ";
			$string .= $y . $yw;
		}

		if ($d > 0) {
			$dw = $d > 1 ? " days, " : " day, ";
			$string .= $d . $dw;
		}

		if ($h > 0) {
			$hw = $h > 1 ? " hours, " : " hour, ";
			$string .= $h . $hw;
		}

		if ($m > 0) {
			$mw = $m > 1 ? " minutes, " : " minute, ";
			$string .= $m . $mw;
		}

		if ($s > 0) {
			$sw = $s > 1 ? " seconds " : " second ";
			$string .= $s . $sw;
		}

		return preg_replace("/\s+/", " ", $string);
	}
}
?>