#!/usr/bin/perl -w
use strict;
use warnings;
use autodie;
use Digest::MD5 qw(md5_hex);

my $file = 'runtime/commands.py';

my ($ifh, $ofh);
open($ifh, '<', $file);
open($ofh, '>', $file . '.new');
while(<$ifh>)
	{
	s#(sys\.platform\.startswith\('linux'\))#($1 or sys.platform.startswith('openbsd'))#;
	print $ofh $_;
	}
rename($file . '.new', $file);
