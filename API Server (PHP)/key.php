<?php

echo base64_encode(base64_encode(openssl_random_pseudo_bytes(32)));

?>