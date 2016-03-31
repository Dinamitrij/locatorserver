call setup_environment
%MYSQLDUMP_PATH% -u%MYSQLUSER% -p%MYSQLPASS% --host=localhost --port=3306 %APP% configuration --complete-insert --skip-extended-insert --add-drop-table -r %PROJECT_DIR%\bat\configuration.sql


