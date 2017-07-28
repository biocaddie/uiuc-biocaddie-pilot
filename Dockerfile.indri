FROM ubuntu:xenial

RUN   apt-get update -y && \
      apt-get install -y build-essential \
         git \
         maven \
         openjdk-8-jdk-headless \
         parallel \
         r-base \
         vim \
         wget \
         zlibc \
         zlib1g \
         zlib1g-dev &&  \
      cd /usr/local/src/ && \
      wget https://sourceforge.net/projects/lemur/files/lemur/indri-5.8/indri-5.8.tar.gz/download -O indri-5.8.tar.gz && \
      tar xvfz indri-5.8.tar.gz && cd indri-5.8 &&   \
      ./configure --enable-java --with-javahome=/usr/lib/jvm/java-8-openjdk-amd64 && \
      make && make install && \
      cd /usr/local/src/ && \ 
      wget http://trec.nist.gov/trec_eval/trec_eval_latest.tar.gz && \
      tar xvfz trec_eval_latest.tar.gz && cd trec_eval.9.0/ && \
      make && make install


