# Requires Ubuntu 16
FROM phusion/baseimage:0.10.2

RUN echo 'DPkg::options { "--force-confdef"; };' >> /etc/apt/apt.conf \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get -y upgrade \
    && DEBIAN_FRONTEND=noninteractive apt-get -y install strongswan strongswan-plugin-farp strongswan-plugin-dhcp iptables uuid-runtime ndppd openssl \
    && rm -rf /var/lib/apt/lists/*

RUN rm /etc/ipsec.secrets
RUN mkdir /config
RUN (cd /etc && ln -s /config/ipsec.secrets .)

ADD ./etc/* /etc/
ADD ./bin/start-vpn /etc/service/vpn/run
ADD ./bin/generate-mobileconfig /usr/bin/generate-mobileconfig

VOLUME /etc
VOLUME /config
# http://blogs.technet.com/b/rrasblog/archive/2006/06/14/which-ports-to-unblock-for-vpn-traffic-to-pass-through.aspx
EXPOSE 500/udp 4500/udp

CMD ["/sbin/my_init"]