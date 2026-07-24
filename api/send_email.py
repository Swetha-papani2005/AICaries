import sys
import os
import json
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

def main():
    if len(sys.argv) < 3:
        print(json.dumps({"success": False, "message": "Usage: send_email.py to subject (body via stdin)"}))
        return

    to_email = sys.argv[1]
    subject = sys.argv[2]
    body = sys.stdin.read()

    # Load configuration from environment variables first, fallback to mail_config.json
    sender_email = os.environ.get("SMTP_SENDER_EMAIL")
    sender_password = os.environ.get("SMTP_SENDER_PASSWORD")
    smtp_server = os.environ.get("SMTP_SERVER", "smtp.gmail.com")
    try:
        smtp_port = int(os.environ.get("SMTP_PORT", 587))
    except:
        smtp_port = 587
    use_tls = os.environ.get("SMTP_USE_TLS", "true").lower() == "true"

    if not sender_email or not sender_password:
        config_path = os.path.join(os.path.dirname(__file__), 'mail_config.json')
        if os.path.exists(config_path):
            try:
                with open(config_path, 'r') as f:
                    config = json.load(f)
                sender_email = config.get("sender_email", "")
                sender_password = config.get("sender_password", "")
                smtp_server = config.get("smtp_server", "smtp.gmail.com")
                smtp_port = int(config.get("smtp_port", 587))
                use_tls = config.get("use_tls", True)
            except Exception as e:
                print(json.dumps({"success": False, "message": f"Error parsing config file: {str(e)}"}))
                return
        else:
            print(json.dumps({"success": False, "message": "SMTP credentials not configured (env variables or mail_config.json missing)"}))
            return

    if not sender_email or "YOUR_GMAIL_HERE" in sender_email or not sender_password or "YOUR_APP_PASSWORD" in sender_password:
        print(json.dumps({"success": False, "message": "Please configure real SMTP credentials in env variables or api/mail_config.json"}))
        return

    try:
        # Create message container
        msg = MIMEMultipart()
        msg['From'] = sender_email
        msg['To'] = to_email
        msg['Subject'] = subject

        # Attach html body
        msg.attach(MIMEText(body, 'html'))

        # Connect to SMTP server
        server = smtplib.SMTP(smtp_server, smtp_port, timeout=15)
        if use_tls:
            server.starttls()
        
        server.login(sender_email, sender_password)
        server.sendmail(sender_email, to_email, msg.as_string())
        server.quit()

        print(json.dumps({"success": True, "message": "Email sent successfully"}))
    except Exception as e:
        print(json.dumps({"success": False, "message": str(e)}))

if __name__ == "__main__":
    main()
