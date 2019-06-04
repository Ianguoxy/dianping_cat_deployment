# How to use docker to deploy the CAT server.

Env:
CAT 3.0.0

Ubuntu 14.04

1.Install docker and docker-compose 

https://docs.docker.com/install/linux/docker-ce/ubuntu/

https://docs.docker.com/compose/install/

2. download cat-master from github.

        $ mkdir cat
        $ cd cat
        $ wget https://github.com/dianping/cat/archive/master.zip
        ...
        $ unzip master.zip
        $ ls
        cat-master  master.zip
        $ cd cat-master 
        $ ls
        cat-alarm  cat-client  cat-consumer  cat-core  cat-hadoop  cat-home  docker  integration  java_formatter.xml  lib  LICENSE  NOTICE.txt  pom.xml  README.md  script
        


3. Update Config file:

1> create mysql schema in own db.
   
      /cat-master$ cd script
      /cat-master/script$ ls
      CatApplication.sql
      
      /cat-master/script$ sudo vim CatApplication.sql
      
      -------------------------
      #Add sql : Create SCHEMA cat for CAT Server
      CREATE SCHEMA IF NOT EXISTS `cat` DEFAULT CHARACTER SET utf8 ;
      USE `cat`;
      ...
      ...
      -------------------------
      
      /cat-master/script$ mysql -uroot -p < CatApplication.sql
      

2> use own mysql db also need to update mysql config below in docker-compose.yml:

    /cat-master$ cd docker
    /cat-master/docker$ ls
    client.xml  datasources.sh  datasources.xml  docker-compose.yml  Dockerfile
    $sudo vim docker-compose.yml

         ------------------------------------------------------------------------------------- 
          # mail@dongguochao.com
          version: '2.2'
          services:
            cat:
              container_name: cat
              ######## build from Dockerfile ###########
              build:
                context: ../
                dockerfile: ./docker/Dockerfile
              ######## End -> build from Dockerfile ###########
              environment:
                # if you have your own mysql, config it here, and disable the 'mysql' config blow
                - MYSQL_URL=192.168.18.180 # links will maintain /etc/hosts, just use 'container_name'
                - MYSQL_PORT=3306
                - MYSQL_USERNAME=root
                - MYSQL_PASSWD=123456
                - MYSQL_SCHEMA=cat
              working_dir: /app
              volumes:
                # 默认127.0.0.1，可以修改为自己真实的服务器集群地址
                - "./client.xml:/data/appdatas/cat/client.xml"
                # 默认使用环境变量设置。可以启用本注解，并修改为自己的配置
          #      - "./datasources.xml:/data/appdatas/cat/datasources.xml"
              command: /bin/sh -c 'chmod +x /datasources.sh && /datasources.sh && catalina.sh run'
          #    links:
          #      - mysql
          #    depends_on:
          #      - mysql
              ports:
                - "8080:8080"
                - "2280:2280"
            # disable this if you have your own mysql
          #  mysql:
          #    container_name: cat-mysql
          #    image: mysql:5.7.22
              # expose 33306 to client (navicat)
          #    ports:
          #       - 33306:3306
          #    volumes:
                # change './docker/mysql/volume' to your own path
                # WARNING: without this line, your data will be lost.
          #      - "./mysql/volume:/var/lib/mysql"
                # 第一次启动，可以通过命令创建数据库表 ：
                # docker exec 容器id bash -c "mysql -uroot -Dcat < /init.sql"
          #      - "../script/CatApplication.sql:/init.sql"
          #    command: mysqld -uroot --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --init-connect='SET NAMES utf8mb4;' --innodb-flush-log-at-trx-commit=0
          #    environment:
          #      MYSQL_ALLOW_EMPTY_PASSWORD: "true"
          #      MYSQL_DATABASE: "cat"
          #      MYSQL_USER: "root"
          #      MYSQL_PASSWORD: ""
       ------------------------------------------------------------------------------------- 



