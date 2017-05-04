FROM ubuntu:xenial

RUN   apt-get update -y && \
      apt-get install -y build-essential openjdk-8-jre-headless maven parallel r-base wget vim  zlibc zlib1g zlib1g-dev && \
      cd /usr/local/src/ && wget https://sourceforge.net/projects/lemur/files/lemur/indri-5.11/indri-5.11.tar.gz/download -O indri-5.11.tar.gz && \
      tar xvfz indri-5.11.tar.gz && cd indri-5.11 &&   \
      ./configure --enable-java --with-javahome=/usr/lib/jvm/java-8-openjdk-amd64 && \
      make && make install
