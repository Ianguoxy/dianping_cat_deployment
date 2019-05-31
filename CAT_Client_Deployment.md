 # deploy cat client
 
 
 1.  !!!! must make dir for config file and log file .
 
https://github.com/dianping/cat/blob/master/lib/_/preparations.md
 
 2.add java file and must add dependencies below in your pom.xml
 
 
 https://github.com/dianping/cat/blob/master/integration/spring-boot/CatFilterConfigure.java
 
 
 --------------------------------------------------------------
            <dependencies>
            
              <dependency>
                      <groupId>com.dianping.cat</groupId>
                      <artifactId>cat-client</artifactId>
                      <version>3.0.0</version>
                  </dependency>
                  
                  </dependencies>
                  
                   <repositories>
                        <repository>
                           <id>central</id>
                           <name>Maven2 Central Repository</name>
                           <layout>default</layout>
                           <url>http://repo1.maven.org/maven2</url>
                        </repository>
                        <repository>
                           <id>unidal.releases</id>
                           <url>http://unidal.org/nexus/content/repositories/releases/</url>
                        </repository>
                       </repositories>

                  <pluginRepositories>
                        <pluginRepository>
                           <id>central</id>
                           <url>http://repo1.maven.org/maven2</url>
                        </pluginRepository>
                        <pluginRepository>
                           <id>unidal.releases</id>
                           <url>http://unidal.org/nexus/content/repositories/releases/</url>
                        </pluginRepository>
                     </pluginRepositories>
 
 
 https://github.com/dianping/cat/blob/master/lib/java/README.md
 
