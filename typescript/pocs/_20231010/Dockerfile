FROM node:18-bullseye-slim

RUN apt-get update \
    && apt-get install -y ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /home/node/app


COPY package*.json ./

RUN npm i

COPY . .

RUN npm install -g ts-node

ENV TEMPORAL_ADDRESS=antonio.a2dd6.tmprl.cloud:7233
ENV TEMPORAL_NAMESPACE=antonio.a2dd6
ENV TEMPORAL_CLIENT_CERT_PATH=./certs/client.pem
ENV TEMPORAL_CLIENT_KEY_PATH=./certs/client.key

CMD [ "npm", "run", "start"]

