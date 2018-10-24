#!/bin/sh
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

die() {
	echo "ERROR: $*"
	cat instruct.txt
	exit 1
}
[ -n "$SVR" ] || die "env SVR not set"
[ -s "$SVR" ] || die "Minecraft server jar '$SVR' not found"
[ -n "$MCP" ] || die "env MCP not set"
[ -s "$MCP" ] || die "Mod Coder's Pack '$MCP' not found"
[ -n "$BTW" ] || die "env BTW not set"
[ -s "$BTW" ] || die "Better Than Wolves zip '$BTW' not found"

set -ex

# Unpack minecraft server.
rm -rf tmp/jar
mkdir -p tmp/jar
( cd tmp/jar &&\
  unzip -o ../../"$SVR" )

# Unpack BTW.
rm -rf tmp/btw
mkdir -p tmp/btw
( cd tmp/btw &&\
  unzip -o ../../"$BTW" )

# Create vanilla server patched with BTW.
rm -rf tmp/btwjar
mkdir -p tmp/btwjar
( cd tmp/btwjar &&\
  cp -fR ../jar/* . &&\
  cp -fR ../btw/MINECRAFT_SERVER-JAR/* . &&\
  zip -r -1 ../mc_btw.jar * )

# Decompile server with MCP.
rm -rf tmp/mcp
mkdir -p tmp/mcp
( cd tmp/mcp &&\
  unzip -o ../../"$MCP" &&\
  cp -fR ../mc_btw.jar jars/minecraft_server.jar &&\
python2.7 runtime/decompile.py --server --noreformat --norecompile )

# Apply MCP hotfixes.
perl -w hooks.pl hooks-mcp/*.pl

# Recompile vanilla + BTW server, generate checksums.
rm -rf tmp/mcp/bin
( cd tmp/mcp &&\
  python2.7 runtime/recompile.py --server &&\
  python2.7 runtime/reobfuscate.py --server &&\
  python2.7 runtime/updatemd5.py --server )
