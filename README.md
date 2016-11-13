HDM
==============

HDM (Hierachy Distributed Matrix) is a light-weight, optimized, functional framework for data processing and analytics on large scale data sets.

# Quick start a HDM cluser


## start master

Users can start the master node of HDM by execute the shell cmd `./startip.sh master [port]?` under the root folder of hdm-core, for example:

```shell
cd ./hdm-core
./startup-master.sh
```

or

```shell
cd ./hdm-core
./startup.sh master 8999
```


## start a slave

Users can start a slave node by executing the shell cmd `./startup.sh slave [port of slave] [address of the master] [number of cores] [size of JVM memory] [port for data transferring]`

```shell
cd ./hdm-core
./startup.sh slave 10001 127.0.1.1:8999 2 3G 9001
```


## submit depdency to the server

Users can submit the dependency for an HDM application by executing the shell cmd: `./hdm-client.sh submit [master url] [application ID] [application version] [dependency file] [author]`

```shell
./hdm-client.sh submit "akka.tcp://masterSys@127.0.1.1:8999/user/smsMaster/ClusterExecutor"
  | "hdm-examples"
  | "0.0.1"
  | "/home/tiantian/Dev/workspace/hdm/hdm-benchmark/target/HDM-benchmark-0.0.1.jar"
  | dwu
```

## start HDM console

Users can start the HDM console by develop the `.war` file of hdm-console to any web server such as Apache Tomcat or Jetty.



# Programming in HDM


## Primitives


## Actions


## Results Collection


## Examples


# Message Queries to HDM server


# Build HDM from source code

