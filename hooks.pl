#!/usr/bin/perl -w
# ==============================================================================
# Copyright (C)2012 by Aaron Suen <warr1024@gmail.com>
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
# This script installs custom hooks into the decompiled MCP source code for
# functionality needed by BWR.  Note that the lines off of which these hooks
# hang are identified by MD5, so exactly zero lines of upstream Mojang/FC code
# are included anywhere in BWR.

use strict;
use warnings;
use autodie;
use Digest::MD5 qw(md5_hex);

# Subroutine to patch a file, including scanning through it line-by-line,
# identifying lines and inserting hooks after them, then moving the new
# output file over top of the old one.  Hooks are tagged so they can be
# removed on subsequent re-runs of the script, so the whole process is
# idempotent.
sub dopatch
	{
	# First arg is the file, subsequent are key/value pairs of
	# pre-line MD5 : hook to add.
	my $file = shift();
	my %hooks = (@_);

	# Open the original file and spool output to a new file.
	my ($ifh, $ofh);
	open($ifh, '<', $file);
	open($ofh, '>', $file . '.new');
	while(<$ifh>)
		{
		# Strip off any lines that end in the special
		# patch tag, thus removing any previous versions
		# of the patch.
		m#//\s*BWR-PATCH\s*$# and next;

		# Output all original source lines.
		print $ofh $_;

		# Remove trailing newlines, and separate out
		# any leading whitespace.
		chomp;
		my $pref = '';
		s#^(\s+)## and $pref = $1;
		s#\s+$##;

		# Lookup the hook by the line's MD5 (excluding whitespace).
		# If a hook is found to hang after this line, then add it
		# with the same leading whitespace (no need to make it look
		# sloppy), and add the special patch tag so subsequent runs
		# can strip this hook back out.
		my $key = md5_hex($_);
		my $val = $hooks{$key};
		if($val)
			{
			print $ofh $pref . $val . " // BWR-PATCH\n";
			delete $hooks{$key};
			}
		}

	# Make sure that all expected hooks were installed.  If any is
	# missing, kick up a fit.  This could happen if upstream code is
	# updated, and we want to catch this during the build.
	scalar(keys %hooks) and die('missing hooks: ' . join(' ', values(%hooks)));

	# Move the new patched file into place over the original.
	rename($file . '.new', $file);
	}

# Path to the main decompiled source.
my $srcpath = 'mcp/src/minecraft_server/net/minecraft/src/';

# Add all hooks to existing MC/BTW classes.
dopatch($srcpath . 'World.java',
	'43a2251845cc22fe9c9f213b6f9dad98',
		'mod_BetterWithRenewables.m_instance.load();',
	'ac21e31c83bddcfd956fb477b92376ee',
		'   par1Entity = mod_BetterWithRenewables.m_instance.TransformEntityOnSpawn(par1Entity);'
	);
dopatch($srcpath . 'ServerConfigurationManager.java',
	'4e716b9b8fdd574849aa84e8d51335f4',
		'mod_BetterWithRenewables.m_instance.ServerPlayerConnectionInitialized(var6, par2EntityPlayerMP);'
	);
