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



#in sysrepo installation it was added (include <stdint.h>) to the file /sysrepo-master/tests/test_rpc_action.c
#to solve a make install error


#Bazel support tool not installed but available at https://github.com/p4lang/PI#bazel-support 


