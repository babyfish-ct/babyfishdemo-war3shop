#!/bin/bash

echo -n "Please choose database, o(Oracle, suggested) or h(HSQLDB): "
while [ 1 -eq 1 ]
do
    read database
    database=$(echo "$database" | tr '[A-Z]' '[a-z]')
    if [ "$database" = "o" ]; then
        break
    elif [ "$database" = "oracle" ]; then
        database="o"
        break
    elif [ "$database" = "h" ]; then
        break
    elif [ "$database" = "hsqldb" ]; then
        database="h"
        break
    else
        echo -n "Invalid choice, please enter your choice again: "
    fi
done

args=
if [ "$database" = "o" ]; then
    args=-Doracle
    cd ojdbc
    ./install.sh
    cd ..
fi
cd ./db-installer/project
echo "Use maven to create eclipse projects with the arguments "[$args]
mvn eclipse:eclipse $args
echo "Use maven to compile and test projects with the arguments "[$args]
mvn clean install $args
cd ../..

if [ "$database" = "o" ]; then
    java -classpath ./db-installer/project/target/war3shop-db-installer-1.1.0.Alpha.jar:./db-installer/project/target/lib/ojdbc6-11.2.0.1.0.jar -Doracle org.babyfishdemo.war3shop.db.installer.shell.Shell
else
    java -classpath ./db-installer/project/target/war3shop-db-installer-1.1.0.Alpha.jar:./db-installer/project/target/lib/hsqldb-j5-2.2.4.jar org.babyfishdemo.war3shop.db.installer.shell.Shell
fi

cd ../src
echo "Create eclipse projects for web application"
mvn eclipse:eclipse $args
echo "Compile the web application"
mvn clean install $args
