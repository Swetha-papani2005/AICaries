<?php
require_once 'config.php';
$conn = getConnection();
$q = $conn->query("SELECT key_value FROM settings WHERE key_name = 'ai_model_url'");
if ($q && $q->num_rows > 0) {
    $r = $q->fetch_assoc();
    echo $r['key_value'];
} else {
    echo "NO_URL_REGISTERED";
}
$conn->close();
?>
