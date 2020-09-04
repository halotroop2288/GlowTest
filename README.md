# Untitled Adventure/Factory Voxel Game

AKA ***[Litecraft](https://github.com/halotroop/LiteCraft): Take 3***

AKA ***Codename: BrightCraft***

A fork of [@falkreon's](https://github.com/falkreon) [GlowTest](https://github.com/falkreon/GlowTest).
(Thanks, man! I hate writing the basics!)

## Goals
 - Work **alone** on a project for once in my life
   - Seriously. Working with others is really stressful.
   - I hear that unassisted self-teaching is a really great way to learn.
 - Make a voxel sandbox game, inspired by the most fun ways to play Minecraft
   - Gameplay focussed around adventure OR automation
     - Adventure mode
       - Would force players to keep moving around by keeping players on their toes at all times
       - Difficulty would ramp up in areas where players spend more of their time,
       spawning enemies, and allowing environmental disasters after long periods
       - Updates would add difficulty as players find workarounds to challenging game mechanics
     - Factory mode
       - Would encourage players to stay in one place,
       to create factories that automate every game mechanic.
       - Update would add new rewards for automation,
       new ways to automate,
       and new mechanics to work with
       - Players may also use this mode to build neat structures, such as buildings to house 
     - Other than incentives, all features would be available in both modes
     - No cheater/creative mode! Force players to prototype their stuff in real-time!
    - Internal focus around modularity
       - Adding things to the game should be super simple. As little as one line of code.
 - Have fun
    - My life is going horribly. I hope this makes it feel better.
    - Might spice it up with some queer/furry stuff some time down the line.

## Decisions, decisions...
 - Rendering engine:
   - [x] Use someone else's open-source engine
     - Saves time (oh my god I have had enough of writing boilerplate LWJGL code!)
     - Potential to be a lot better than what I could write
   - [ ] Use my own open-source engine
     - Saves me from having to learn someone else's codebase
     - Prevents potential interaction with other developers
     - If N*tch can do it, any idiot could!
 - Data formats
   - World save (Binary)
     - Need a data format that can efficiently store binary data that represents a world.
       - [ ] [BSON](https://github.com/mongodb/mongo-java-driver) (Binary javaScript Object Notation)
         - Based on JSON
         - Recommended by [@Falkreon](https://github.com/falkreon):
         a wise coder, and the maintainer of [Glow](https://github.com/playsawdust/Glow)
       - [ ] [SOD](https://github.com/valoeghese/SOD-Java) (Segregated Ordinal Data)
         - Written by [@valoeghese](https://github.com/valoeghese), an acquaintance of mine.
         - Interesting format
         - Used in the second failed attempt of LiteCraft
       - [ ] [NBT](http://jnbt.sourceforge.net/) (Named Binary Tag)
         - Same format used by Minecraft, so proven to be good for this application
         - Library has a restrictive license ([BSD](http://jnbt.sourceforge.net/LICENSE.TXT))
         - Library written by [Graham Edgecombe](http://www.grahamedgecombe.com/),
         but format created by N*tch.
         - Source code for the library seems to be lost? Subversion repository link is broken on the website.
   - Config/Settings/Options (Plain text)
     - Need a readable data format that can store user-configurable data
       that represents settings in-game, and related to the game.
       - [ ] [JSON5](https://json5.org/) / [HJSON](https://hjson.github.io/)
         - A popular standard
         - Has forgiving syntax
         - Recommended by [@Falkreon](https://github.com/falkreon)
         - Can be parsed with [Jankson](https://github.com/falkreon/Jankson),
         which is already used by the project
       - [ ] [Zoesteria Config](https://github.com/valoeghese/ZoestriaConfig)
         - Another standard written by [@valoeghese](https://github.com/valoeghese)
         - A standard I've personally wanted to try using for my projects, but never learned.
       - [ ] XML
         - Extremely popular standard
         - Not often used for configs (at least I don't think so?)
         - Overly-verbose syntax
       - [ ] Multiple standards
         - It's possible to implement multiple standards for configs, if need be, or is wanted.
         - Increases amount of options for modders / outside collaborators who might like to use other standards. 
         - Time consuming to implement
         - Increases project size and complexity
         - Not as straightforward to implement or use
 - Sound implementation
   - How I see it, sounds could be implemented in one of two ways:
       - [ ] **Traditional:** Loading sounds from sound files
         - [ ] Implement an OGG and/or WAV loader, probably using OpenAL
           - Time consuming with little creative payoff
           - Done to death, every game since the original PlayStation has done this.
           - Has a [tutorial](https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter22/chapter22.html)
           - I would probably have to learn how to deal with 3D spatial sound myself
         - [ ] Use an existing 3D audio library, like [Paul's SoundSystem](https://github.com/kovertopz/Paulscode-SoundSystem)
           - Literally what Minecraft did for a long time.
           - Also done quite often
       - [ ] **Game Jam FTW:** Dynamic Sounds via a Java version of [BFXR](https://www.bfxr.net/)
         1) Create a Java version of the [JavaScript application](http://github.grumdrig.com/jsfxr/) [JSFXR](https://github.com/mneubrand/jsfxr),
            which is itself a JavaScript version of a [Flash application](https://www.bfxr.net/), [BFXR](https://github.com/increpare/bfxr),
            which was created by a [Ludum Dare](https://ldjam.com/) participant
            to reduce the time needed to create audio for indie games.
         2) Allow this Java version to read data from [the decided plain text data format (JSON?)] files
            and play sounds generated by the program with that data.
         3) Create some sort of sequencer based on [the decided plain text data format (JSON?)] to create
         music from it.
         - This would also give game jam participants access to these new tools, which would be cool!.

 # License
 This project is licensed under Mozilla Public License version 2,
 to match the license of my chosen render engine, [Glow](https://github.com/playsawdust/Glow).
 
 For more information, see [LICENSE](https://github.com/halotroop2288/GlowTest/blob/trunk/LICENSE).
 
 I want to use licensed music from indie artists. That will probably have its own licensing notice.