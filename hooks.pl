#!/usr/bin/perl -w
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
use strict;
use warnings;
use Digest::MD5 qw(md5_hex);

do {
	my $fpath;
	my @lines;

	sub srcsave {
		$fpath or die("no file loaded");
		warn("srcsave: $fpath\n");
		open(my $fh, ">", "$fpath.new") or die("$fpath.new: $!");
		print $fh map { "$_$/" } @lines;
		close($fh);
		-s "$fpath.orig" or link($fpath, "$fpath.orig");
		rename("$fpath.new", $fpath);
	}

	sub srcsaveauto {
		$fpath and srcsave();
	}

	sub srcload {
		srcsaveauto();
		($fpath) = @_;
		$fpath = "tmp/mcp/src/minecraft_server/net/minecraft/src/$fpath";
		warn("srcload: $fpath\n");
		my $actual = -s "$fpath.orig" ? "$fpath.orig" : $fpath;
		open(my $fh, "<", $actual) or die("$actual: $!");
		@lines = <$fh>;
		close($fh);
		chomp(@lines);
	}

	sub srcfind {
		my($md) = @_;
		$md = lc($md);
		warn("srcfind: $md\n");
		for(my $i = 0; $i < scalar(@lines); $i++) {
			my $l = "$lines[$i]";
			$l =~ s#^\s+##;
			$l =~ s#\s+$##;
			lc(md5_hex($l)) eq $md or next;
			warn("found at $i\n");
			return $i;
		}
		die("$fpath: md5 $md not found");
	}

	sub srcread {
		my($pos) = @_;
		warn("srcread at $pos: $lines[$pos]\n");
		return $lines[$pos];
	}
	
	sub srcwrite {
		my($pos, $len, @list) = @_;
		warn("srcwrite at $pos replace $len:\n");
		map { warn("  $_\n") } @list;
		return splice(@lines, $pos, $len, @list);
	}

	sub srcindent {
		my($pos) = @_;
		$lines[$pos] =~ m#^(\s+)# and return $1;
		return "";
	}
};

sub runhook {
	my($f) = @_;
	warn("run script: $f\n");
	open(my $fh, "<", $f) or die("$f: $!");
	my $s = do { local $/; <$fh> };
	close($fh);
	eval {
		no strict;
		my $f = $f;
		eval "$s";
	};
	$@ and die("$f: $@");
	srcsaveauto();
}

map { runhook($_) } @ARGV;
