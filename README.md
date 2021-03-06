# SUMMARY

Better With Renewables is an add-on for the Better Than Wolves mod for
Minecraft that makes more resources renewable, obtainable, and/or
available to extend endgame play, especially on custom "challenge" maps
that are resource-scarce.

Project Website: https://gitlab.com/btwbwr
BTW Forums: http://sargunster.com/btwforum/viewtopic.php?f=12&t=6009

For installation instructions, see the included INSTALL file.
For license terms, see the included LICENSE file.
For a list of changes and history, see the CHANGELOG file.

# CREDITS

Core Design Team:
	Benanov
	DaveYanakov
	Warr1024

Lead Developer:
	Warr1024

Lead Tester:
	Benanov

Other Testers and Contributors:
	destin_eternal
	ExpHP
	NoirTheBlackCat
	Pfilson
	Uristqwerty
	TaterBoy

# DESIGN GOALS

- Make all existing resources renewable, given a reasonable supply
  of starting resources.

- Make most resources that are likely not to be included in custom
  "challenge" maps obtainable using other resources that are likely
  to be included.

- Seamless integration with Better Than Wolves, retaining spirit and
  challenge of Better Than Wolves game design.  New alternative sources
  for resources will be less desirable than existing BTW methods
  when BTW methods are available.

- Do not break existing BTW or Vanilla builds or maps, do not modify
  world generation.


- Remain backwards-compatible with BTW, and forwards-compatible
  with future expansion of BTW.  Removing BWR will cause only BWR-
  specific builds to break, but the world will still load and run with
  standard BTW functionality.

- Coding standards: http://geosoft.no/development/javastyle.html

# FEATURES

Renewable Dirt:
	There is a block introduced by BTW that is dirt-like and
	nutrient-rich, but unsuitable for planting due to a low pH;
	rinse the acid out to leave dirt behind.  Added heat speeds up
	the process.

Renewable Clay:
	Sand rinsed in acid is etched into finer clay particles.
	Added heat speeds up the process.

Renewable Lily Pads:
	Lily pads will spread (very limited) to adjacent spaces that
	meet their living requirements.  They can now grow and survive
	on deep enough flowing water, so they can be farmed using pumps
	with Hardcore Buckets, when water sources are scarce.

Renewable Lapis Lazuli:
	Lapis particles can be washed from the products of the genetic
	mutants that create it. A fine mineral powder will bond to the
	lapis and make it settle out of suspension.

Renewable Soul Sand:
	Fresh souls, harvested without human violence, can be forced
	under high pressure into a nutrient-rich block by machinery made
	of sufficiently hard materials.  The reaction is highly
	exothermic.

Renewable Netherrack:
	The essence of hellfire is distilled from the nether by
	organisms that have evolved in that most hostile of
	environments.  Combined with the requisite Souls of the Damned,
	these can be infused into an appropriate medium.

Renewable Lava Sources:
	A block of a certain very hot solid can be melted into lava from
	the added heat of nearby lava.

Renewable Dead Shrubs:
	Render the leaves off a living plant of similar shape.

Renewable Glowstone:
	Redstone dust has a chance of absorbing concentrated sunlight
	when experiencing a change in potential.  Gasses released
	during the reaction cause the redstone to "boil," which can
	trigger nearby buddy blocks erratically.

Renewable Cobwebs:
	Stew spider silk with something suitably sticky.  Don't try to
	use too much silk at one time, or you'll end up with a matted
	mess.

Renewable Diamond:
	Melt the tears of the damned along with an appropriately-
	colored dopant, at high temperatures.  A small amount of
	hellfire dust is needed as a catalyst, but it needs to be mixed
	with a stabilizing agent, which is consumed in the reaction.
	Failure to monitor or automate this reaction can be dangerous.

Recyclable Diamonds:
	Diamond equipment can be broken down into raw diamonds (for uses
	in lenses and other tools) by dissolving the agents that bond
	them in a very strong alkaline.

Alternative Nether Portal Recipe:
	A portal frame made of emerald can be tainted into a portal to
	the underworld by a dark ritual, requiring the reading of an
	arcane incantation over the sacrifice of a holy man.

Domesticated Blazes:
	By placing a skull atop a heart of gold, surrounded by vertical
	bars, an artificial blaze can be created.  These blazes do not
	de-spawn, though they are not exactly tame either.  When fed an
	appropriate fuel, blazes can reproduce asexually, giving birth
	to another blaze in nearby fire.  Blazes will only reproduce
	in the Nether.

Plant Crossbreeding:
	A highly fertile space, left unplanted, will encourage the
	growth of mutations from adjacent species.  Nearly every kind of
	growing plant/fungus is obtainable.  Experiment with different
	parent species for different results.

Animal Crossbreeding:
	A pair of animals of different species, under sufficiently
	desperate circumstances, may breed and produce offspring.
	Certain desirable mutations can be achieved by modifying the
	environment to be a more suitable habitat for that species.
	Some mutations, however, will be dangerous abominations.

Forced Animal Spawning:
	A couple of species animals can now spawn, very rarely, on tall
	grass, in full sunlight, if food is left out for them, and there
	are no other animals nearby.  Combined with animal cross-
	breeding, this makes desert-only custom map playable.

Wolf Breeding Automation:
	When a certain organic chemical is fed to wolves with their
	food, it will act as an aphrodisiac, making the wolves willing
	to mate in awkward positions while under its influence.  The
	chemical fed to wolves is volatile, as will the wolves also be
	while under its influence.
	
Dung Block Automation:
	Loose dung can now be stewed automatically into blocks.

Low-Efficiency Recipes:
	Alternative recipes and techniques for producing resources with
	existing recipes using the millstone, earlier in the tech tree
	than normally possible, but at a steeper price.
		- Gold Recycling
		- Hellfire Dust
		- Redstone

Hardercore Villagers:
	Goods that may be sold by villagers have been further
	restricted.  This reduces villager trades bypassing some of the
	more interesting challenges in the tech tree.

Automatic Update Check:
	The Better With Renewables server will periodically check the
	project website for new releases, and notify players if a new
	version is available.
