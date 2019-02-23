<?php

require_once "../assets/inc/config.php";
require_once "../assets/inc/database.php";
require_once "../assets/inc/encryption.php";

$encryption = new encryption();
$returnArray["success"] = false; 

if (isset($_REQUEST["accessKey"], $_REQUEST["authToken"])) {
	if (isset($_REQUEST["scriptName"], $_REQUEST["username"], $_REQUEST["updateTime"], $_REQUEST["runtime"], $_REQUEST["updateMethod"])) {$scriptName = $_REQUEST["scriptName"];
		$username = $_REQUEST["username"];
		$updateTime = $_REQUEST["updateTime"];
		$runtime = $_REQUEST["runtime"];
		$updateMethod = $_REQUEST["updateMethod"];
			
		if (($encryption->decrypt256Bit($_REQUEST["accessKey"]) == $encryption->decrypt256Bit($scriptAccessKey[$scriptName])) && ($encryption->decrypt256Bit($_REQUEST["authToken"]) == $encryption->decrypt256Bit($scriptAuthToken[$scriptName]))) {
			$db = new database();
			
			if ($updateMethod == "online") {
				if (in_array($scriptName, $availableScripts)) {
					$db->query("SELECT `id` FROM `onlineUsers` WHERE `username` = :username");
					$db->bind(":username", $username);
							
					if ($db->execute()) {
						$rows = $db->rowCount();

						if ($rows == 1) {
							$db->query("UPDATE `onlineUsers` SET `script_name` = :script_name, `update_time` = :update_time, `runtime` = :runtime WHERE `username` = :username");
						} else {
							$db->query("INSERT INTO `onlineUsers` (`id`, `username`, `script_name`, `update_time`, `runtime`) VALUES(0, :username, :script_name, :update_time, :runtime)");
						}
						
						$db->bind(":username", $username);
						$db->bind(":script_name", $scriptName);
						$db->bind(":update_time", $updateTime);
						$db->bind(":runtime", $runtime);
						
						if (!$db->execute()) {
							$returnArray["message"] = "Failed to add you to the list of online users."; 
						}
						
						$returnArray["success"] = true; 
					} else {
						$returnArray["message"] = "Failed to access our database for adding you to the online users."; 
					}
				} else {
					$returnArray["message"] = "You do not have a script that supports the online users function."; 
				}
			} else if ($updateMethod == "offline") {
				$db->query("UPDATE `onlineUsers` SET `update_time` = 0, `runtime` = 0 WHERE `username` = :username");
				$db->bind(":username", $username);
						
				if (!$db->execute()) {
					$returnArray["message"] = "Failed to access our database for removing you from the online users."; 
				}
				
				$returnArray["success"] = true; 
			}
		} else {
			$returnArray["message"] = "Bad access key or authorization token for utilizing the online users function."; 
		}
	} else {
		$returnArray["access"] = "We did not receive all of the parameters to access the online users function."; 
	}
} else {
	$returnArray["access"] = "We did not receive the access key or authorization token to access the online users function."; 
}

echo json_encode($returnArray);

?>