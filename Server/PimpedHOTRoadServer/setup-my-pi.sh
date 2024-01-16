#!/usr/bin/sh

set -e

cd ~

echo "Installing dependencies"
sudo apt update && sudo apt upgrade -y
sudo apt install libjpeg9-dev imagemagick libv4l-dev cmake openjdk-17-jdk maven git build-essential pigpio -y

echo "cloning git repositories"
git clone https://github.com/htl-leonding-tadeot/roboterfuehrerschein/
git clone https://github.com/jacksonliam/mjpg-streamer

echo "Building roboterfuehrerschein server..."
cd roboterfuehrerschein/Server/PimpedHOTRoadServer/
git switch tadeot24/experimental
mvn clean package

cd ~
echo "Building mjpg streamer..."
cd mjpg-streamer/mjpg-streamer-experimental
make

cd ~
USER_HOME=$(pwd)
echo "Setting up systemd service"
cat << EOF > hrinit.sh
#!/bin/sh
cd "$USER_HOME/mjpg-streamer/mjpg-streamer-experimental"
sudo java -jar "$USER_HOME/roboterfuehrerschein/Server/PimpedHOTRoadServer/target/PimpedHOTRoadServer.jar" & sudo ./mjpg_streamer -i "./input_uvc.so -n -y -f 15 -q 20 -r 320x240" -o "./output_http.so -n -w ./www -p 80"
EOF
chmod +x hrinit.sh

cat << EOF > hotroad.service
[Unit]
Description=HOTRoad service
After=network-online.target

[Service]
Type=simple
ExecStart=$USER_HOME/hrinit.sh

[Install]
WantedBy=multi-user.target
EOF

sudo cp hotroad.service /etc/systemd/system/
sudo systemctl enable --now hotroad.service





