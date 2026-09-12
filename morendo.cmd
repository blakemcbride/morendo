@echo off
rem Starts Morendo:  morendo -shell   (interactive CLIPS shell)
rem                  morendo -gui     (Swing GUI)
rem Works from a checkout after "bld build" (target\classes) and from an unpacked
rem distribution (morendo.jar). Logging options: see log4j2.xml in the jar / src\main\resources.
set DIR=%~dp0
if exist "%DIR%morendo.jar" (
    set CP=%DIR%morendo.jar;%DIR%libs\*
) else (
    set CP=%DIR%target\classes;%DIR%libs\*
)
java -Xms256m -Xmx1g -cp "%CP%" org.jamocha.Morendo %*
