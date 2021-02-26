export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export JUNIT_HOME=/home/ting/DataLoopBugDetection/soot-soot-2.5.0
export CLASSPATH=$JUNIT_HOME/junit-4.12.jar:$JUNIT_HOME/hamcrest-core-1.3.jar:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/jre/lib/jce.jar:/usr/share/java/scala-library.jar:$JUNIT_HOME/soot-2.5.0.jar

currentpath=$(pwd)
# mkdir -p $currentpath/output_compress451
# rm -rf $currentpath/output_compress451/*
path=/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin
libpath=/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/lib
libs=$libpath/guava-20.0.jar:$libpath/commons-lang-2.6.jar:$libpath/commons-logging-1.2.jar:$libpath/netty-3.7.0.Final-javadoc.jar:$libpath/netty-3.7.0.Final-sources.jar:$libpath/commons-io-2.6.jar:$libpath/netty-3.7.0.Final.jar:$libpath/commons-net-3.1.jar:$libpath/servlet-api-2.5.jar:$libpath/commons-collections-3.2.1.jar:$libpath/jackson-core-asl-1.9.13.jar:$libpath/guava-21.0.jar:$libpath/hadoop-common-0.23.0.jar:$libpath/hadoop-hdfs-0.23.0.jar:$libpath/hadoop-yarn-common-0.23.0.jar:$libpath/commons-configuration-1.6.jar:$libpath/hadoop-annotations-0.23.0.jar:$libpath/hadoop-auth-0.23.0.jar:$libpath/apache-cassandra-2.0.8-SNAPSHOT.jar:$libpath/compress-lzf-0.8.4.jar

cd $path
cp -n hive5235/* sootOutput/hive5235/ #copy but not override
cp -r typeMissingTimeout sootOutput
cd sootOutput/
java -cp $libs:$(pwd):$CLASSPATH hive5235.testcode