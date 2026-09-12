@echo off
rem Starts Morendo:  morendo -shell   (interactive CLIPS shell)
rem                  morendo -gui     (Swing GUI)
rem Works from a checkout after "bld build" (target\<module>\classes) and from an unpacked
rem distribution (module jars in libs\). Logging options: see log4j2.xml in the core jar.
set DIR=%~dp0
set CP=%DIR%libs\*
for /d %%m in ("%DIR%target\*") do if exist "%%m\classes" set CP=%%m\classes;%CP%
java -Xms256m -Xmx1g -cp "%CP%" org.jamocha.Morendo %*
