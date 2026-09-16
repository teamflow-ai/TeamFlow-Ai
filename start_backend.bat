@echo off
title TeamFlow AI - Starting Backend Microservices
echo ========================================================
echo   Launching TeamFlow AI Backend Microservices...
echo ========================================================
cd /d "%~dp0\backend"
powershell -ExecutionPolicy Bypass -File "run_all_no_redis.ps1"
pause
