import subprocess
import re
import urllib.request
import time
import sys

def start_tunnel():
    cmd = [
        "ssh",
        "-o", "StrictHostKeyChecking=no",
        "-o", "ServerAliveInterval=60",
        "-o", "UserKnownHostsFile=NUL",
        "-R", "80:localhost:5000",
        "serveo.net"
    ]
    
    while True:
        print("\n===================================================")
        print("  AICARIES LIVE DATABASE-DRIVEN TUNNEL MONITOR")
        print("===================================================")
        print("Starting ssh connection to serveo.net...")
        
        try:
            # Start ssh process
            process = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1
            )
            
            for line in iter(process.stdout.readline, ''):
                print(line.strip())
                
                # Search for forwarding URL
                match = re.search(r'Forwarding HTTP traffic from (https://[a-zA-Z0-9.-]+)', line)
                if match:
                    tunnel_url = match.group(1)
                    print(f"\n[+] Active Tunnel URL detected: {tunnel_url}")
                    
                    # Register tunnel URL to the database
                    register_url = f"http://localhost/aicaries/api/register_tunnel.php?url={tunnel_url}"
                    try:
                        print(f"[*] Registering URL to cloud database via local API...")
                        req = urllib.request.Request(
                            register_url,
                            headers={'User-Agent': 'Mozilla/5.0'}
                        )
                        with urllib.request.urlopen(req) as response:
                            resp_text = response.read().decode('utf-8')
                            print(f"[+] Registration Response: {resp_text}")
                    except Exception as e:
                        print(f"[!] Database registration failed: {str(e)}")
                        print("[!] Make sure XAMPP Apache is running locally!")
            
            process.wait()
        except Exception as e:
            print(f"[!] Error running ssh process: {str(e)}")
            
        print("\n[!] Connection lost. Reconnecting in 5 seconds...")
        time.sleep(5)

if __name__ == "__main__":
    start_tunnel()
