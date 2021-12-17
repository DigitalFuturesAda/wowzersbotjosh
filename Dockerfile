FROM openjdk:11
RUN mkdir -p /app
COPY ./ /app
WORKDIR /app
RUN apt-get -y update && \
    apt-get install -y --no-install-recommends wget unzip
RUN wget https://services.gradle.org/distributions/gradle-5.6-bin.zip
RUN mkdir /opt/gradle
RUN unzip -d /opt/gradle gradle-5.6-bin.zip
ENV PATH="/opt/gradle/gradle-5.6/bin:${PATH}"
RUN gradle build
CMD ["gradle", "run"]
