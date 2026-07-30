<?php
require_once 'config.php';
$conn = getConnection();
$q = $conn->query("SELECT * FROM settings");
while ($r = $q->fetch_assoc()) {
    echo $r['key_name'] . ": " . $r['key_value'] . "\n";
}
?>
