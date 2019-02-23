<?php
define("DB_HOST", "localhost");
define("DB_NAME", "");
define("DB_USERNAME", "");
define("DB_PASSWORD", "");

define("AES_256_CBC", "aes-256-cbc");
$availableScripts = array(
	1 => "testPlatform",
	2 => "engineerFishing",
	3 => "engineerPickpocket",
	4 => "engineerEssenceMiner",
	5 => "engineerCooker",
	6 => "engineerRunecrafter",
	7 => "engineerPizzaMaker",
	7 => "engineerShopBuyer"
);

$scriptAccessKey = array(
	"testPlatform" => "",
	"engineerFishing" => "",
	"engineerPickpocket" => "",
	"engineerEssenceMiner" => "",
	"engineerCooker" => "",
	"engineerRunecrafter" => "",
	"engineerPizzaMaker" => "",
	"engineerShopBuyer" => ""
);

$scriptAuthToken = array(
	"testPlatform" => "",
	"engineerFishing" => "",
	"engineerPickpocket" => "",
	"engineerEssenceMiner" => "",
	"engineerCooker" => "",
	"engineerRunecrafter" => "",
	"engineerPizzaMaker" => "",
	"engineerShopBuyer" => ""
);

define("SITENAME", "Engineer Scripts");
define("BASEURL", "apiserver/app/osb/");
define("SITEDESC", "Engineer Scripts for OSB by Tyler of MMA Engineer.");
define("AUTHOR", "Engineer");

date_default_timezone_set("UTC");

ini_set('display_errors',1);
error_reporting(E_ALL|E_STRICT);
ini_set('error_log','script_errors.log');
ini_set('log_errors','On');
?>