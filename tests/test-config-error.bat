@echo off
call "%~dp0..\run.bat" --vfs "data\example.json" --log "logs\error.csv" --script "tests\startup-error.txt"