##################################  linux

sudo make p4-build
sudo make start

##################################  (mininet cli)

sudo make mn-cli

# we adding to each host the ip address and the mac address of the other host
h1a ip -6 neigh replace 2001:1:1::B lladdr 00:00:00:00:00:1B dev h1a-eth0
h1b ip -6 neigh replace 2001:1:1::A lladdr 00:00:00:00:00:1A dev h1b-eth0


h1a ping h1b  #all lost

##################################  (P4Runtime Shell)

To connect the P4Runtime Shell to `leaf1` and push the pipeline configuration obtained before, use the following command:

sudo util/p4rt-sh --grpc-addr localhost:50001 --config p4src/build/p4info.txt,p4src/build/bmv2.json --election-id 0,1


# Create the table entry for h1b (MAC: 00:00:00:00:00:1B) to port 4
te_h1b = table_entry["IngressPipeImpl.l2_exact_table"](action = "IngressPipeImpl.set_egress_port")
te_h1b.match["hdr.ethernet.dst_addr"] = "00:00:00:00:00:1B"
te_h1b.action["port_num"] = "4"
print(te_h1b)

# Create the table entry for h1a (MAC: 00:00:00:00:00:1A) to port 3
te_h1a = table_entry["IngressPipeImpl.l2_exact_table"](action = "IngressPipeImpl.set_egress_port")
te_h1a.match["hdr.ethernet.dst_addr"] = "00:00:00:00:00:1A"
te_h1a.action["port_num"] = "3"
print(te_h1a)

te_h1b.insert()
te_h1a.insert()

##################################  (mininet cli)
h1a ping h1b  #all worked


##################################  linux  (WARNING: at onos-cli, added "-o HostKeyAlgorithms=ssh-rsa" to avoid error related to authentication)
sudo make onos-cli


##################################  linux (TO APPLY APP CHANGES AT ONOS at runtime)

sudo make app-build
sudo make app-reload


##################################  linux (push topology to onos)

sudo make netcfg


