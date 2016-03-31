@echo off

call ..\..\bat\setup_account
rem ------------------------------------------------------------------------------------
rem Sensitive data is being configured in the private file "..\..\bat\setup_account.bat"
rem OPENSHIFT_LOGIN, OPENSHIFT_PASSWORD, OPENSHIFT_SERVER, APP, MYSQLUSER, MYSQLPASS
rem ------------------------------------------------------------------------------------

set SSHEXE_PATH=C:\Apps\Git\bin\ssh.exe
set MYSQLEXE_PATH=c:\xampp\mysql\bin\mysql
set MYSQLDUMP_PATH=c:\xampp\mysql\bin\mysqldump
set PROJECT_DIR=g:\Projects\LocatorServer
set WORKDIR=%PROJECT_DIR%\work
set WARDEPLOYDIR=%PROJECT_DIR%\wardeploy4
