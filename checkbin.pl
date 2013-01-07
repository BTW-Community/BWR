#!/usr/bin/perl -w
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
# This script checks the bin/ folder under MCP to make sure that all BWR java
# files successfully compiled into classes.  For some reason, when the compile
# failes, MCP still returns a successful code, so this does a further sanity
# check to halt the build if recompile failed.

use strict;
use warnings;

# Paths for the compiled output, and BWR-specific
# source, respectively.
my $binpath = 'mcp/bin/minecraft_server/net/minecraft/src';
my $srcpath = 'src';

my %classes = ( );
my $dh;

# Identify all classes defined in source.
opendir($dh, $srcpath) or die($!);
while(my $e = readdir($dh))
	{
	$e =~ m#(.*)\.java$# and $classes{$1} = 1;
	}
closedir($dh);

# Mark off all compiled classes.
opendir($dh, $binpath) or die($!);
while(my $e = readdir($dh))
	{
	$e =~ m#(.*)\.class$# or next;
	$classes{$1} and delete $classes{$1};
	}
closedir($dh);

# If any source classes exist for which a compiled version
# was not marked off, report them as an error.
scalar(keys %classes) and die('Missing classes:'
	. join(', ', sort(keys %classes)));
