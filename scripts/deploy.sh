if [ "$#" -lt  "3" ]
 then
   # Use: ./deploy MyPem.pem ec2-XXX-XXX-XXX-XXX.eu-west-1.compute.amazonaws.com myJar.jar XXX.XXX.XXX.XXX XXX.XXX.XXX.XXX
   echo "Use: ./deploy PEM DNS JAR IP SEED"
fi

PEM=$1
DNS=$2
FILE=$3
IP=$4
SEED=$5

TARGET="ubuntu@${DNS}:/home/ubuntu/"

#scp -i $PEM installDependencies.sh "${TARGET}installDependencies.sh"
#ssh -i $PEM "ubuntu@${DNS}" chmod +x "installDependencies.sh"
#ssh -i $PEM "ubuntu@${DNS}" ./installDependencies.sh

ssh -i $PEM "ubuntu@${DNS}" "rm WebChat.zip"
ssh -i $PEM "ubuntu@${DNS}" "rm -rf webchat-1.0"

scp -i  $PEM $FILE "${TARGET}WebChat.zip"

ssh -i $PEM "ubuntu@${DNS}" "pkill -f java"
ssh -i $PEM "ubuntu@${DNS}" "unzip WebChat.zip"
ssh -i $PEM "ubuntu@${DNS}" "rm webchat-1.0/RUNNING_PID"
ssh -i $PEM "ubuntu@${DNS}" "sed 's/HOST/${IP}/' webchat-1.0/conf/application.base > webchat-1.0/conf/application.conf"
ssh -i $PEM "ubuntu@${DNS}" "./webchat-1.0/bin/webchat -Dakka.cluster.seed-nodes.0=akka.tcp://application@${SEED}:8000"
#ssh -i $PEM "ubuntu@${DNS}" java -jar Node.jar -cluster


# FILE=Vertx-DistributedWebChat/target/WebChatVertxMaven-0.1.0-fat.jar
#
# # NODE 1
# scp -i $PEM $FILE ubuntu@ec2-54-154-158-98.eu-west-1.compute.amazonaws.com:/home/ubuntu/WebChatVertxMaven-0.1.0-fat.jar
# # NODE 2
# scp -i $PEM $FILE ubuntu@ec2-54-154-158-98.eu-west-1.compute.amazonaws.com:/home/ubuntu/WebChatVertxMaven-0.1.0-fat.jar
# target/WebChatVertxMaven-0.1.0-fat.jar
