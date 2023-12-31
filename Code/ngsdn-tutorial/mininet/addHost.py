# -*- coding: utf-8 -*-
import argparse
from mininet.cli import CLI
from mininet.log import setLogLevel
from mininet.net import Mininet
from mininet.node import Host
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.node import UserSwitch
from mininet.util import pmonitor

CPU_PORT = 255

#ISTO É PYTHON2
#copiar script para o container
#   docker cp /home/p4/Desktop/Tese/Código/ngsdn-tutorial/mininet/addHost.py mininet:/root/
#to run, mininet>: 
#   py /root/addHost.py


class TutorialTopo(Topo):
    """2x2 fabric topology with IPv6 hosts"""

    def __init__(self, *args, **kwargs):
        Topo.__init__(self, *args, **kwargs)

        # Leaves
        # gRPC port 50001
        leaf1 = self.addSwitch('leaf1', cls=UserSwitch, cpuport=CPU_PORT)
        # gRPC port 50002
        leaf2 = self.addSwitch('leaf2', cls=UserSwitch, cpuport=CPU_PORT)

        # Spines
        # gRPC port 50003
        spine1 = self.addSwitch('spine1', cls=UserSwitch, cpuport=CPU_PORT)
        # gRPC port 50004
        spine2 = self.addSwitch('spine2', cls=UserSwitch, cpuport=CPU_PORT)

        # Switch Links
        self.addLink(spine1, leaf1)
        self.addLink(spine1, leaf2)
        self.addLink(spine2, leaf1)
        self.addLink(spine2, leaf2)

        # IPv6 hosts attached to leaf 1
        h1a = self.addHost('h1a', mac="00:00:00:00:00:1A", ip='2001:1:1::a/64', defaultRoute='via 2001:1:1::ff')
        h1b = self.addHost('h1b', mac="00:00:00:00:00:1B", ip='2001:1:1::b/64', defaultRoute='via 2001:1:1::ff')
        h1c = self.addHost('h1c', mac="00:00:00:00:00:1C", ip='2001:1:1::c/64', defaultRoute='via 2001:1:1::ff')
        h2 = self.addHost('h2', mac="00:00:00:00:00:20", ip='2001:1:2::1/64', defaultRoute='via 2001:1:2::ff')
        self.addLink(h1a, leaf1)  # port 3
        self.addLink(h1b, leaf1)  # port 4
        self addLink(h1c, leaf1)  # port 5
        self.addLink(h2, leaf1)  # port 6

        # IPv6 hosts attached to leaf 2
        h3 = self.addHost('h3', mac="00:00:00:00:00:30", ip='2001:2:3::1/64', defaultRoute='via 2001:2:3::ff')
        h4 = self.addHost('h4', mac="00:00:00:00:00:40", ip='2001:2:4::1/64', defaultRoute='via 2001:2:4::ff')
        self.addLink(h3, leaf2)  # port 3
        self.addLink(h4, leaf2)  # port 4

class AdditionalHost(Host):
    """Additional host connected to a specified switch."""

    def __init__(self, name, switch, **kwargs):
        super(AdditionalHost, self).__init__(name, **kwargs)
        self.switch = switch

    def setSwitch(self, switch):
        self.switch = switch

    def config(self, **kwargs):
        super(AdditionalHost, self).config(**kwargs)
        if self.switch:
            self.setSwitchMAC()

    def setSwitchMAC(self):
        switch_mac = self.switch.MAC()
        self.intf().setMAC(switch_mac)

def add_additional_host(topo, host_name, switch, mac, ipv6, ipv6_gw):
    host = topo.addHost(host_name, cls=AdditionalHost, mac=mac, ip=ipv6, defaultRoute='via ' + ipv6_gw)
    topo.addLink(host, switch)

def main():
    # Update the existing topology name
    class UpdatedTutorialTopo(TutorialTopo):
        pass

    # Create the Mininet topology with the additional host
    topo = UpdatedTutorialTopo()
    switch_h4 = topo.getNodeByName('h4')
    add_additional_host(topo, 'h5', switch_h4, '00:00:00:00:00:50', '2001:2:5::1/64', '2001:2:4::ff')

    net = Mininet(topo=topo, controller=RemoteController, autoSetMacs=True)
    net.start()
    CLI(net)
    net.stop()
    print('#' * 80)
    print('ATTENTION: Mininet was stopped! Perhaps accidentally?')
    print('No worries, it will restart automatically in a few seconds...')
    print('To access again the Mininet CLI, use `make mn-cli`')
    print('To detach from the CLI (without stopping), press Ctrl-D')
    print('To permanently quit Mininet, use `make stop`')
    print('#' * 80)

if __name__ == "__main__":
    setLogLevel('info')
    main()
