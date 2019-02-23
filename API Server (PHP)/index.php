<?php
require_once "./assets/inc/statistics.php";

$statistics = new statistics();
$totalStats = $statistics->grabTotalStats();
$scriptStats = $statistics->grabAllScripts();
?>

<!DOCTYPE html>
<html lang='en'>
	<head>
		<meta charset='utf-8' />
		<title><?php echo SITENAME; ?></title>
		<meta http-equiv='X-UA-Compatible' content='IE=edge'>
		<meta content='width=device-width, initial-scale=1' name='viewport' />
		<meta content='<?php echo SITEDESC; ?>' name='description' />
		<meta content='<?php echo AUTHOR; ?>' name='author' />
		
		<link href='./assets/global/plugins/font-awesome/css/font-awesome.min.css' rel='stylesheet' type='text/css' />
		<link href='./assets/global/plugins/bootstrap/css/bootstrap.min.css' rel='stylesheet' type='text/css' />
		<link href='./assets/global/css/components.css' rel='stylesheet' id='style_components' type='text/css' />
		<link href='./assets/global/css/custom.css' rel='stylesheet' id='style_components' type='text/css' />
		<link href='./assets/pages/css/pricing.css' rel='stylesheet' type='text/css' />
	</head>

    <body>
		<div class='container'>
			<div class='row'>
				<img id='logo' src='./assets/images/logo.png' class='img-responsive' alt='Engineer Scripts Logo' />
			</div>
			
			<div class='row'>			
				<div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
					<div class='dashboard-stat2 bordered'>
						<div class='display'>
							<div class='number'>
								<h3 class='font-blue-sharp'>
									<?php echo $totalStats["runtime"]; ?>
								</h3>
								<small>RUNTIME</small>
							</div>
							<div class='icon'>
								<i class='fa fa-clock-o'></i>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class='row'>
				<div class='col-lg-6 col-md-6 col-sm-6 col-xs-12'>
					<div class='dashboard-stat2 bordered'>
						<div class='display'>
							<div class='number'>
								<h3 class='font-green-sharp'>
									<?php echo $totalStats["xpGained"]; ?>
								</h3>
								<small>XP GAINED</small>
							</div>
							<div class='icon'>
								<i class='fa fa-plus'></i>
							</div>
						</div>
					</div>
				</div>
				<div class='col-lg-6 col-md-6 col-sm-6 col-xs-12'>
					<div class='dashboard-stat2 bordered'>
						<div class='display'>
							<div class='number'>
								<h3 class='font-red-haze'>
									<?php echo $totalStats["levelsGained"]; ?>
								</h3>
								<small>LEVELS GAINED</small>
							</div>
							<div class='icon'>
								<i class='fa fa-level-up'></i>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class='row'>
				<div class='col-lg-6 col-md-6 col-sm-6 col-xs-12'>
					<div class='dashboard-stat2 bordered'>
						<div class='display'>
							<div class='number'>
								<h3 class='font-purple-soft'>
									<small class='font-purple-soft'>$</small> <?php echo $totalStats["profit"]; ?>
								</h3>
								<small>PROFIT</small>
							</div>
							<div class='icon'>
								<i class='fa fa-usd'></i>
							</div>
						</div>
					</div>
				</div>
				<div class='col-lg-6 col-md-6 col-sm-6 col-xs-12'>
					<div class='dashboard-stat2 bordered'>
						<div class='display'>
							<div class='number'>
								<h3 class='font-dark'>
									<?php echo $totalStats["usersOnline"]; ?>
								</h3>
								<small><?php echo $totalStats["usersOnlineText"]; ?> ONLINE</small>
							</div>
							<div class='icon'>
								<i class='fa fa-usd'></i>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			<div class='portlet light'>
				<div class='portlet-body'>
					<div class='pricing-content-1'>
						<div class='row'>
							<div class='col-md-4'>
								<div class='price-column-container border-active'>
									<div class='price-table-head bg-blue'>
										<h2 class='no-margin'><?php echo $scriptStats["engineerFishing"]["scriptName"]; ?></h2>
									</div>
									<div class='arrow-down border-top-blue'></div>
									<div class='price-table-pricing'>
										<h3>
											<img src='./assets/images/fishing_icon.png' />
										</h3>
										<div class='price-ribbon'>Popular</div>
									</div>
									<div class='price-table-content'>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-user'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerFishing"]["numUsers"] . " " . $scriptStats["engineerFishing"]["numUsersText"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-clock-o'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerFishing"]["runtime"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-plus'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerFishing"]["xpGained"]; ?> XP Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-level-up'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerFishing"]["levelsGained"]; ?> Levels Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-hashtag'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerFishing"]["fishCaught"]; ?> Fish Caught</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-usd'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerFishing"]["profit"]; ?> Profit</div>
										</div>
									</div>
								</div>
							</div>
							
							<div class='col-md-4'>
								<div class='price-column-container border-active'>
									<div class='price-table-head bg-red'>
										<h2 class='no-margin'><?php echo $scriptStats["engineerPickpocket"]["scriptName"]; ?></h2>
									</div>
									<div class='arrow-down border-top-red'></div>
									<div class='price-table-pricing'>
										<h3>
											<img src='./assets/images/thieving_icon.png' />
										</h3>
									</div>
									<div class='price-table-content'>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-user'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["numUsers"] . " " . $scriptStats["engineerCooker"]["numUsersText"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-clock-o'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["runtime"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-plus'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["xpGained"]; ?> XP Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-level-up'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["levelsGained"]; ?> Levels Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-hashtag'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["pocketsPicked"]; ?> Pockets Picked</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-hashtag'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["stunned"]; ?> Stuns</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-hashtag'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["deaths"]; ?> Deaths</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-usd'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerPickpocket"]["profit"]; ?> Profit</div>
										</div>
									</div>
								</div>
							</div>
							
							<div class='col-md-4'>
								<div class='price-column-container border-active'>
									<div class='price-table-head bg-green'>
										<h2 class='no-margin'><?php echo $scriptStats["engineerEssenceMiner"]["scriptName"]; ?></h2>
									</div>
									<div class='arrow-down border-top-green'></div>
									<div class='price-table-pricing'>
										<h3>
											<img src='./assets/images/mining_icon.png' />
										</h3>
									</div>
									<div class='price-table-content'>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-user'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerEssenceMiner"]["numUsers"] . " " . $scriptStats["engineerCooker"]["numUsersText"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-clock-o'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerEssenceMiner"]["runtime"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-plus'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerEssenceMiner"]["xpGained"]; ?> XP Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-level-up'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerEssenceMiner"]["levelsGained"]; ?> Levels Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-hashtag'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerEssenceMiner"]["essenceMined"]; ?> Essence Mined</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-usd'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerEssenceMiner"]["profit"]; ?> Profit</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						
						<div class='spacer'></div>
						
						<div class='row'>
							<div class='col-md-4'>
								<div class='price-column-container border-active'>
									<div class='price-table-head bg-purple'>
										<h2 class='no-margin'><?php echo $scriptStats["engineerCooker"]["scriptName"]; ?></h2>
									</div>
									<div class='arrow-down border-top-purple'></div>
									<div class='price-table-pricing'>
										<h3>
											<img src='./assets/images/cooking_icon.png' />
										</h3>
										<div class='price-ribbon'>Popular</div>
									</div>
									<div class='price-table-content'>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-user'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerCooker"]["numUsers"] . " " . $scriptStats["engineerCooker"]["numUsersText"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-clock-o'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerCooker"]["runtime"]; ?></div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-plus'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerCooker"]["xpGained"]; ?> XP Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-level-up'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerCooker"]["levelsGained"]; ?> Levels Gained</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-hashtag'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerCooker"]["itemsCooked"]; ?> Items Cooked</div>
										</div>
										<div class='row mobile-padding'>
											<div class='col-xs-3 text-right mobile-padding'>
												<i class='fa fa-usd'></i>
											</div>
											<div class='col-xs-9 text-left mobile-padding'><?php echo $scriptStats["engineerCooker"]["profit"]; ?> Profit</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
    </body>
</html>