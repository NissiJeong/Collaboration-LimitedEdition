#!/bin/sh
java -server -cp "lib/ngrinder-core-3.5.9-p1.jar:lib/ngrinder-runime-3.5.9-p1.jar:lib/*" org.ngrinder.NGrinderAgentStarter --mode=agent --command=run $@
