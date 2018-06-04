import subprocess
import json
import time
import urllib.request
import os

pem="scripts/TFG.pem"
zip_file="target/universal/webchat-1.0.zip"
groupName="Cluster"
count=1

def url_is_alive(dns):
    """
    Checks that a given URL is reachable.
    :param url: A URL
    :rtype: bool
    """
    request = urllib.request.Request("http://%s:9000" % dns)
    request.get_method = lambda: 'HEAD'

    try:
        urllib.request.urlopen(request)
        return True
    except urllib.error.URLError:
        return False


def run(pem, node, zip_file, seed):
    dns = node['DNS']
    print("STARTING %s" % dns)
    outfile = open('logs/%s-log.log' % dns, 'w')
    subprocess.call("./scripts/deploy.sh %s %s %s %s %s &" % (pem, dns, zip_file, node["PRIVATE_IP"], seed['PRIVATE_IP']), shell=True, stdout=outfile, stderr=outfile)
    with open(os.devnull, "w") as f:
        subprocess.call("./scripts/addServerToHA.sh node_%s %s &" % (dns, dns), shell=True, stdout=f, stderr=f)


subprocess.call("rm haproxy/haproxy.cfg", shell=True)
res=subprocess.Popen(" aws ec2 describe-instances --filter Name=instance.group-name,Values=\"%s\" Name=instance-state-name,Values=running --query 'Reservations[*].Instances[*].[PublicDnsName, PrivateIpAddress, PublicIpAddress]' --output text" % groupName, shell=True, stdout=subprocess.PIPE).stdout.read()
have_master=False

nodes = []
master = None


for instance in [ i.split('\t') for i in res.decode("utf-8").split('\n') if len(i.split('\t')) == 3]:

    node= dict()
    node['DNS'] = instance[0]
    node['PRIVATE_IP'] = instance[1]
    node['PUBLIC_IP'] = instance[2]
    # ONLY FIRST
    if not have_master:
        have_master = True
        print("Running: activator dist")
        subprocess.call("./activator dist", shell=True, stdout=subprocess.PIPE)
        run(pem, node, zip_file, node)
        node['isMaster'] = True
        master = node
    # OTHERS
    else:
        node['isMaster'] = False
    nodes.append(node)

with open('logs/instances.json', 'w') as outfile:
    json.dump(nodes, outfile)


while True and len(nodes) > 0:
    print("DEPLOYING MASTER ...")
    if url_is_alive(master['DNS']):
        break
    time.sleep( 10 )

print("Master UP")

for node in nodes:
    if not node['isMaster']:
        run(pem, node, zip_file, master)

for node in nodes:
    if not node['isMaster']:
        while True and len(nodes) > 0:
            if url_is_alive(node['DNS']):
                break
            time.sleep( 10 )
        print("NODE: "+node['DNS']+" is UP")

# outfile_ha = open('logs/haproxy.txt', 'w')
# subprocess.call("haproxy -f haproxy/haproxy.cfg", shell=True, stdout=outfile_ha, stderr=outfile_ha)
