 #!/bin/bash

export DATAFLOW_VERSION=2.10.2
export SKIPPER_VERSION=2.9.2
docker-compose -f docker-compose.yml -f docker-compose-rabbitmq.yml -f docker-compose-mysql.yml up