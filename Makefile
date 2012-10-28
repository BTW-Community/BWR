BTW=BTWMod4-30.zip
MCP=mcp72.zip
SVR=minecraft_server.jar

bwr_btw_${SVR}: src/* hooks.pl mcp tmp/jar tmp/btw
	cp -fR src/* mcp/src/minecraft_server/net/minecraft/src/
	perl -w hooks.pl
	cd mcp &&\
	python runtime/recompile.py &&\
	python runtime/reobfuscate.py
	mkdir -p tmp/bwrjar
	cd tmp/bwrjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	cp -fR ../../mcp/reobf/minecraft_server/* . &&\
	7z a -y -tzip -mx=9 ../../bwr_btw_${SVR}.new *
	mv -f bwr_btw_${SVR}.new bwr_btw_${SVR}

mcp: tmp/btw_${SVR} ${MCP}
	mkdir -p mcp
	cd mcp &&\
	7z x -y ../${MCP} &&\
	cp -fR ../tmp/btw_${SVR} jars/minecraft_server.jar &&\
	perl -w ../mcppatch.pl &&\
	python runtime/decompile.py

tmp/btw_${SVR}: tmp/btw tmp/jar
	mkdir -p tmp/btwjar
	cd tmp/btwjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	7z a -y -tzip -mx=1 ../btw_${SVR} *

tmp/btw: ${BTW}
	rm -rf tmp/btw
	mkdir -p tmp/btw
	cd tmp/btw &&\
	7z x -y ../../${BTW}

tmp/jar: ${SVR}
	rm -rf tmp/jar
	mkdir -p tmp/jar
	cd tmp/jar &&\
	7z x -y ../../${SVR}
	
clean:
	rm -rf mcp tmp bwr_btw_${SVR} bwr_btw_${SVR}.new

${SVR}:
	#------------------------------------------------------------------------ 
	# You need to download the appropriate version of ${SVR}
	# from www.minecraft.net and place it in the project's root directory.
	#------------------------------------------------------------------------ 
	exit 1

${MCP}:
	#------------------------------------------------------------------------ 
	# You need to download the appropriate version of Minecraft Coder's Pack
	# from mcp.ocean-labs.de and place it in the project's root directory.
	# The expected version is ${MCP}; check for project updates.
	#------------------------------------------------------------------------ 
	exit 1

${BTW}:
	#------------------------------------------------------------------------ 
	# You need to download the appropriate version of Better Than Wolves
	# from sargunster.com and place it in the project's root directory.
	# The expected version is ${BTW}; check for project updates.
	#------------------------------------------------------------------------ 
	exit 1
