ARG UPSTREAM_REPO
ARG UPSTREAM_TAG
FROM ${UPSTREAM_REPO:-uselagoon}/python-3.12:${UPSTREAM_TAG:-latest}

RUN apk add --no-cache \
      bash \
      curl \
      gettext \
      git \
      jq \
      moreutils \
      openssh-client \
      rsync

ENV CRYPTOGRAPHY_DONT_BUILD_RUST=1

WORKDIR /ansible
COPY . /ansible

RUN apk add --no-cache --virtual .build-deps \
      g++ \
      libffi-dev \
      openssl-dev \
    && pip3 install -r requirements.txt \
    && apk del .build-deps

RUN ansible-galaxy collection install -vvv ansible.posix community.general kubernetes.core

# download, extract and install kubectl binary
ENV KUBECTL_VER=v1.31.0
RUN architecture=$(case $(uname -m) in x86_64 | amd64) echo "amd64" ;; aarch64 | arm64 | armv8) echo "arm64" ;; *) echo "amd64" ;; esac) \
    && curl -s -LO "https://dl.k8s.io/release/$KUBECTL_VER/bin/linux/${architecture}/kubectl" \
    && curl -s -LO "https://dl.k8s.io/$KUBECTL_VER/bin/linux/${architecture}/kubectl.sha256" \
    && echo "$(cat kubectl.sha256)  kubectl" | sha256sum \
    && chmod +x kubectl \
    && mv kubectl /usr/local/bin/kubectl \
    && rm kubectl.sha256

RUN git config --global user.email "deploytest@lagoon.sh" && git config --global user.name deploytest

COPY hosts /etc/ansible/hosts

ENV ANSIBLE_FORCE_COLOR=true \
    SSH_AUTH_SOCK=/tmp/ssh-agent \
    KEYCLOAK_URL=http://keycloak:8080 \
    JWTSECRET=super-secret-string \
    JWTAUDIENCE=api.dev \
    JWTUSER=test-suite

COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
