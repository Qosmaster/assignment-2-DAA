@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -Xlint:all -d out src\*.java
if errorlevel 1 exit /b 1
java -cp out Tests
if errorlevel 1 exit /b 1
java -Xms256m -Xmx1g -cp out Benchmark
if errorlevel 1 exit /b 1
