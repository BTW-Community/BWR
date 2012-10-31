#!/usr/bin/perl -w
use strict;
use warnings;
use autodie;
use Digest::MD5 qw(md5_hex);

my $binpath = 'mcp/bin/minecraft_server/net/minecraft/src';
my $srcpath = 'src';

my %classes = ( );
my $dh;

opendir($dh, $srcpath);
while(my $e = readdir($dh))
	{
	$e =~ m#(.*)\.java$# and $classes{$1} = 1;
	}
closedir($dh);

opendir($dh, $binpath);
while(my $e = readdir($dh))
	{
	$e =~ m#(.*)\.class$# or next;
	$classes{$1} and delete $classes{$1};
	}
closedir($dh);

scalar(keys %classes) and die('Missing classes: ' . join(', ', sort(keys %classes)));
