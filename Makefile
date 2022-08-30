MANAGER_DOCKER_FILE=build/manager.Dockerfile
MANAGER_IMG=blockmanager

CHAIN_DOCKER_FILE=build/huns-chain.Dockerfile
CHAIN_IMG=huns-chain

WALLET_DOCKER_FILE=build/huns-wallet.Dockerfile
WALLET_IMG=huns-wallet

BUILD_TIME ?= $(shell date +"%Y-%m-%dT%H:%M:%SZ%Z")
VERSION=$(shell date +"%y.%V"-$(shell git rev-parse --short HEAD))

manager-docker:	## 	Build service as a docker image
	docker build --rm \
	    -f ${MANAGER_DOCKER_FILE} \
	    -t ${MANAGER_IMG} \
	    --compress \
	    --build-arg BUILT="${BUILD_TIME}" \
	    --build-arg VERSION="${VERSION}" \
	    .

huns-chain-docker:	## Build service as a docker image
	docker build --rm \
	    -f ${CHAIN_DOCKER_FILE} \
	    -t ${CHAIN_IMG} \
	    --compress \
	    --build-arg BUILT="${BUILD_TIME}" \
	    --build-arg VERSION="${VERSION}" \
	    .

huns-wallet-docker:	## Build service as a docker image
	docker build --rm \
	    -f ${WALLET_DOCKER_FILE} \
	    -t ${WALLET_IMG} \
	    --compress \
	    --build-arg BUILT="${BUILD_TIME}" \
	    --build-arg VERSION="${VERSION}" \
	    .