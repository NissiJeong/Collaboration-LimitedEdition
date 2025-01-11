@ECHO OFF
setLocal EnableDelayedExpansion
for /R ./lib %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!
java -server -cp "lib/ngrinder-core-3.5.9-p1.jar;lib/ngrinder-runime-3.5.9-p1.jar;%CLASSPATH%" org.ngrinder.NGrinderAgentStarter --mode agent --command run %*
