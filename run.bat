@echo off
for /f "tokens=*" %%a in (.env) do (
    set "%%a"
)
java -jar target/demo-0.0.1-SNAPSHOT.jar