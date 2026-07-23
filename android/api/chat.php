<?php
require_once 'config.php';

$input = json_decode(file_get_contents("php://input"), true);
$message = trim($input['message'] ?? '');
$history = $input['history'] ?? [];

if (empty($message)) {
    sendResponse(false, "Message is empty");
}

$config_path = __DIR__ . '/chat_config.json';
$GROQ_API_KEY = "";
if (file_exists($config_path)) {
    $chat_config = json_decode(file_get_contents($config_path), true);
    $GROQ_API_KEY = $chat_config['groq_api_key'] ?? '';
}
$GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

// Build messages array
$messages = [];

// System message
$messages[] = [
    "role" => "system",
    "content" => "You are a helpful AI assistant inside a dental health app called AICaries. You can answer anything the user asks — dental health, general health, or any other topic. Be friendly, clear, and concise."
];

// Add conversation history
foreach ($history as $item) {
    $messages[] = [
        "role" => $item['role'] === 'model' ? 'assistant' : $item['role'],
        "content" => $item['text']
    ];
}

// Add current user message
$messages[] = [
    "role" => "user",
    "content" => $message
];

$payload = json_encode([
    "model" => "llama-3.3-70b-versatile",
    "messages" => $messages,
    "max_tokens" => 1024,
    "temperature" => 0.7
]);

$ch = curl_init($GROQ_URL);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    "Content-Type: application/json",
    "Authorization: Bearer " . $GROQ_API_KEY
]);
curl_setopt($ch, CURLOPT_TIMEOUT, 30);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);
curl_close($ch);

if ($response === false) {
    sendResponse(false, "cURL error: " . $curlError);
}

if ($httpCode !== 200) {
    sendResponse(false, "Groq API error (HTTP $httpCode): " . $response);
}

$data = json_decode($response, true);
$reply = $data['choices'][0]['message']['content'] ?? null;

if (!$reply) {
    sendResponse(false, "No reply from Groq: " . json_encode($data));
}

sendResponse(true, "OK", ["reply" => $reply]);
?>