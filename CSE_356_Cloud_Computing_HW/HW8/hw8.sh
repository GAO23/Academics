#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

apt install mysql-server -y; 
mysql_secure_installation; # configure the mysql, answer y to all questions
mysql -u root -p; # eneter password to enter mysql

# create tables, you have to type these queries manually
# create database hw8;
# create table assists( player varchar(40), club varchar(10), pos varchar(4), gp int, gs int, a int, gwa int, hma int, rda int, min DECIMAL(3,2) );

# now paste these line below to import cvs 

# load data local infile "/home/ubuntu/CSE_356/HW8/without_last_line.csv" into table assists 
# fields terminated by ','
# lines terminated by '\n'
# (player, club, pos, gp, gs, a, gwa, hma, rda, min);

# then run this to iron out none ascii
# update assists set player = "Víctor Bernárdez" where club = 'SJ' and pos = 'D' and min = 0.04;
# update assists set player = "Marcos Ureña" where club = 'LAFC' and pos = 'F' and min = 0.15;
# update assists set player = "Nicolás Lodeiro" where club = 'SEA' and pos = 'M' and min = 0.36;
# update assists set player = "Álvaro Fernández" where club = 'SEA' and pos = 'M' and gp = 10;
# update assists set player = "Gonzalo Verón" where club = 'NYR' and pos = 'M-F';

# lastly run..
# ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '209471879';
# FLUSH PRIVILEGES;

# exit when done

# now deploy the node server
