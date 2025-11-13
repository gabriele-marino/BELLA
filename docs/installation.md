# Installation

## BEAST2 package dependencies

BELLA strictly depends on the following BEAST2 packages (install them before installing BELLA):
* [BDMM-Prime](https://github.com/tgvaughan/BDMM-Prime)

For most functions and to run our example files, we require additional packages:
* [feast](https://github.com/tgvaughan/BDMM-Prime/feast)

## Via BEAUti

BELLA requires a working installation of BEAST 2.7 which can be obtained from https://www.beast2.org/. 
The package itself can then be installed via the built-in package manager in the following way:

1. Open BEAUti.
2. From the `File` menu select `Manage Packages`.
3. Click the `Package repositories` button at the bottom of the dialog box.
4. Click `Add URL` and enter the following repository URL:
   [tps://github.com/gabriele-marino/BELLA/package.xml)](tps://github.com/gabriele-marino/BELLA/package.xml).

## From Source Code

You may also install BELLA manually. Follow instructions at [https://www.beast2.org/managing-packages](https://www.beast2.org/managing-packages/#:~:text=If%20for%20some%20reason%20you,zip%20inside%20the%20VSS%20directory) and use the `.zip` file from your preferred [release](https://github.com/gabriele-marino/BELLA/releases). 

## Building from Source Code

To build BELLA from source you'll need the following to be installed:

* OpenJDK version 17 or greater
* A recent version of OpenJFX
* the Apache Ant build system

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
