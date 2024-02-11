#FROM THE ORIGINAL PROJECT FILE
#ONOS_VERSION = 2.1.0
#ONOS_MD5 = 6ca21242cf837a726cfbcc637107026b
#ONOS_URL = http://repo1.maven.org/maven2/org/onosproject/onos-releases/$(ONOS_VERSION)/onos-$(ONOS_VERSION).tar.gz
#ONOS_TAR_PATH = #~/onos.tar.gz
APP_OAR = #app/target/srv6_usid-1.0-SNAPSHOT.oar



p4:
	cd p4src && make build

onos-install:
	sudo docker pull onosproject/onos@sha256:eadc0d3c32fdd52525ae9b2dd4702f61ae02e97dcd064f637aeb7f9de479a640

onos-run:
	$(info ************ STARTING ONOS ************)
	sudo docker run -t -d -p 8181:8181 -p 8101:8101 -p 5005:5005 -p 830:830 --name onos onosproject/onos	#from the instructions.sh file
	cd ~ && ./start_onos.sh

topo:
	$(info ************ STARTING MININET TOPOLOGY ************)
	sudo -E python mininet/topo.py --onos-ip ${OCI}

netcfg:
	$(info ************ PUSHING NETCFG TO ONOS ************)
	onos-netcfg ${OCI} config/netcfg.json

app-build: p4
	$(info ************ BUILDING ONOS APP ************)
	cd app && mvn clean package

$(APP_OAR):
	$(error Missing app binary, run 'make app-build' first)

app-reload: $(APP_OAR)
	$(info ************ RELOADING ONOS APP ************)
	onos-app ${OCI} reinstall! app/target/srv6_usid-1.0-SNAPSHOT.oar

test-all:
	$(info ************ RUNNING ALL PTF TESTS ************)
	cd ptf && make all

reset:
	-cd ~ && sudo docker restart -t -d -p 8181:8181 -p 8101:8101 -p 5005:5005 -p 830:830 --name onos onosproject/onos	
	-cd p4src && make clean
	-cd ptf && make clean
	-sudo rm -rf app/target
	-sudo mn -c
	-sudo rm -rf /tmp/bmv2-*


