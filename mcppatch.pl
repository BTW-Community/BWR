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
# This script is a minor patch to MCP to make it work with OpenBSD.  All of
# MCP's functionality already works on OpenBSD using the same methods as Linux,
# if the correct prerequisites are installed; MCP only needs to detect it
# correctly.

use strict;
use warnings;

chdir('mcp');
chdir('runtime');
my $file = 'commands.py';

my @lines = ();
my $fh;
open($fh, '<', $file) or die($!);
while(<$fh>)
	{
	s#(sys\.platform\.startswith\('linux'\))#($1 or sys.platform.startswith('openbsd'))#;
	push @lines, $_;
	}
close($fh);

unlink($file) or die($!);
open($fh, '>', $file) or die($!);
map { print $fh $_; } @lines;
close($fh);
