# Masters-2Year-1Semester-Thesis
 
#Use Ubuntu 22.04 LTS 
 
#To install docker follow the official and up to date intructions "Install using the apt repository"  
https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository


#P4c (the official supported version is 20.04 LTS) https://github.com/p4lang/p4c
#(For the dependencies)
sudo apt-get install cmake g++ git automake libtool libgc-dev bison flex \
libfl-dev libboost-dev libboost-iostreams-dev \
libboost-graph-dev llvm pkg-config python3 python3-pip \
tcpdump
pip3 install --user -r requirements.txt

#P4c
source /etc/lsb-release
echo "deb http://download.opensuse.org/repositories/home:/p4lang/xUbuntu_${DISTRIB_RELEASE}/ /" | sudo tee /etc/apt/sources.list.d/home:p4lang.list
curl -fsSL https://download.opensuse.org/repositories/home:p4lang/xUbuntu_${DISTRIB_RELEASE}/Release.key | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/home_p4lang.gpg > /dev/null
sudo apt-get update
sudo apt install p4lang-p4c






BMv2; protobuf; grpc; sysrepo; libyang; PI; P4c; P4Runtime(P4I)
(only David) Sumo
(only David) Mininet