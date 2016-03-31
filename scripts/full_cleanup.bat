call setup_environment

cd %PROJECT_DIR%\bat
call cleanup
cd %WARDEPLOYDIR%
rhc app-tidy %APP%
ping 127.0.0.1 -n 10