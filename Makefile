# ==============================================================================
# Copyright (C)2018 by Aaron Suen <warr1024@gmail.com>
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
# ------------------------------------------------------------------------------

BTW=BTWMod4-ABCEEFABc.zip
MCP=mcp751.zip
SVR=minecraft_server.jar

dock: ${SVR} ${MCP} ${BTW}
	docker build \
		--build-arg BTW="${BTW}" \
		--build-arg MCP="${MCP}" \
		--build-arg SVR="${SVR}" \
		--tag bwr:latest .

bwr.zip: src/* hooks/* hooks.pl mcp tmp/jar tmp/btw
	cp -fR src/* mcp/src/minecraft_server/net/minecraft/src/
	perl -w hooks.pl
	rm -rf mcp/bin
	cd mcp &&\
	python2.7 runtime/recompile.py --server
	perl -w checkbin.pl
	cd mcp &&\
	python2.7 runtime/reobfuscate.py --server
	mkdir -p tmp/bwrjar
	cd tmp/bwrjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	cp -fR ../../mcp/reobf/minecraft_server/* . &&\
	zip -r -1 ../../bwr_btw_${SVR}.new *
	mv -f bwr_btw_${SVR}.new bwr_btw_${SVR}

mcp: tmp/mc_btw.jar mcp.zip
	mkdir -p mcp
	cd mcp &&\
	unzip -o ../mcp.zip  &&\
	cp -fR ../tmp/mc_btw.jar jars/minecraft_server.jar
	cd mcp &&\
	python2.7 runtime/decompile.py --server --noreformat

tmp/mc_btw.jar: tmp/btw tmp/jar
	mkdir -p tmp/btwjar
	cd tmp/btwjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	zip -r -1 ../mc_btw.jar *

tmp/btw:
	rm -rf tmp/btw
	mkdir -p tmp/btw
	cd tmp/btw &&\
	unzip -o ../../btw.zip

tmp/jar:
	rm -rf tmp/jar
	mkdir -p tmp/jar
	cd tmp/jar &&\
	unzip -o ../../svr.zip

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
