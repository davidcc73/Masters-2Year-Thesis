# Masters-2Year-1Semester-Thesis
 
#Use Ubuntu 22.04 LTS 
 
#To install docker follow the official and up to date intructions "Install using the apt repository"  
https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository

Client: Docker Engine - Community
 Version:           24.0.7
 API version:       1.43
 Go version:        go1.20.10
 Git commit:        afdd53b
 Built:             Thu Oct 26 09:07:41 2023
 OS/Arch:           linux/amd64
 Context:           default

Server: Docker Engine - Community
 Engine:
  Version:          24.0.7
  API version:      1.43 (minimum version 1.12)
  Go version:       go1.20.10
  Git commit:       311b9ff
  Built:            Thu Oct 26 09:07:41 2023
  OS/Arch:          linux/amd64
  Experimental:     false

 containerd:
  Version:          1.6.24
  GitCommit:        61f9fd88f79f081d64d6fa3bb1a0dc71ec870523

 runc:
  Version:          1.1.9
  GitCommit:        v1.1.9-0-gccaecfc

 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0


#(probably only David Caetano needs)
#P4c (the official supported version is 20.04 LTS) https://github.com/p4lang/p4c 
source /etc/lsb-release
echo "deb http://download.opensuse.org/repositories/home:/p4lang/xUbuntu_${DISTRIB_RELEASE}/ /" | sudo tee /etc/apt/sources.list.d/home:p4lang.list
curl -fsSL https://download.opensuse.org/repositories/home:p4lang/xUbuntu_${DISTRIB_RELEASE}/Release.key | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/home_p4lang.gpg > /dev/null
sudo apt-get update
sudo apt install p4lang-p4c

#For the dependencies (RUN ON THE ROOT OF P4c, only the last line needs it, after running may need to add directory to PATH)
# Before some time in 2023-July, the `sudo pip3 install
# -rrequirements.txt` command below installed the Cython package
# version 0.29.35.  After that time, it started installing Cython
# package version 3.0.0, which gives errors on the `sudo pip3 install
# .` command afterwards.  Fix this by forcing installation of a known
# working version of Cython.
sudo pip3 install Cython==0.29.35
sudo pip3 install -rrequirements.txt
GRPC_PYTHON_BUILD_WITH_CYTHON=1 sudo pip3 install .
sudo ldconfig

sudo apt-get install cmake g++ git automake libtool libgc-dev bison flex \
libfl-dev libboost-dev libboost-iostreams-dev \
libboost-graph-dev llvm pkg-config python3 python3-pip \
tcpdump
pip3 install --user -r requirements.txt

###################################################################
#BMv2
git clone https://github.com/p4lang/behavioral-model
cd behavioral-model
cd ci 
chmod +x install-nnpy.sh
sudo ./install-nnpy.sh						

chmod +x install-nanomsg.sh
sudo ./install-nanomsg.sh

chmod +x install-thrift.sh						#needs to be modified the used version in the script from 0.13.0 to 0.19.0
sudo ./install-thrift.sh
cd ..

sudo apt-get install -y automake cmake libgmp-dev \
    libpcap-dev libboost-dev libboost-test-dev libboost-program-options-dev \
    libboost-system-dev libboost-filesystem-dev libboost-thread-dev \
    libevent-dev libtool flex bison pkg-config g++ libssl-dev
./autogen.sh
./configure

sudo make
sudo make install 
sudo ldconfig
#test installation and make sure it passes them all, retries may fix some problems
sudo make check


###################################################################
#P4Runtime(PI) https://github.com/p4lang/PI
#Clone the repositorie and run this line to update sub-repositories
git submodule update --init --recursive

#Dependencies: 
#Already installed: readline; libboost-thread-dev; Protobuf; gRPC
sudo apt-get -y install valgrind
sudo apt-get -y install libtool-bin
sudo apt-get -y install libboost-dev libboost-system-dev libboost-thread-dev

sudo apt-get install libcurl4-gnutls-dev -qq
git clone https://github.com/clibs/clib.git /tmp/clib && cd /tmp/clib
make
sudo make install

git clone https://github.com/clibs/cmocka.git
cd cmocka
mkdir build 
cd build
cmake -DCMAKE_INSTALL_PREFIX=/usr -DCMAKE_BUILD_TYPE=Debug ..
make
sudo make install


sudo apt-get install uncrustify
sudo apt-get install libpcre2-dev

#libyang   and in the root run
git clone https://github.com/CESNET/libyang
cd libyang
sudo mkdir -p /usr/local/share/yang/modules/libyang
mkdir build; cd build
cmake ..
make
sudo make install


#sysrepo  
nano ~/.bashrc    
#add this 2 lines to the end of the file: 
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
export CMAKE_PREFIX_PATH=/usr/local:$CMAKE_PREFIX_PATH
#refresh console
source ~/.bashrc 

sudo ln -s /usr/local/lib/libyang.so /usr/lib/x86_64-linux-gnu/libyang.so
sudo ln -s /usr/local/lib/libyang.so.2 /usr/lib/x86_64-linux-gnu/libyang.so.2
#after this clone, go to /sysrepo-master/tests/test_rpc_action.c and add (include <stdint.h>)
git clone https://github.com/sysrepo/sysrepo
cd sysrepo-master
cd mkdir build; cd build
cmake ..
sudo make
sudo make install


#Building p4runtime.proto
cd PI
./autogen.sh
./configure --with-proto
make
make check
sudo make install


#Bazel support
#download Bazelisk binary on https://github.com/bazelbuild/bazelisk/releases 
#move it To /usr/local/bin/bazel
sudo mv ~/Downloads/bazelisk-linux-amd64 /usr/local/bin/bazel
sudo chmod +x /usr/local/bin/bazel
nano ~/.bashrc
#add this line to the end of the file: 
export PATH="/usr/local/bin:$PATH"
#reopen file to reload
nano ~/.bashrc

#to test Bazel on the PI installation, it will: build the P4Runtime PI frontend and run the tests
cd PI
bazel build //proto/frontend:pifeproto
bazel test //proto/tests:pi_proto_tests


###################################################################
#ONOS SDN





###################################################################
#(only David Caetano) Eclipse Sumo
sudo add-apt-repository ppa:sumo/stable
sudo apt-get update
sudo apt-get install sumo sumo-tools sumo-doc



###################################################################
#(only David Caetano) Mininet, Native Installation from Source http://mininet.org/download/
git clone https://github.com/mininet/mininet
cd mininet
git tag  # list available versions
git checkout -b mininet-2.3.0 2.3.0  # or whatever version you wish to install
cd ..
#go to mininet/util/install.sh and replace all "git clone git" by "git clone htpps"
sudo PYTHON=python3 mininet/util/install.sh -a
#test installation
sudo mn --switch ovsbr --test pingall


###################################################################
#Test P4 in Mininet, this will test for router (we will use switchs in the work, but it's ok)
clone https://github.com/p4lang/behavioral-model

#1º-Terminal
	cd behavioral-model
    cd mininet
    sudo python3 1sw_demo.py --behavioral-exe ../targets/simple_router/simple_router --json ../targets/simple_router/simple_router.json

#2º-Terminal
	cd behavioral-model
    cd targets/simple_router
    ./runtime_CLI < commands.txt
	
#1º-Terminal 
	pingall			#if there is no drops them all is good