from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import Host

# Custom topology class to add a new switch, links, and a host
class CustomTopology(Topo):
    def build(self):
        # Your existing switches
        spine2 = self.addSwitch('spine2')  # Replace with your P4 switch type
        leaf2 = self.addSwitch('leaf2')  # Replace with your P4 switch type

        # Create a new switch 'spine3'
        spine3 = self.addSwitch('spine3')  # Replace with your P4 switch type

        # Create links between spine3, spine2, and leaf2 with explicit interface names
        self.addLink(spine3, spine2, intfName1='spine3-eth1', intfName2='spine2-eth1')
        self.addLink(spine3, leaf2, intfName1='spine3-eth2', intfName2='leaf2-eth1')

        # Add a host 'h5' connected to 'spine3'
        host5 = self.addHost('h5')
        self.addLink(spine3, host5)

# Create the Mininet instance with your custom topology
topo = CustomTopology()
net = Mininet(topo=topo, controller=None)

# Start Mininet
net.start()

# Interact with your network as needed
# Example: net.pingAll()

# To remove the 'spine3' switch and the host 'h5' from the network. n resolve bem:
# mininet > net.get('spine3').delete()


# Remove a switch from the network:     mininet > net.get('spine3').delete()
