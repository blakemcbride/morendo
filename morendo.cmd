@echo off
rem Starts Morendo:  morendo -shell   (interactive CLIPS shell)
rem                  morendo -gui     (Swing GUI)
rem Works from a checkout after "bld build" (target\classes) and from an unpacked
rem distribution (morendo.jar). Run it from this directory: log4j.properties is read
rem from the current directory.
set DIR=%~dp0
if exist "%DIR%morendo.jar" (
    set CP=%DIR%morendo.jar;%DIR%libs\*
) else (
    set CP=%DIR%target\classes;%DIR%libs\*
)
java -Xms256m -Xmx1g -cp "%CP%" org.jamocha.Morendo %*
