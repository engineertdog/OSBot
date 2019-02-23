<?php

require_once "../assets/inc/config.php";
require_once "../assets/inc/database.php";
require_once "../assets/inc/encryption.php";

$encryption = new encryption();
$returnArray["success"] = false; 

if (isset($_REQUEST["accessKey"], $_REQUEST["authToken"], $_REQUEST["scriptName"], $_REQUEST["username"], $_REQUEST["runtime"], $_REQUEST["xpGained"], $_REQUEST["levelsGained"], $_REQUEST["fishCaught"], $_REQUEST["profit"])) {
	$scriptName = $_REQUEST["scriptName"];
	$username = $_REQUEST["username"];
	$runtime = $_REQUEST["runtime"];
	$xpGained = $_REQUEST["xpGained"];
	$levelsGained = $_REQUEST["levelsGained"];
	$fishCaught = $_REQUEST["fishCaught"];
	$profit = $_REQUEST["profit"];
		
	if (($encryption->decrypt256Bit($_REQUEST["accessKey"]) == $encryption->decrypt256Bit($scriptAccessKey[$scriptName])) && ($encryption->decrypt256Bit($_REQUEST["authToken"]) == $encryption->decrypt256Bit($scriptAuthToken[$scriptName]))) {
		if ($scriptName == "engineerFishing") {
			$db = new database();
			$commit = false;
			
			$db->query("SELECT `id` FROM `engineerFishing` WHERE `username` = :username");
			$db->bind(":username", $username);
				
			if ($db->execute()) {
				$rows = $db->rowCount();
				$db->beginTransaction();

				if ($rows == 1) {
					$db->query("UPDATE `engineerFishing` SET `runtime` = runtime+:runtime, `xp_gained` = xp_gained+:xp_gained, `levels_gained` = levels_gained+:levels_gained, `fish_caught` = fish_caught+:fish_caught, `profit` = profit+:profit WHERE `username` = :username");
				} else {
					$db->query("INSERT INTO `engineerFishing` (`id`, `username`, `runtime`, `xp_gained`, `levels_gained`, `fish_caught`, `profit`) VALUES(0, :username, :runtime, :xp_gained, :levels_gained, :fish_caught, :profit)");
				}
				
				$db->bind(":username", $username);
				$db->bind(":runtime", $runtime);
				$db->bind(":xp_gained", $xpGained);
				$db->bind(":levels_gained", $levelsGained);
				$db->bind(":fish_caught", $fishCaught);
				$db->bind(":profit", $profit);
				
				if ($db->execute()) {
					$db->query("UPDATE `skills` SET `fishing_xp` = fishing_xp+:fishing_xp, `fishing_levels` = fishing_levels+:fishing_levels WHERE `id` = 1");
					$db->bind(":fishing_xp", $xpGained);
					$db->bind(":fishing_levels", $levelsGained);
					
					if ($db->execute()) {
						$db->query("SELECT `id` FROM `userStats` WHERE `username` = :username");
						$db->bind(":username", $username);
							
						if ($db->execute()) {
							$rows = $db->rowCount();

							if ($rows == 1) {
								$db->query("UPDATE `userStats` SET `runtime` = runtime+:runtime, `profit` = profit+:profit WHERE `username` = :username");
							} else {
								$db->query("INSERT INTO `userStats` (`id`, `username`, `runtime`, `profit`) VALUES (0, :username, :runtime, :profit)");
							}
							
							$db->bind(":runtime", $runtime);
							$db->bind(":profit", $profit);
							$db->bind(":username", $username);
							
							if ($db->execute()) {
								$commit = true;
							}
						}
					}
				}
						
				if ($commit) {
					$returnArray["success"] = true; 
					$db->endTransaction();
				} else {
					$returnArray["message"] = "Failed to save your data for the Engineer Fishing script."; 
					$db->cancelTransaction();
				}
			} else {
				$returnArray["message"] = "Failed to access our database to save your data for the Engineer Fishing script."; 
			}
		} else {
			$returnArray["message"] = "The script you are trying to save your data to does not match our database."; 
		}
	} else {
		$returnArray["message"] = "Bad access key or authorization token for utilizing the online users function."; 
	}
} else {
	$returnArray["access"] = "We did not receive all of the parameters to save your data for the Engineer Fishing script."; 
}

echo json_encode($returnArray);

?>