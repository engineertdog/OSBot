<?php
if (count(get_included_files()) <= 1) {
	exit;
}

class encryption {
	private $AES_256_CBC = AES_256_CBC;
	
	public function generate256Bit() {
		return base64_encode(openssl_random_pseudo_bytes(32));
	}
	
	public function encrypt256Bit($data) {
		return base64_encode($data);
	}
	
	public function decrypt256Bit($data) {
		return base64_decode($data);
	}
}
?>