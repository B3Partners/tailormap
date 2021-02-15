ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- USER SQL
-- CREATE USER "JENKINS_FLAMINGO" IDENTIFIED BY "jenkins_flamingo"  DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP";
CREATE USER "JENKINS_FLAMINGO" IDENTIFIED BY "jenkins_flamingo"  DEFAULT TABLESPACE "USERTBS" TEMPORARY TABLESPACE "TEMPTS1";
-- QUOTAS
-- ALTER USER "JENKINS_FLAMINGO" QUOTA UNLIMITED ON "USERS";
ALTER USER "JENKINS_FLAMINGO" QUOTA UNLIMITED ON "USERTBS";

-- ROLES
GRANT "CONNECT" TO "JENKINS_FLAMINGO" ;
GRANT "RESOURCE" TO "JENKINS_FLAMINGO" ;
ALTER USER "JENKINS_FLAMINGO" DEFAULT ROLE "CONNECT","RESOURCE";

-- GRANT CREATE VIEW TO "JENKINS_FLAMINGO" ;
-- GRANT CREATE SYNONYM TO "JENKINS_FLAMINGO" ;