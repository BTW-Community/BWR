BTW=BTWMod4-30.zip
MCP=mcp72.zip
SVR=minecraft_server.jar

bwr_btw_${SVR}: src/* hooks.pl mcp
	cp -R src/* mcp/src/minecraft_server/net/minecraft/src/
	perl -w hooks.pl
	cd mcp &&\
	python runtime/recompile.py &&\
	python runtime/reobfuscate.py
	cp mcp/temp/server_reobf.jar bwr_btw_${SVR}

mcp: btw_${SVR} ${MCP}
	mkdir -p mcp
	cd mcp &&\
	7z x ../${MCP} &&\
	cp ../btw_${SVR} jars/minecraft_server.jar &&\
	perl -w ../mcppatch.pl &&\
	python runtime/decompile.py

btw_${SVR}: ${BTW} ${SVR}
	mkdir -p tmp/btw
	cd tmp/btw &&\
	7z x ../../${BTW}
	mkdir -p tmp/jar
	cd tmp/jar &&\
	7z x ../../${SVR} &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	7z a -tzip -mx=1 ../../btw_${SVR} *
	
clean:
	rm -rf mcp tmp btw_${SVR} bwr_btw_${SVR}

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
