call setup_environment
%MYSQLDUMP_PATH% -u%MYSQLUSER% -p%MYSQLPASS% --host=localhost --port=3306 %APP% gpsdata --skip-extended-insert --add-drop-table -r gps.sql