4. Docker deployment  https://github.com/dianping/cat/wiki/readme_server


        /cat-master$ cd docker
        /cat-master/docker$ docker-compose build    #build and start cat tomcat, this step shall take long time...
        ...
        ...
        [INFO] ------------------------------------------------------------------------
        [INFO] Reactor Summary:
        [INFO] 
        [INFO] parent ............................................. SUCCESS [ 50.022 s]
        [INFO] cat-client ......................................... SUCCESS [03:14 min]
        [INFO] cat-core ........................................... SUCCESS [ 10.403 s]
        [INFO] cat-hadoop ......................................... SUCCESS [01:09 min]
        [INFO] cat-consumer ....................................... SUCCESS [  4.527 s]
        [INFO] cat-alarm .......................................... SUCCESS [  5.830 s]
        [INFO] cat-home ........................................... SUCCESS [ 45.086 s]
        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESS
        [INFO] ------------------------------------------------------------------------
        [INFO] Total time: 06:21 min
        [INFO] Finished at: 2019-05-30T11:02:01Z
        [INFO] Final Memory: 45M/380M
        
        
        /cat-master/docker$ docker-compose start cat   #start cat server tomcat
        Starting cat ... done
        
        /cat-master/docker$ docker-compose logs cat   #check cat server log
        cat    | May 30, 2019 10:45:38 AM org.apache.coyote.AbstractProtocol start
        cat    | INFO: Starting ProtocolHandler ["http-bio-8080"]
        cat    | May 30, 2019 10:45:38 AM org.apache.coyote.AbstractProtocol start
        cat    | INFO: Starting ProtocolHandler ["ajp-bio-8009"]
        cat    | May 30, 2019 10:45:38 AM org.apache.catalina.startup.Catalina start
        cat    | INFO: Server startup in 9440 ms
        cat    | [INFO] Working directory is /app
        cat    | [INFO] War root is /usr/local/tomcat/webapps/cat
        
5.visit http://localhost:8080/cat/r  the page shows "CAT服务端有问题" 
         
6. Update CAT Server config 

    Cat有三个重要配置, 分别是: ClientConfig, RouterConfig 和 ServerConfig, 分别代表客户端信息, 服务路由信息以及服务端配置信息.

    ClientConfig 配置(即client.xml 配置文件):　配置对应的那个cat server的ip，因此每个客户端都需要在目录/data/appdatas/cat 下面添加改配置文件.
    每个客户端对应一组对服务器以及一个domain信息, 客户端默认从这组服务中的一个节点上拉取路由配置信息. 客户端的查询参数里带有domain信息, 服务端的路由配置里如果有相应的domain则返回相应domain下的一组server信息, 如果没有则返回default servers.
    返回的路由信息包含一组日志服务节点以及采样比例(sample), 日志服务节点包含权重, socket端口号以及id(ip). 采样比例是指客户端的cat日志多少次里抽样发送1次, 例如0.2则代表记录5次日志会忘服务端发送1次.

    RouterConfig 配置(通过 url 配置):　
    客户端拉取到router信息后, 和router的日志server列表中第一个可用server之间建立netty channel, 并启动一个线程对channel进行维护. 对channel的维护主要包括:
    1. 比较服务端路由信息和客户端上次抓取的是否一致, 不一致则更新客户端router信息, 并重新建立新channel.
    2. 判断当前channel状态, 如果状态不正常, 则从router的server列表里重新找出一个能用的server建立channel.
    客户端拉取不到router信息时, 默认使用客户端下的server列表作为远程日志服务器组.

    ServerConfig 配置(通过 url 配置):
    ServerConfig主要用于服务端节点的职能描述, 主要有以下几类功能:
    1. 定义服务节点职能, 可以运行哪类任务, 以及是否可以发送告警信息和是否是hdfs存储节点.
    2. 通过consoleConfig定义相应报表数据的获取节点, 一般用于告警节点远程拉取所有节点的报表数据进行筛选.
    3. ConsumerConfig定义各种类型的事务时间阈值, 从名字可以看出来分别定义url, sql以及cache类型的事务时间的阈值, 超过这个时间会被认为是一个problem. 


  1> get inner ip of cat tomcat server from doker container.
  
        /cat-master/docker$ docker inspect cat
        ---------------------------------------
        [{{
        
        "Networks": {
                "docker_default": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": [
                        "603d27193af5",
                        "cat"
                    ],
                    "NetworkID": "7101e07fc90ba878b3261b6fc611eb254c8a92d57b19c2af6f81ddce6175879a",
                    "EndpointID": "3fe3d96e5f1f9581b85f55fcf54002d063a97bac190238009f35a9bed5cec419",
                    "Gateway": "172.19.0.1",
                    "IPAddress": "172.19.0.2",   # inner ip of doker container
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "02:42:ac:13:00:02",
                    "DriverOpts": null
                }
            }
        }}]
        

