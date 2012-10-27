#!/usr/bin/perl -w
use strict;
use warnings;
use autodie;
use Digest::MD5 qw(md5_hex);

sub dopatch
	{
	my $file = shift();
	my %hooks = (@_);

	my ($ifh, $ofh);
	open($ifh, '<', $file);
	open($ofh, '>', $file . '.new');
	while(<$ifh>)
		{
		m#//\s*BWR-PATCH\s*$# and next;
		print $ofh $_;
		chomp;

		my $pref = '';
		s#^(\s+)## and $pref = $1;
		s#\s+$##;

		my $key = md5_hex($_);
		my $val = $hooks{$key};
		if($val)
			{
			print $ofh $pref . $val . " // BWR-PATCH\n";
			delete $hooks{$key};
			}
		}

	scalar(keys %hooks) and die('missing hooks: ' . join(' ', values(%hooks)));
	rename($file . '.new', $file);
	}

my $srcpath = 'mcp/src/minecraft_server/net/minecraft/src/';
dopatch($srcpath . 'World.java',
	'43a2251845cc22fe9c9f213b6f9dad98', 'mod_BetterWithRenewables.m_instance.load();',
	'ac21e31c83bddcfd956fb477b92376ee', '   par1Entity = mod_BetterWithRenewables.m_instance.TransformEntityOnSpawn(par1Entity);'
	);
dopatch($srcpath . 'ServerConfigurationManager.java',
	'4e716b9b8fdd574849aa84e8d51335f4', 'mod_BetterWithRenewables.m_instance.ServerPlayerConnectionInitialized(var6, par2EntityPlayerMP);'
	);
