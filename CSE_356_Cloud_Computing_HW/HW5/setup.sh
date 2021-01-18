#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

apt update && apt dist-upgrade;
apt install nginx -y;
mv ./ngix_round_robin_balanacer_proxy /etc/nginx/sites-available/default; # if on fedora, this file should be place conf.d
nginx -s reload;
systemctl reload nginx; 
setsebool -P httpd_can_network_connect 1; # do this or proxy won't work for centos, fedora, or red hat
