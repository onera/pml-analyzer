---
layout: doc-page
title: "Getting started with Docker"
---

The simplest way to use PML Analyzer is to download a pre-configured Docker image to run interference analyses.
The only dependency is the [Docker execution engine](https://docs.docker.com/get-docker/).
Docker enables you to create from this image a container where all dependencies have been already resolved.
### Container for a simple execution of PML examples
The following commands create a simple container that can be used to run the examples provided in PML Analyzer.
```shell
# load directly the archive containing the image of the preconfigured PML Analyzer environment
docker load < [IMAGE_NAME].tar.gz

# The image has been configured to run as a non-root user
# To share content between the container and the host, it is mandatory to create
# the directories upfront with the write rights
mkdir [PATH_TO_SHARED_FOLDER]/[SHARED_FOLDER_NAME]
chmod -R a+w [PATH_TO_ANALYSIS_RESULT]/[SHARED_FOLDER_NAME]

# In particular, it is interesting to share a folder to store the interference analysis results [PATH_TO_ANALYSIS_RESULT]
# and to share a folder to store the export results [PATH_TO_EXPORT_RESULT]

# To run a 'one-shot' container (removed after exit) from an image with shared directories for the results.
# Note that directory sharing is not mandatory, one can just run the container without the -v options
docker run -it --rm \
-v [PATH_TO_ANALYSIS_RESULT]:/home/user/code/analysis \
-v [PATH_TO_EXPORT_RESULT]:/home/user/code/export \
[IMAGE_NAME]
```

### Container for platform modelling
You can create a container that also share some source files describing your own platform [MY_PLATFORM]. Let us consider that the source files
describing you platform are located in [PLATFORM_MODEL_PATH] and the interference specifications are located in
[PLATFORM_INTERFERENCE_SPECIFICATION_PATH]. You can add these files as source code of the project by sharing them with the
container as follows:
```shell
# In case the user wants to create its own platform [MY_PLATFORM],
# Create share a folder to store the models [PATH_TO_MODEL]
# Create share a folder to store the specification [PATH_TO_INTERFERENCE_SPECIFICATION]
docker run -it --rm \
-v [PATH_TO_MODEL]:/home/user/code/src/main/scala/pml/examples/[MY_PLATFORM] \
-v [PATH_TO_INTERFERENCE_SPECIFICATION]:/home/user/code/src/main/scala/views/interference/examples/[MY_PLATFORM] \
-v [PATH_TO_ANALYSIS_RESULT]:/home/user/code/analysis \
-v [PATH_TO_EXPORT_RESULT]:/home/user/code/export \
[IMAGE_NAME]
```
### Running PML Analyzer in Docker

Once the container is run in an interactive mode, all the examples provided in the following sections can be run by using SBT.
To display all the possible entry-points of the project, especially your models, just execute the following command
```shell
sbt run
```
Note that you can edit your code on the host and re-build it in the container by simply running again
```shell
sbt compile
```

You can also indicate the memory allocated to SBT using the `-J-XmxNG` option where N
is the number of Go.
```shell
# Allocate 4Go to run SBT
sbt -J-Xmx4G run
```

### Exchanging files between container and host

The simplest way to retrieve files from the container or to modify internal files is to use the
`docker cp` command.
```shell
# From the container to the host
docker cp  CONTAINER:SRC_PATH DEST_PATH

# From the host to the container
docker cp  DEST_PATH CONTAINER:SRC_PATH 
```

A more integrated sharing can be achieved thanks to docker volumes as shown in the previous sections.

### Dealing with multiple images and containers
Some useful commands for image and container management in Docker:
```shell
# to list all existing containers
docker ps -a

# to run an stopped container
docker start [CONTAINER_NAME] -t

# to remove a given container
docker rm [CONTAINER_NAME]

# remove all containers
docker rm $(docker ps -a -q)

# list images
docker images

# remove an image
docker rmi [IMAGE NAME]
```

If you want to keep a container available after its creation please create the container as follows:
```shell
docker run -it -t [CONTAINER_NAME] [VOLUME_OPTIONS] [IMAGE_NAME]
```

You can then use it again by running
```shell
doker start -i [CONTAINER_NAME]
```