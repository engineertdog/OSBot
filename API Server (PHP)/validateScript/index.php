<?php

require_once "../assets/inc/config.php";
require_once "../assets/inc/database.php";
require_once "../assets/inc/encryption.php";

$encryption = new encryption();
$returnArray["access"] = "failed"; 

if (isset($_REQUEST["accessKey"], $_REQUEST["authToken"])) {
	if (isset($_REQUEST["scriptName"], $_REQUEST["username"], $_REQUEST["userKey"])) {
		$scriptName = $_REQUEST["scriptName"];
		$username = $_REQUEST["username"];
		$userKey = $_REQUEST["userKey"];
			
		if (($encryption->decrypt256Bit($_REQUEST["accessKey"]) == $encryption->decrypt256Bit($scriptAccessKey[$scriptName])) && ($encryption->decrypt256Bit($_REQUEST["authToken"]) == $encryption->decrypt256Bit($scriptAuthToken[$scriptName]))) {
			$db = new database();
			
			if (in_array($scriptName, $availableScripts)) {
				$db->query("SELECT `script_name`, `user_key`, `expires` FROM `validateScript` WHERE `username` = :username");
				$db->bind(":username", $username);
					
				if ($db->execute()) {
					$rows = $db->rowCount();

					if ($rows == 1) {
						$userInfo = $db->single();
						
						if ($scriptName == $userInfo["script_name"]) {
							if ($encryption->decrypt256Bit($userKey) == $encryption->decrypt256Bit($userInfo["user_key"])) {
								if (time() < $userInfo["expires"]) {
									$returnArray["access"] = "valid"; 
								} else {
									$returnArray["access"] = "Your access to " . $scriptName . " has expired."; 
								}
							} else {
								$returnArray["access"] = "We could not validate your user key against our database."; 
							}
						} else {
							$returnArray["access"] = "You have an invalid script name for the access key you're trying to use."; 
						}
					} else {
						$returnArray["access"] = "You do not have access to the script: " . $scriptName . "."; 
					}
				} else {
					$returnArray["access"] = "We could not access our database to validate your use of the script: " . $scriptName . "."; 
				}
			} else {
				$returnArray["access"] = "You have an invalid script name for the access key you're trying to use."; 
			}
		} else {
			$returnArray["access"] = "You have an invalid access key or authorization token to use the script: " . $scriptName . "."; 
		}
	} else {
		$returnArray["access"] = "We did not receive all of the parameters to validate your use of the script."; 
	}
} else {
	$returnArray["access"] = "We did not receive the access key or authorization token to validate your use of the script."; 
}
						
echo json_encode($returnArray);

?>