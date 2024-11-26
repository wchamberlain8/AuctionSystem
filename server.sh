javac *.java &
rmiregistry & 
sleep 1
java FrontEnd &
java Replica 1 &
java Replica 2 &
java Replica 3
