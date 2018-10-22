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
	mkdir -p tmp
	docker run -i --rm -v `pwd`/tmp:/home/user/tmp bwr:latest make tmp/bwr.zip
	[ -s tmp/bwr.zip ] && ln -f tmp/bwr.zip bwr_btw_minecraft_server.jar

tmp/bwr.zip: src/* hooks/* hooks.pl tmp/mcp tmp/jar tmp/btw
	cp -fR src/* tmp/mcp/src/minecraft_server/net/minecraft/src/
	perl -w hooks.pl hooks/BWR.*.pl
	rm -rf tmp/mcp/bin
	cd tmp/mcp &&\
	python2.7 runtime/recompile.py --server
	perl -w checkbin.pl
	cd tmp/mcp &&\
	python2.7 runtime/reobfuscate.py --server
	mkdir -p tmp/bwrjar
	cd tmp/bwrjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	cp -fR ../mcp/reobf/minecraft_server/* . &&\
	zip -r -1 ../new.zip *
	mv -f tmp/new.zip tmp/bwr.zip

tmp/mcp: tmp/mc_btw.jar mcp.zip
	mkdir -p tmp/mcp
	cd tmp/mcp &&\
	unzip -o ../../mcp.zip  &&\
	cp -fR ../mc_btw.jar jars/minecraft_server.jar &&\
	python2.7 runtime/decompile.py --server --noreformat --norecompile
	perl -w hooks.pl hooks/MCP.*.pl
	rm -rf tmp/mcp/bin
	cd tmp/mcp &&\
	python2.7 runtime/recompile.py --server
	cd tmp/mcp &&\
	python2.7 runtime/reobfuscate.py --server
	cd tmp/mcp &&\
	python2.7 runtime/updatemd5.py --server

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

clean:
	rm -rf bwr_btw_minecraft_server.jar tmp

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
