# ==============================================================================
# Copyright (C)2013 by Aaron Suen <warr1024@gmail.com>
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

BTW=BTWMod4-42.zip
MCP=mcp726a.zip
SVR=minecraft_server.jar

# Primary build target: from a patched MCP with decompiled BTW server,
# apply BWR patches and build BWR server.
bwr_btw_${SVR}: src/* hooks/* hooks.pl mcp tmp/jar tmp/btw
	cp -fR src/* mcp/src/minecraft_server/net/minecraft/src/
	perl -w hooks.pl
	rm -rf mcp/bin
	cd mcp &&\
	python runtime/recompile.py --server
	perl -w checkbin.pl
	cd mcp &&\
	python runtime/reobfuscate.py --server
	mkdir -p tmp/bwrjar
	cd tmp/bwrjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	cp -fR ../../mcp/reobf/minecraft_server/* . &&\
	zip -r -1 ../../bwr_btw_${SVR}.new *
	mv -f bwr_btw_${SVR}.new bwr_btw_${SVR}

# From a patched BTW server and MCP archive, unpack and patch MCP,
# and decompile the BTW server.
mcp: tmp/btw_${SVR} ${MCP}
	mkdir -p mcp
	cd mcp &&\
	unzip -o ../${MCP} &&\
	cp -fR ../tmp/btw_${SVR} jars/minecraft_server.jar
	perl -w mcppatch.pl
	cd mcp &&\
	python runtime/decompile.py --server --noreformat

# Create a patched BTW server from unpacked Vanilla server and
# unpacked BTW archive.
tmp/btw_${SVR}: tmp/btw tmp/jar
	mkdir -p tmp/btwjar
	cd tmp/btwjar &&\
	cp -fR ../jar/* . &&\
	cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
	zip -r -1 ../btw_${SVR} *

# Unpack the BTW archive.
tmp/btw: ${BTW}
	rm -rf tmp/btw
	mkdir -p tmp/btw
	cd tmp/btw &&\
	unzip -o ../../${BTW}

# Unpack the Vanilla server.
tmp/jar: ${SVR}
	rm -rf tmp/jar
	mkdir -p tmp/jar
	cd tmp/jar &&\
	unzip -o ../../${SVR}

# Clean up all intermediate and final output.
clean:
	rm -rf mcp tmp bwr_btw_${SVR} bwr_btw_${SVR}.new

# Download target for the Vanilla minecraft server.  The user needs
# to follow instructions and download it.
${SVR}:
	#------------------------------------------------------------------------ 
	# You need to download the appropriate version of ${SVR}
	# from www.minecraft.net and place it in the project's root directory.
	#------------------------------------------------------------------------ 
	exit 1

# Download target for the MCP archive.  The user needs to follow
# instructions and download it.
${MCP}:
	#------------------------------------------------------------------------ 
	# You need to download the appropriate version of Minecraft Coder's Pack
	# from mcp.ocean-labs.de and place it in the project's root directory.
	# The expected version is ${MCP}; check for project updates.
	#------------------------------------------------------------------------ 
	exit 1

# Download target for the BTW archive.  The user needs to follow
# instructions and download it.
${BTW}:
	#------------------------------------------------------------------------ 
	# You need to download the appropriate version of Better Than Wolves
	# from sargunster.com and place it in the project's root directory.
	# The expected version is ${BTW}; check for project updates.
	#------------------------------------------------------------------------ 
	exit 1
