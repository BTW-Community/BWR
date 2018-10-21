FROM openjdk:8-jdk-slim
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install -y python2.7
RUN apt-get install -y unzip
RUN apt-get install -y zip
RUN apt-get install -y make
RUN apt-get install -y patch
RUN apt-get install -y libdigest-md5-perl
RUN useradd -m user
WORKDIR /home/user
ARG SVR
COPY $SVR svr.zip
ARG MCP
COPY $MCP mcp.zip
ARG BTW
COPY $BTW btw.zip
COPY hooks hooks
COPY hooks.pl .
COPY checkbin.pl .
COPY src src
COPY Makefile .
RUN chown -R user /home/user
USER user
RUN make tmp/jar
RUN make tmp/btw
RUN make tmp/mc_btw.jar
RUN make tmp/mcp
RUN make bwr.zip
