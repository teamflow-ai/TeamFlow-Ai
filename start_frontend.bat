@echo off
title TeamFlow AI - Frontend Server
echo ========================================================
echo   Launching TeamFlow AI Frontend (Vite)...
echo ========================================================
cd /d "%~dp0\frontend"
npm run dev
pause
