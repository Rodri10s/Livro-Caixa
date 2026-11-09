@echo off
cd /d "%~dp0"
if not exist "build\LivroCaixa.jar" powershell -ExecutionPolicy Bypass -File ".\build.ps1"
java -jar "build\LivroCaixa.jar"
