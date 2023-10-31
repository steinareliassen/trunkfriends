## TL;DR

- Install Java, version 17 or newer.

- clone repository

- build and run application with "gradlew run"

## Running Trunkfriends from source

Since you can buil and run from source with virtually no
requirement to preinstalled software, running from source
is very easy, even if you are not a developer or have much
technical knowledge.

You put the source code in a folder, and execute a simple command
to start trunkfriends.

### First, install Java, version 17 or higher

- Visit https://www.oracle.com/java/technologies/downloads/#java17
- You can alternatively install OpenJDK via brew or apt-get

### Installing Trunkfriends using git

If you already have git installed and are comfortable using this for
simple things like cloning and pulling, this is one of the best 
options. Upgrading to a newer version of Trunkfriends is then nothing
else than a simple

git pull

Simply clone the following repos:

https://github.com/steinareliassen/trunkfriends.git

On the location where you want the folder containing trunkfriends to be.

### Installing and running Trunkfriends from zip file

If you do not have git, downloading the source as a zip file is
a goo option.

https://github.com/steinareliassen/trunkfriends/archive/refs/heads/main.zip

Unzip the folder on the location you want Trunkfriends to be.

When a new version of Trunkfriends comes out, you simply delete
the folder, download the zip file again, and unpack it. Trunkfriends
stores data in a separate location, so this will not be removed.

## Running Trunkfriends 

Simply go to the Trunkfriends folder you obtained from the git repository,
and run 

On OSX / Unix / Linux:

_sh gradlew run_

On Windows : 

_gradlew.bat run_

Please note that the first time you do this, gradle (the build system used to
build this project) will need to download the dependencies required to run the 
project, and this may take a while. The next time, these dependencies are 
cached, and starting will take no time.