2>Update config at http://localhost:8080/cat/s/config    u:admin  p:admin
 
  (1)ServerConfig: http://localhost:8080/cat/s/config?op=serverConfigUpdate
    
          <?xml version="1.0" encoding="utf-8"?>
            <server-config>
               <server id="default">
                  <properties>
                     <property name="local-mode" value="false"/>
                     <property name="job-machine" value="false"/>
                     <property name="send-machine" value="false"/>
                     <property name="alarm-machine" value="false"/>
                     <property name="hdfs-enabled" value="false"/>
                     <property name="remote-servers" value="172.19.0.2:8080"/>
                  </properties>
                  <storage local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="2" local-logivew-storage-time="1" har-mode="true" upload-thread="5">
                     <hdfs id="dump" max-size="128M" server-uri="hdfs://172.19.0.2/" base-dir="/user/cat/dump"/>
                     <harfs id="dump" max-size="128M" server-uri="har://172.19.0.2/" base-dir="/user/cat/dump"/>
                     <properties>
                        <property name="hadoop.security.authentication" value="false"/>
                        <property name="dfs.namenode.kerberos.principal" value="hadoop/dev80.hadoop@testserver.com"/>
                        <property name="dfs.cat.kerberos.principal" value="cat@testserver.com"/>
                        <property name="dfs.cat.keytab.file" value="/data/appdatas/cat/cat.keytab"/>
                        <property name="java.security.krb5.realm" value="value1"/>
                        <property name="java.security.krb5.kdc" value="value2"/>
                     </properties>
                  </storage>
                  <consumer>
                     <long-config default-url-threshold="1000" default-sql-threshold="100" default-service-threshold="50">
                        <domain name="cat" url-threshold="500" sql-threshold="500"/>
                        <domain name="OpenPlatformWeb" url-threshold="100" sql-threshold="500"/>
                     </long-config>
                  </consumer>
               </server>
               <server id="172.19.0.2">
                  <properties>
                     <property name="job-machine" value="true"/>
                     <property name="send-machine" value="true"/>
                     <property name="alarm-machine" value="true"/>
                  </properties>
               </server>
            </server-config>
            
 (2)RouterConfig: http://localhost:8080/cat/s/config?op=routerConfigUpdate
      
      
          <?xml version="1.0" encoding="utf-8"?>
            <router-config backup-server="172.19.0.2" backup-server-port="2280">
               <default-server id="172.19.0.2" weight="1.0" port="2280" enable="true"/>
               <network-policy id="default" title="默认" block="false" server-group="default_group">
               </network-policy>
               <server-group id="default_group" title="default-group">
                  <group-server id="172.19.0.2"/>
               </server-group>
               <domain id="cat">
                  <group id="default">
                     <server id="172.19.0.2" port="2280" weight="1.0"/>
                  </group>
               </domain>
            </router-config>

  (3)ClientConfig: client.xml  
  
        #进入cat server的 container 内
        $ sudo docker exec -it 603d27193af5 /bin/bash 
        [root@603d27193af5 app]# cd /data/appdatas/cat
        [root@603d27193af5 cat]# vi client.xml
                <?xml version="1.0" encoding="utf-8"?>
                <config mode="client">
                    <servers>
                        <server ip="172.19.0.2" port="2280" http-port="8080"/>
                    </servers>
                </config>
        
        [root@603d27193af5 app] cd /usr/local/tomcat/bin
        [root@603d27193af5 bin] ./shutdown.sh
        ...
        [root@603d27193af5 bin] ./startup.sh
        ...
        Tomcat started...
        
  
3> restart tomcat to make the updated configuration effect
  
         /cat-master/docker$ docker-compose restart cat   #restart cat server tomcat
        
  
7.visit http://localhost:8080/cat/r  the page shows "CAT服务端正常" 
  
