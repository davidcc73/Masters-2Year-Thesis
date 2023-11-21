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
