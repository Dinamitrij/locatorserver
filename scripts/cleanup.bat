call setup_environment

cd %WARDEPLOYDIR%
git gc --aggressive
ping 127.0.0.1 -n 3