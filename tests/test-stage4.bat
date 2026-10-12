@echo off
call "%~dp0..\run.bat" --vfs "data\deep.json" --log "logs\stage4-ok.csv" --script "tests\stage4-ok.txt"
if errorlevel 1 exit /b 1

call "%~dp0..\run.bat" --vfs "data\deep.json" --log "logs\stage4-error.csv" --script "tests\stage4-error.txt"
if errorlevel 1 exit /b 1

call "%~dp0..\run.bat" --vfs "data\deep.json" --log "logs\stage4-args.csv" --script "tests\stage4-args-error.txt"
if errorlevel 1 exit /b 1

call "%~dp0..\run.bat" --vfs "data\deep.json" --log "logs\stage4-du.csv" --script "tests\stage4-du-error.txt"