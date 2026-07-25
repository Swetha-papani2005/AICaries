@echo off
title AICaries SSH Tunnel
:loop
cls
echo ===================================================
echo   AICARIES LIVE AI MODEL TUNNEL
echo ===================================================
echo Starting tunnel to serveo.net...
echo.
ssh -o StrictHostKeyChecking=no -o ServerAliveInterval=60 -o UserKnownHostsFile=NUL -R 80:localhost:5000 serveo.net
echo.
echo Tunnel disconnected! Restarting in 5 seconds...
ping 127.0.0.1 -n 6 > nul
goto loop
