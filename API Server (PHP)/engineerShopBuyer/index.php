<?php

require_once "../assets/inc/config.php";
require_once "../assets/inc/database.php";
require_once "../assets/inc/encryption.php";

$encryption = new encryption();
$returnArray["success"] = false; 

if (isset($_REQUEST["accessKey"], $_REQUEST["authToken"], $_REQUEST["scriptName"], $_REQUEST["username"], $_REQUEST["runtime"], $_REQUEST["itemsBought"])) {
	$scriptName = $_REQUEST["scriptName"];
	$username = $_REQUEST["username"];
	$runtime = $_REQUEST["runtime"];
	$itemsBought = $_REQUEST["itemsBought"];
		
	if (($encryption->decrypt256Bit($_REQUEST["accessKey"]) == $encryption->decrypt256Bit($scriptAccessKey[$scriptName])) && ($encryption->decrypt256Bit($_REQUEST["authToken"]) == $encryption->decrypt256Bit($scriptAuthToken[$scriptName]))) {
		if ($scriptName == "engineerShopBuyer") {
			$db = new database();
			$commit = false;
			
			$db->query("SELECT `id` FROM `engineerShopBuyer` WHERE `username` = :username");
			$db->bind(":username", $username);
				
			if ($db->execute()) {
				$rows = $db->rowCount();
				$db->beginTransaction();

				if ($rows == 1) {
					$db->query("UPDATE `engineerShopBuyer` SET `runtime` = runtime+:runtime, `items_bought` = items_bought+:items_bought WHERE `username` = :username");
				} else {
					$db->query("INSERT INTO `engineerShopBuyer` (`id`, `username`, `runtime`, `items_bought`) VALUES(0, :username, :runtime, :items_bought)");
				}
				
				$db->bind(":username", $username);
				$db->bind(":runtime", $runtime);
				$db->bind(":items_bought", $itemsBought);
				
				if ($db->execute()) {
					$db->query("SELECT `id` FROM `userStats` WHERE `username` = :username");
					$db->bind(":username", $username);
						
					if ($db->execute()) {
						$rows = $db->rowCount();

						if ($rows == 1) {
							$db->query("UPDATE `userStats` SET `runtime` = runtime+:runtime WHERE `username` = :username");
						} else {
							$db->query("INSERT INTO `userStats` (`id`, `username`, `runtime`, `profit`) VALUES (0, :username, :runtime, 0)");
						}
						
						$db->bind(":runtime", $runtime);
						$db->bind(":username", $username);
						
						if ($db->execute()) {
							$commit = true;
						}
					}
				}
						
				if ($commit) {
					$returnArray["success"] = true; 
					$db->endTransaction();
				} else {
					$returnArray["message"] = "Failed to save your data for the Engineer Shop Buyer script."; 
					$db->cancelTransaction();
				}
			} else {
				$returnArray["message"] = "Failed to access our database to save your data for the Engineer Shop Buyer script."; 
			}
		} else {
			$returnArray["message"] = "The script you are trying to save your data to does not match our database."; 
		}
	} else {
		$returnArray["message"] = "Bad access key or authorization token for utilizing the online users function."; 
	}
} else {
	$returnArray["access"] = "We did not receive all of the parameters to save your data for the Engineer Shop Buyer script."; 
}

echo json_encode($returnArray);

?>