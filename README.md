EnerJ
=====

The type checker and simulation framework used in the evaluation of EnerJ are
available for download. The system is very much a work in progress and is
available under the terms of [Matt Might's CRAPL][crapl].

I unfortunately can't promise support with using this code. But if you have a
bug report or other inquiry, [send me email][email].

[crapl]: http://matt.might.net/articles/crapl/
[email]: mailto:asampson@cs.washington.edu


Compiling EnerJ
---------------

To use the system, first download and install the [Checker Framework][], which adds JSR 308 type annotations to Java. Be sure to get [version 1.3.1][]. Set the JSR308 environment variable to point to the directory containing the CF's checkers directory; the EnerJ build files will look for this variable.

Then get the [EnerJ source][] (this package) and the [CF runtime library][]. You can also clone the source as Mercurial repositories [from BitBucket][bb]. Look for the "enerj" and "checker-runtime" repositories on my page.

[bb]: https://bitbucket.org/adrian
[CF runtime library]: https://bitbucket.org/adrian/checker-runtime/get/tip.tar.bz2
[EnerJ source]: https://bitbucket.org/adrian/enerj/get/tip.tar.bz2
[version 1.3.1]: http://types.cs.washington.edu/checker-framework/releases/1.3.1/checkers.zip
[Checker Framework]: http://types.cs.washington.edu/checker-framework/

Expand both packages into a common directory. Make sure the directories are named "enerj" and "checker-runtime" and are siblings in the directory tree. First, build the checker-runtime library against the Checker Framework by typing `ant` in its directory. Then build EnerJ the same way.

If you have trouble with the build—for example, if ant complains about a missing tools.jar or compiler-related classes, be sure to set your `$JAVA_HOME` environment variable to point at the appropriate place. (Exactly where depends on your OS and JDK installation.)


Using the Compiler
------------------

The `enerjc` and `enerj` tools (in `bin`) can be used to compile and run EnerJ programs in place of `javac` and `java`.

Pass the flag `-Alint=simulation` to `enerjc` to compile source files with the simulation source-to-source translation enabled. Then pass `-noisy` to `enerj` to enable error injection in a simulated approximate program.


Known Bugs
----------

The type checker has no known bugs, but the simulator translation does. (It's based on a pile of hacks on top of the internal, undocumented javac API, so some things don't work as they should.)

* Inner classes that reference final variables in the scope of their containing method don't work. To use EnerJ, you'll need to turn these classes into outer classes and pass state explicitly.
* Name conflicts between classes and packages can cause problems. This is because the simulation translation uses fully-qualified names to refer to classes but this can break if a package name is shadowed by a class name. To avoid this, follow the Java convention of naming your classes with CamelCase and your packages in lower case.
