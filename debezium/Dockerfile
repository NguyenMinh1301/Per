FROM alpine:3.19

# Install dependencies required by init-connector.sh
# bash: script interpreter
# curl: to make HTTP requests to Kafka Connect
# jq: to parse/validate JSON
# gettext: for envsubst
RUN apk add --no-cache bash curl jq gettext

# Create configuration directory
WORKDIR /init

# Copy scripts and configuration
COPY init-connector.sh /init/init-connector.sh
COPY connector-postgres.json /config/connector-postgres.json

# Make script executable
RUN chmod +x /init/init-connector.sh

# Set environment variables used by the script
ENV CONNECTOR_CONFIG=/config/connector-postgres.json

# Run the script
CMD ["/init/init-connector.sh"]
