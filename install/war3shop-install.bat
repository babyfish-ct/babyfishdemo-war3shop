:: The "choice" command can only be used in win7,
:: In order to support winxp, use "set /p".
:beginChoice
set /p database=Please choose database, o(Oracle, suggested) or h(HSQLDB): 
@echo off
(
    if /i "%database%" == "o" (goto :endChoice)
    if /i "%database%" == "oracle" (
        set database=o
        goto :endChoice
    )
    if /i "%database%" == "h" (goto :endChoice)
    if /i "%database%" == "hsqldb" (
        set database=h
        goto :endChoice
    )
)
@echo Invalid choice, please enter your choice again:
goto :beginChoice
:endChoice

set args=
if /i "%database%" == "h" goto :endMaven
set args=-Doracle
cd ojdbc
call install.bat
cd ..
:endMaven

cd .\db-installer\project
echo Use maven to create eclipse projects with the arguments[%args%]
call mvn-wrapper eclipse:eclipse %args%
echo Use maven to create eclipse projects with the arguments[%args%]
call mvn-wrapper clean install %args%
cd ..\..

if /i "%database%" == "o" goto :oracle
java -classpath ./db-installer/project/target/war3shop-db-installer-1.1.0.Alpha.jar;./db-installer/project/target/lib/hsqldb-j5-2.2.4.jar org.babyfishdemo.war3shop.db.installer.shell.Shell
goto :finished
:oracle
java -classpath ./db-installer/project/target/war3shop-db-installer-1.1.0.Alpha.jar;./db-installer/project/target/lib/ojdbc6-11.2.0.1.0.jar -Doracle org.babyfishdemo.war3shop.db.installer.shell.Shell
:finished

cd ../src
echo Create eclipse projects for web application
call mvn-wrapper eclipse:eclipse %args%
echo Compile the web application
call mvn-wrapper clean install %args%

pause
