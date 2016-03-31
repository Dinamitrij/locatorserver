call setup_environment
%MYSQLEXE_PATH% -h 127.0.0.1 -u %MYSQLUSER% --password=%MYSQLPASS% --port=3306 %APP% < %PROJECT_DIR%\bat\%APP%.sql

