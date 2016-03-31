call setup_environment

cd %WARDEPLOYDIR%
%GITEXE_PATH% add . -A
ping 127.0.0.1 -n 3
%GITEXE_PATH% commit -am "Precompiled WAR file deployment"
ping 127.0.0.1 -n 3
%GITEXE_PATH% push -f
ping 127.0.0.1 -n 10