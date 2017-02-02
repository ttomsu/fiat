FROM java:8

MAINTAINER delivery-engineering@netflix.com

COPY . workdir/

WORKDIR workdir

RUN GRADLE_USER_HOME=cache ./gradlew buildDeb -x test

RUN dpkg -i ./fiat-web/build/distributions/*.deb

RUN cd .. && rm -rf workdir

CMD ["/opt/fiat/bin/fiat"]
