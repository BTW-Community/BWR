FROM openjdk:8-jdk-slim
WORKDIR /home/user
COPY . .
RUN apt-get update &&\
	apt-get upgrade -y &&\
	apt-get install -y python2.7 unzip zip make patch libdigest-md5-perl &&\
	apt-get clean &&\
	useradd -m user &&\
	chown -R user .
USER user
