
@echo off
setlocal

set "TEMP_VFS=%TEMP%\vfs-stage5-%RANDOM%-%RANDOM%.json"

copy /Y "%~dp0..\data\deep.json" "%TEMP_VFS%" >nul
if errorlevel 1 exit /b 1

echo Testing all commands...
call "%~dp0..\run.bat" --vfs "%TEMP_VFS%" --log logs\stage5-all.csv --script tests\stage5-all.txt

del /Q "%TEMP_VFS%" 2>nul

echo Testing invalid path...
call "%~dp0..\run.bat" --vfs data\deep.json --log logs\stage5-error.csv --script tests\stage5-error.txt

echo Testing invalid arguments...
call "%~dp0..\run.bat" --vfs data\deep.json --log logs\stage5-args.csv --script tests\stage5-args-error.txt

endlocal
