FROM debian:10

# Switch to archive mirror (Debian 10 is EOL)
RUN sed -i 's|http://deb.debian.org|http://archive.debian.org|g' /etc/apt/sources.list && \
    sed -i 's|http://security.debian.org|http://archive.debian.org|g' /etc/apt/sources.list && \
    sed -i '/buster-updates/d' /etc/apt/sources.list

RUN apt-get update && apt-get install -y \
    gcc \
    make \
    libc-dev \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build
