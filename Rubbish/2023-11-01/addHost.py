import argparse
from mininet.topo import Topo
from mininet.node import Host
from mininet.util import ipAdd, ipBase

# Rest of your script


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
    host = topo.addHost(host_name, cls=AdditionalHost, mac=mac, ipv6=ipv6, ipv6_gw=ipv6_gw)
    topo.addLink(host, switch)

def main():
    # Update the existing topology name
    class UpdatedTutorialTopo(TutorialTopo):
        pass

    # Create the Mininet topology with the additional host
    topo = UpdatedTutorialTopo()
    switch_h4 = topo.getNodeByName('h4')
    add_additional_host(topo, 'h5', switch_h4, '00:00:00:00:00:50', '2001:2:5::1/64', '2001:2:4::ff')

    net = Mininet(topo=topo, controller=None)
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
    parser = argparse.ArgumentParser(description='Mininet topology script for 2x2 fabric with stratum_bmv2 and IPv6 hosts')
    args = parser.parse_args()
    setLogLevel('info')
    main()
