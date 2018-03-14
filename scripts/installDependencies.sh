# sudo add-apt-repository ppa:openjdk-r/ppa
# sudo apt-get update
# sudo apt-get install openjdk-8-jdk
sudo apt-get install -y python-software-properties debconf-utils
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer
sudo apt install unzip
