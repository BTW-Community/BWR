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

bwr.zip: mod/* hooks/* hooks.pl tmp/mcp
	cp -fR mod/* tmp/mcp/src/minecraft_server/net/minecraft/src/
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
	zip -r -1 ../../bwr.zip *

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

tmp/btw: btw.zip
	rm -rf tmp/btw
	mkdir -p tmp/btw
	cd tmp/btw &&\
	unzip -o ../../btw.zip

tmp/jar: svr.zip
	rm -rf tmp/jar
	mkdir -p tmp/jar
	cd tmp/jar &&\
	unzip -o ../../svr.zip
