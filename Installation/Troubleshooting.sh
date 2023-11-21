Cython
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
cd ..


sysrepo
#in sysrepo installation it was added (include <stdint.h>) to the file /sysrepo-master/tests/test_rpc_action.c
#to solve a make install error


nnpy
#WARNING: Running pip as the 'root' user can result in broken permissions and conflicting behaviour with the system package manager. It is recommended to use a virtual environment instead: https://pip.pypa.io/warnings/venv


thrift (THE DEFAULT VERSION IN THE .sh IS 0.13.0)
#0.11.0 fails "make"
#0.13.0 fails "make", fails "make check" simple_switch test beacause of "segmentation fault"
#0.19.0 passes "make" and "make check" MAY STIL Cause some problem later on 


#ONOS container
#did not runned to create a symlink
sudo ln -sf /usr/bin/docker.io /usr/local/bin/docker 

#tested with this traits:
sudo docker run -t -d -p 8181:8181 -p 8101:8101 -p 5005:5005 -p 830:830 --name onos onosproject/onos

    -t will allocate a pseudo-tty to the container
    -d will run the container in foreground
    -p <CONTAINER_PORT>:<HOST_PORT> Publish a CONTAINER_PORT to a HOST_PORT. Some of the ports that ONOS uses:
        8181 for REST API and GUI
        8101 to access the ONOS CLI
        9876 for intra-cluster communication (communication between target machines)
        6653 for OpenFlow
        6640 for OVSDB
        830 for NETCONF
        5005 for debugging, a java debugger can be attached to this port
		
#So with the previous command we are publishing the ONOS CLI, GUI, NETCONF, and Debugger ports.