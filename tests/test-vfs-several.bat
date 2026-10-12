@echo off
setlocal

set "VFS_TMP=%TEMP%\vfs-several-%RANDOM%.json"
copy /Y "%~dp0..\data\several.json" "%VFS_TMP%" >nul
if errorlevel 1 exit /b 1

call "%~dp0..\run.bat" --vfs "%VFS_TMP%" --log logs\vfs-several.csv --script tests\stage3-ok.txt

del /q "%VFS_TMP%" 2>nul
endlocal