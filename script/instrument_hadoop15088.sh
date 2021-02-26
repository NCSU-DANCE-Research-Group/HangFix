
#######################the website#############################
#https://www.sable.mcgill.ca/soot/tutorial/profiler2/index.html
###############################################################





export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export JUNIT_HOME=/home/ting/DataLoopBugDetection/soot-soot-2.5.0
export CLASSPATH=$JUNIT_HOME/junit-4.12.jar:$JUNIT_HOME/hamcrest-core-1.3.jar:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/jre/lib/jce.jar:/usr/share/java/scala-library.jar:$JUNIT_HOME/soot-2.5.0.jar

currentpath=$(pwd)
mkdir -p $currentpath/output_hadoop15088
rm -rf $currentpath/output_hadoop15088/*
path=/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin
cd $path
cp $currentpath/confFiles/conf_hadoop15088 $path/conf
java -cp $(pwd):$CLASSPATH typeIOReturn.MainDriver hadoop15088.StreamXmlRecordReader