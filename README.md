# BELLA

Building from Source
--------------------

To build BELLA from source you'll need the following to be installed:
- OpenJDK version 17 or greater
- A recent version of OpenJFX
- the Apache Ant build system

Once these are installed and in your execution path, issue the following
command from the root directory of this repository:

```sh
JAVA_FX_HOME=/path/to/openjfx/ ant
```
The package archive will be left in the `dist/` subdirectory.

Note that unless you already have a local copy of the latest
[BEAST 2 source](https://github.com/CompEvol/beast2)
in the directory `../beast2` and the latest
[BeastFX source](https://github.com/CompEvol/beastfx)
in the directory `../beastfx` relative to the BELLA root, the build
script will attempt to download them automatically.  Thus, most builds
will require a network connection.


BEAST2 package dependencies
--------------------

BELLA strictly depends on the following BEAST2 packages:
  - [BDMM-Prime](https://github.com/tgvaughan/BDMM-Prime)

For most functions and to run our example files, we require additional packages:
  - [feast](https://github.com/tgvaughan/BDMM-Prime/feast)
