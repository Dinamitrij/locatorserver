call setup_environment

cd %WARDEPLOYDIR%
git add . -A
ping 127.0.0.1 -n 3
git commit -am "Precompiled WAR file deployment"
ping 127.0.0.1 -n 3
git push -f
ping 127.0.0.1 -n 10