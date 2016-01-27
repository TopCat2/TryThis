##java -jar target/hello-world-0.0.1-SNAPSHOT.jar  jobs/demoOne.xml demoOneJob \
##     inDirectoryName="$1" inFileName="$2" eventName="$3"
java -cp ~/IdeaProjects/try/config:target/classes:target/hello-world-0.0.1-SNAPSHOT.jar  \
     org.springframework.batch.core.launch.support.CommandLineJobRunner \
     jobs/demoOne.xml demoOneJob \
     inDirectoryName="$1" inFileName="$2" eventName="$3" -next
