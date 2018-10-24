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

BWR=0.27.04ABCEEFABc
BTW=BTWMod4-ABCEEFABc.zip
MCP=mcp751.zip
SVR=minecraft_server.jar
REPO=btwbwr

DOCKARGS=	--build-arg BWR="${BWR}" \
		--build-arg BTW="${BTW}" \
		--build-arg MCP="${MCP}" \
		--build-arg SVR="${SVR}" \
		--build-arg REPO="${REPO}"

dev: devpre
	docker build --tag "${REPO}-dev:latest" \
		${DOCKARGS} -f Dockerfile.dev .
	docker run -i --rm "${REPO}-dev:latest" cat bwr.zip >bwr_btw_minecraft_server.jar
	unzip -t bwr_btw_minecraft_server.jar
	
devpre: base
	docker build --tag "${REPO}-dev:pre" \
		${DOCKARGS} -f Dockerfile.devpre .

testdist: dist
	docker build --tag "${REPO}-dev:testdist" \
		${DOCKARGS} -f Dockerfile.testdist .
	docker run -i --rm "${REPO}-dev:testdist" >bwr_btw_minecraft_server.jar
	unzip -t bwr_btw_minecraft_server.jar
	
dist: base
	cd src && docker build --tag "${REPO}:dist" \
		${DOCKARGS} -f Dockerfile.dist .

base:
	cd src && docker build --tag "${REPO}:base" \
		${DOCKARGS} -f Dockerfile.base .

clean:
	rm -f bwr_btw_minecraft_server.jar
	docker image rm "${REPO}:base" "${REPO}:dist" "${REPO}-dev:pre" "${REPO}-dev:latest" "${REPO}-dev:testdist"

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
