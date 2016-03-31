call setup_environment

cd %WORKDIR%
mvn -Popenshift clean package
