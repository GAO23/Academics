#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

apt update;
apt install default-jdk -y;
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -;
apt install apt-transport-https; 
echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-6.x.list;
apt update; apt install elasticsearch;
# edit the yml. Need to do this manaually 
nano /etc/elasticsearch/elasticsearch.yml;
# uncomment network.host and change ip to 0.0.0.0
systemctl daemon-reload;
systemctl disable elasticsearch; # well, enable it if you want 
systemctl start elasticsearch;
apt install kibana;
systemctl disable kibana;
systemctl start kibana;
cd /etc/kibana && nano kibana.yml; # change the server.host to "0.0.0.0"
apt install npm;
npm install elasticdump;
elasticdump --input=./test_meta_mapping.json  --output=localhost:9200/hw7 --type=mapping;
elasticdump --input=./test_data.json  --output=http://130.245.170.194:9200/hw7 --type=data;

