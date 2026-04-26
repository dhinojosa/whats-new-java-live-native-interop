FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu

ARG TARGETOS
ARG TARGETARCH

USER root

RUN apt-get update && apt-get install -y \
    curl \
    tar \
    unzip \
    ca-certificates \
    git \
    bash \
    clang \
    gcc \
    make \
    pkg-config \
    rustc \
    cargo \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

# Install OpenJDK 25
RUN curl -fL -o /tmp/openjdk-25_linux-x64_bin.tar.gz \
    https://download.java.net/java/GA/jdk25.0.2/b1e0dfa218384cb9959bdcb897162d4e/10/GPL/openjdk-25.0.2_linux-x64_bin.tar.gz \
    && tar -xzf /tmp/openjdk-25_linux-x64_bin.tar.gz -C /usr/local/ \
    && rm /tmp/openjdk-25_linux-x64_bin.tar.gz \
    && mv /usr/local/jdk-25 /usr/local/openjdk-25

ENV JAVA_HOME=/usr/local/openjdk-25
ENV PATH=${JAVA_HOME}/bin:${PATH}

# Install Maven
RUN MAVEN_VERSION=3.9.8 \
    && MAVEN_BASE_URL=https://archive.apache.org/dist/maven/maven-$(echo $MAVEN_VERSION | cut -d'.' -f1) \
    && curl -fL -o /tmp/apache-maven-bin.tar.gz ${MAVEN_BASE_URL}/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    && tar -xzf /tmp/apache-maven-bin.tar.gz -C /usr/local/ \
    && rm /tmp/apache-maven-bin.tar.gz \
    && mv /usr/local/apache-maven-$MAVEN_VERSION /usr/local/maven

ENV MAVEN_HOME=/usr/local/maven
ENV PATH=${MAVEN_HOME}/bin:${PATH}

RUN set -eux; \
    case "${TARGETOS}/${TARGETARCH}" in \
      "linux/amd64") \
        JEXTRACT_URL="https://download.java.net/java/early_access/jextract/25/2/openjdk-25-jextract+2-4_linux-x64_bin.tar.gz" ;; \
      "linux/arm64") \
        JEXTRACT_URL="https://download.java.net/java/early_access/jextract/25/2/openjdk-25-jextract+2-4_linux-aarch64_bin.tar.gz" ;; \
      *) \
        echo "Unsupported target: ${TARGETOS}/${TARGETARCH}"; exit 1 ;; \
    esac; \
    curl -fL "$JEXTRACT_URL" -o /tmp/jextract.tar.gz; \
    mkdir -p /usr/local/jextract; \
    tar -xzf /tmp/jextract.tar.gz -C /usr/local/jextract --strip-components=1; \
    rm /tmp/jextract.tar.gz

ENV PATH=/usr/local/jextract/bin:${PATH}

USER vscode
