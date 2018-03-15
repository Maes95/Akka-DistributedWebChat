ID=$1
IP=$2
if [ ! -f haproxy/haproxy.cfg ]; then
  cp haproxy/default.cfg haproxy/haproxy.cfg
fi
sed -i -e "/new nodes here/a\    server ${ID} ${IP}:9000 check" haproxy/haproxy.cfg
