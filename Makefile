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

build: img-build ${SVR} ${MCP} ${BTW}
	docker rm -f bwrbuild ||:
	docker run -d --name bwrbuild bwr:build
	docker exec -i bwrbuild sh -c 'cat >svr.zip' <${SVR}
	docker exec -i bwrbuild sh -c 'cat >btw.zip' <${BTW}
	docker exec -i bwrbuild sh -c 'cat >mcp.zip' <${MCP}
	docker exec -i bwrbuild sh build.sh
	docker exec -i bwrbuild cat bwr.zip >bwr_btw_minecraft_server.jar
	[ -s bwr_btw_minecraft_server.jar ]
	
gui: img-gui
	docker run -it --rm --name bwrgui -p 127.0.0.1:4280:4280/tcp bwr:latest

img-gui: img-build
	cd src && docker build --tag bwr:latest -f Dockerfile.gui .
	
img-build:
	cd src && docker build --tag bwr:build -f Dockerfile.build .

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
