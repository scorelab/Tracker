FROM ubuntu:latest

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6
RUN echo "deb [ arch=amd64,arm64 ] http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.4 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.4.list
RUN apt-get update && apt-get install -y mongodb-org
RUN mkdir -p /data/db

CMD mongod

RUN apt-get install -y nodejs npm
RUN ln -s /usr/bin/nodejs /usr/bin/node

COPY . /src
WORKDIR /src

RUN npm install; npm install -g grunt-cli

EXPOSE 3000

CMD grunt
