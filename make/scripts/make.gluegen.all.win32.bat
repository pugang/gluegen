set THISDIR="C:\JOGL"

set J2RE_HOME=c:\jre1.7.0_45_x32
set JAVA_HOME=c:\jdk1.7.0_45_x32
set ANT_PATH=C:\apache-ant-1.8.2

set PATH=%JAVA_HOME%\bin;%ANT_PATH%\bin;c:\mingw\bin;%PATH%

set LIB_GEN=%THISDIR%\lib
set CLASSPATH=.;%THISDIR%\build-win32\classes
REM    -Dc.compiler.debug=true 
REM    -DuseOpenMAX=true 
REM    -DuseKD=true
REM    -Djogl.cg=1 -D-Dwindows.cg.lib=C:\Cg-2.2

set SOURCE_LEVEL=1.6
set TARGET_LEVEL=1.6
set TARGET_RT_JAR=C:\jre1.6.0_30\lib\rt.jar

REM set JOGAMP_JAR_CODEBASE=Codebase: *.jogamp.org
set JOGAMP_JAR_CODEBASE=Codebase: *.goethel.localnet

ant -Drootrel.build=build-win32 %1 %2 %3 %4 %5 %6 %7 %8 %9 > make.gluegen.all.win32.log 2>&1

