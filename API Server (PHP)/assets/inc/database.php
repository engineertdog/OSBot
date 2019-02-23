<?php
if (count(get_included_files()) <= 1) {
	exit;
}

class database {
	private $host      	= DB_HOST;
	private $dbhname    = DB_NAME;
	private $user      	= DB_USERNAME;
	private $pass      	= DB_PASSWORD;
 
	private $dbh;
	private $stmt;
	private $error;
	
	public function __construct() {
		// Set Connection
		$con = 'mysql:host=' . $this->host . ';dbname=' . $this->dbhname;
		
		// Set options
		// If you want to display errors, use the following values
		// true 
		// ERRMODE_EXCEPTION
		$options = array(
			PDO::ATTR_PERSISTENT    => false,
			PDO::ATTR_ERRMODE       => PDO::ERRMODE_SILENT
		);
		
		// Create a new PDO instanace
		try {
			$this->dbh = new PDO($con, $this->user, $this->pass, $options);
		} catch(PDOException $e) {
			$this->error = $e->getMessage();
		}
	}
	
	public function query($query) {
		$this->stmt = $this->dbh->prepare($query);
	}
	
	public function bind($param, $value, $type = null) {
		if (is_null($type)) {
			switch (true) {
				case is_int($value):
					$type = PDO::PARAM_INT;
					break;
				case is_bool($value):
					$type = PDO::PARAM_BOOL;
					break;
				case is_null($value):
					$type = PDO::PARAM_NULL;
					break;
				default:
					$type = PDO::PARAM_STR;
			}
		}
		
		$this->stmt->bindValue($param, $value, $type);
	}
	
	public function execute() {
		return $this->stmt->execute();
	}
	
	public function resultSet() {
		$this->execute();
		return $this->stmt->fetchAll(PDO::FETCH_ASSOC);
	}
	
	public function single() {
		$this->execute();
		return $this->stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	public function rowCount() {
		return $this->stmt->rowCount();
	}
	
	public function lastInsertId() {
		return $this->dbh->lastInsertId();
	}
	
	public function beginTransaction() {
		return $this->dbh->beginTransaction();
	}
	
	public function endTransaction() {
		return $this->dbh->commit();
	}
	
	public function cancelTransaction() {
		return $this->dbh->rollBack();
	}
	
	public function debugDumpParams() {
		return $this->stmt->debugDumpParams();
	}
}
?>