call setup_environment
%MYSQLEXE_PATH% -h localhost -u locator --password=locator --port=3306 locator < %PROJECT_DIR%\bat\gps.sql
