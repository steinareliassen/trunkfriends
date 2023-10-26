
# Trunkfriends

## Upgrading from previous version of TrunkFriends

Keep the folder from the previous installation, and install
and run the new version. Create a connection to the server using
"add server". Trunkfriends will create a folder called 
".trunkfriends" in your home directory, containing subfolder
for the servername and within this, a subfolder for username.

Quit trunkfriends.

Copy "datafile.dmp" from the previous installation folder to 
this folder. Start trunkfriends again. Select the server, and 
the previously imported data should be there.

## Important Information

Trunk Friends is NOT something close to be considered ready. I am making this
public now, so people can use it if they want. You need to build the project
locally before running it, binary builds will come when more of the configuration
is completed.


This is public domain, comes with no warranty, use as is. The code is
currently a complete mess, and suffers greatly from the fact that I have
not made any UI in decades and have no experience with Jetpack Compose
prior to making this. 

You need to know how to fetch bearer token from browser to use this. If you do 
not know how to do that, please revisit this page in a few days, and instrutions
will be added.

## What is TrunkFriends

I started writing TrunkFriends as a result of suddenly being disconnected 
from a friend when the server I was on,
defederated the server they were on.

I decided I needed a little tool that could make me aware of similar things
happening in the future, so I would not loose touch with friends just because
of defederation. Of course, our servers were still not communicating, but
I could find other ways to reach them. The important part was to be aware

Given that I had a few other issues with some shortcomings of the Mastodon UI
in regards to how the friends UI is working, I thought I could combine the ideas
into one app that may be helpful.

## What does TrunkFriends do?

TrunkFiends is a "friends management interface".

It lets you keep a history of your connection to people you follow and who
follow you. By scanning your followers / following lists, and comparing it to
how it was at previous scan, you can keep track of who friended you at what
point in time, when they unfollowed you, when you unfollowed them, etc.

### Tracking followers / following

This way, if you see that you have unfollowed someone you did not
mean to unfollow, you can check if they were unfollowed as a result of 
server defederation. They may of course have blocked you, if so, please
leave them alone, but if server defederation got between you and a friend,
it is nice to know

## Is TrunkFriends limited to Mastodon? 

For now, Trunk Friends is only tested against Mastodon servers and only have an API
client for Mastodon. However, the software is written to be flexible, and it should
be pretty straight forward to implement support for instance for Firefish. This may
come soon.

## Building Trunk Friends

Clone the repository or download the code from github.

Make sure you have Java SDK installed, version 17 or higher.

Open a terminal / command prompt, and run "gradlew build". This will build Trunk Friends
on windows: gradlew.bat build
on *nix systems: ./gradlew build

Create a copy of the file "config.json.example" called "config.json"

Edit the config.json file, enter the host name of your mastodon instance 
(replace mastodon.social, unless you use mastodon.social), and insert your 
bearer token. You need to fetch bearer token from a logged-in session with your
mastodon server. More info on how to do that will follow.

Start trunk friends by typing:
on windows: gradlew.bat run
on *nix systems: ./gradlew run

## Using Trunk Friends

First time, the view will be rather empty. Press "Refresh against server" to start 
importing your followers / following accounts. A status window will show the progress,
and switch back to the list view. You can here see anyone you follow, and anyone following
you. Finger pointing at you means status if they follow you, finger pointing right
means that you follow them, green means they follow / you follow, red means you don't follow /
they don't follow you.

The result of this is stored in a file called "datafile.dmp".

Next time you press "refresh against serer", Trunk Friends will compare the result against
previous run, and add any changes to the end of the list.

If anyone has unfollowed you, it will be shown where the finger points at you, and the
status from green -> red.

The date on the card will not illustrate when the action happened, but when Trunk Friends
detected that it happened.

If you are curious as to what has happened with just one of the followers, press
the spyglass on the side of the card. Trunk Friends will then display ONLY history lines
for this follower. To get out, press the spyglass again, and all likes will be shown.

A backup of the datafile from the previous run will be created, called datafile.dmp.<date>

In case anything happened that damaged your datafile, you can revert to the previous backup
by deleting datafile.dmp and renaming the backup file to datafile.dmp

Yes, this is alpha software, so it is a bit hacky for now.
