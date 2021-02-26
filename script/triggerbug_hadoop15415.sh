
#######################the website#############################
#https://www.sable.mcgill.ca/soot/tutorial/profiler2/index.html
###############################################################





export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export JUNIT_HOME=/home/ting/DataLoopBugDetection/soot-soot-2.5.0
export CLASSPATH=$JUNIT_HOME/junit-4.12.jar:$JUNIT_HOME/hamcrest-core-1.3.jar:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/jre/lib/jce.jar:/usr/share/java/scala-library.jar:$JUNIT_HOME/soot-2.5.0.jar

path=/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin
libpath=/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/lib
libs=$libpath/guava-20.0.jar:$libpath/commons-lang-2.6.jar:$libpath/commons-logging-1.2.jar:$libpath/netty-3.7.0.Final-javadoc.jar:$libpath/netty-3.7.0.Final-sources.jar:$libpath/commons-io-2.6.jar:$libpath/netty-3.7.0.Final.jar:$libpath/commons-net-3.1.jar:$libpath/servlet-api-2.5.jar:$libpath/commons-collections-3.2.1.jar:$libpath/jackson-core-asl-1.9.13.jar:$libpath/guava-21.0.jar:$libpath/mockito-all-1.8.2.jar
cd $path
java -cp $libs:$(pwd):$CLASSPATH:/usr/share/java/junit4.jar org.junit.runner.JUnitCore hadoop15415.TestIOUtils