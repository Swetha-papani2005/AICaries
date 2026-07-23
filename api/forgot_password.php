<?php
require_once 'config.php';

$input = json_decode(file_get_contents("php://input"), true);
$email = trim($input['email'] ?? '');

if (empty($email)) {
    sendResponse(false, "Email is required");
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(false, "No account found with this email");
}

// Generate reset token
$token = bin2hex(random_bytes(32));

// Save token
$stmt = $conn->prepare("INSERT INTO password_resets (email, token) VALUES (?, ?)");
$stmt->bind_param("ss", $email, $token);
$stmt->execute();

// Build dynamic link using the host accessed by the user
$host = $_SERVER['HTTP_HOST'] ?? 'localhost';
$resetLink = "http://" . $host . "/aicaries/web/dist/index.html?token=" . $token . "&email=" . urlencode($email);

$emailMessage = '
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>AICaries Password Reset</title>
</head>
<body style="font-family: sans-serif; background-color: #f3f4f6; color: #1f2937; padding: 20px;">
    <div style="background: white; max-width: 600px; margin: 0 auto; border-radius: 12px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
        <h2 style="color: #6366f1; margin-top: 0;">AICaries Password Reset Request</h2>
        <p>Hello,</p>
        <p>We received a request to reset the password for your AICaries account. Click the button below to choose a new password:</p>
        <div style="text-align: center; margin: 30px 0;">
            <a href="' . $resetLink . '" style="display: inline-block; padding: 12px 24px; background-color: #6366f1; color: white; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 0.95rem;">Reset Password</a>
        </div>
        <p>Or copy and paste this link directly in your browser:</p>
        <p style="word-break: break-all; color: #6366f1;"><a href="' . $resetLink . '">' . $resetLink . '</a></p>
        <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 30px 0;">
        <p style="font-size: 0.85rem; color: #6b7280; margin-bottom: 0;">If you did not request this password reset, you can safely ignore this email.</p>
    </div>
</body>
</html>
';

// Call the Python send_email.py mailer script using proc_open to pass the HTML message via stdin
$descriptorspec = [
    0 => ["pipe", "r"], // stdin
    1 => ["pipe", "w"], // stdout
    2 => ["pipe", "w"]  // stderr
];

$command = "python " . escapeshellarg(__DIR__ . "/send_email.py") . " " . 
           escapeshellarg($email) . " " . 
           escapeshellarg("AICaries Password Reset Request");

$process = proc_open($command, $descriptorspec, $pipes);

$responseJson = null;
if (is_resource($process)) {
    // Write body to stdin
    fwrite($pipes[0], $emailMessage);
    fclose($pipes[0]);

    // Read stdout
    $stdout = stream_get_contents($pipes[1]);
    fclose($pipes[1]);

    // Read stderr
    $stderr = stream_get_contents($pipes[2]);
    fclose($pipes[2]);

    $return_var = proc_close($process);

    $responseJson = json_decode(trim($stdout), true);
}

if ($responseJson && $responseJson['success']) {
    sendResponse(true, "Password reset link sent to your email");
} else {
    $errMessage = $responseJson ? $responseJson['message'] : "Email sending failed. Please check your SMTP configuration in api/mail_config.json";
    sendResponse(false, $errMessage);
}

$conn->close();
?>