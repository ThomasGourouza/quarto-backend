echo "Installing docker..."
sudo apt-get remove docker docker-engine docker.io containerd runc
sudo curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo rm get-docker.sh
echo "Installing docker-compose..."
sudo curl -L "https://github.com/docker/compose/releases/download/1.28.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
echo "Installing Java"
sudo apt update
sudo apt install openjdk-11-jdk
# sudo update-alternatives --config java
# export JAVA_HOME=/usr/lib/jvm/...
java --version
echo "Installing Maven"
sudo apt install maven
mvn -version
# Installer Angular
sudo apt-get update
sudo apt-get install software-properties-common
curl -sL https://deb.nodesource.com/setup_12.x | sudo -E bash -
sudo apt-get install -y nodejs
sudo apt-get install -y npm
sudo npm install -g npm@latest
sudo npm install -g @angular/cli
sudo npm install --save-dev -n @angular-devkit/build-angular
# pour les droits sur docker:
sudo echo -e "start on startup\ntask\nexec chmod 666 /var/run/docker.sock" >> /etc/init/docker-chmod.conf
