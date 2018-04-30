call setup_environment
rem Backup LOCALHOST
rem %MYSQLDUMP_PATH% -u%MYSQLUSER% -p%MYSQLPASS% --host=localhost --port=3306 %APP% --skip-extended-insert --add-drop-table -r %PROJECT_DIR%\bat\%APP%.sql

rem Backup Remote
%MYSQLDUMP_PATH% -u%MYSQLUSER% -p%MYSQLPASS% --host=%MYSQLHOST% --port=%MYSQLPORT% %APP% --skip-extended-insert --add-drop-table -r %PROJECT_DIR%\bat\%APP%.sql
