call setup_environment
rem IF "--no-create-token" -> always asking for password
rhc setup -l %OPENSHIFT_LOGIN% -p %OPENSHIFT_PASSWORD% --server %OPENSHIFT_SERVER% --create-token

