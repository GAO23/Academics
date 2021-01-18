import getopt, sys
import scapy.all as scapy
from scapy_http import http
import re
import time

class sniffer():

    def __init__(self, args=sys.argv):
        self._args = args
        try:
            # if(len(args) == 1):
            #     raise Exception("not enough arguments\n")
            optlist, args = getopt.getopt(args[1:], 'i:r:')
            self._tracefile = self._interface = self._expression = None
            for o, a in optlist:
                if(o == '-i'):
                    self._interface = a
                elif(o == '-r'):
                    self._tracefile = a
                else:
                    assert False, "Unhandled exception"

        except  Exception as e:
           print(e)
           self.usage()
           sys.exit(2)

        if len(args) != 0:
            self._expression = ' '.join(args)

        self.sniff()

    def sniff(self):
        scapy.load_layer("tls")
        iface = self._interface if self._interface is not None else scapy.conf.iface
        if self._tracefile is None:
            scapy.sniff(iface=iface, prn=self.call_back, filter=self._expression)
        else:
            scapy.sniff(offline=self._tracefile, prn=self.call_back, filter=self._expression)

    def usage(self):
        msg = "Usage: {} [-i interface] [-r tracefile] expression\n".format(self._args[0])
        print(msg)

    def call_back(self, pkt):
        content = pkt.show(dump=True)
        tls_version_regex = re.compile(r'version   = TLS (\d*\.?\d*)')
        tls_version = tls_version_regex.search(content)
        summary = pkt.summary()
        request_addr_regex = re.compile(r'\d.+\d')
        request_addr = request_addr_regex.search(summary)

        if request_addr is None:
            return # since we ignore any none web traffic

        request_addr = request_addr.group()
        time_stamp = time.ctime(pkt.time)
        client_hello = ""
        url_string = ""
        method_string = ""

        if(tls_version is not None):
            client_hello = "client hello, " + tls_version.group()
            url_regex = re.compile(r'servernames= .+')
            url = url_regex.search(content)
            if (url is not None):
                url_string = "url: " + url.group()[16:-2]
        elif pkt.haslayer(http.HTTPRequest):
            url_string = "url: " + (pkt[http.HTTPRequest].Host + pkt[http.HTTPRequest].Path).decode('utf-8')

        method_regex = re.compile(r'Method    = .+')
        method_group = method_regex.search(content)
        if (method_group is not None):
            method_string = method_group.group()

        if (url_string == ""):
            url_string = "url: no url found for this packet"  # since these packets do not contain headers

        msg = "{}, {}, {}, {}, {}".format(time_stamp, request_addr, url_string, client_hello, method_string)
        print(msg)



if __name__ ==  '__main__':
    sniffer = sniffer()
