cd src/main/java/org/example

javac -h . Main.java

g++ -c -I"C:\Program Files\Java\jdk-17\include" -I"C:\Program Files\Java\jdk-17\include\win32" Main.cpp -o Main.o

g++ -shared -o volume_info.dll Main.o -Wl,--add-stdcall-alias

java -cp . -Djava.library.path="." Main.java
