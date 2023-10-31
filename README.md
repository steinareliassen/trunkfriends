
# Trunkfriends

## Important Information

This is public domain, comes with no warranty, use as is. The code is
currently a complete mess, and suffers greatly from the fact that I have
not made any UI in decades and have no experience with Jetpack Compose
prior to making this. 

Trunkfriends stores its data in a folder ".trunkfriends" on the users home
directory. As this software is very much in an alpha state and can potentially
contain bugs, it is recommended you backup this folder from time to time, 
in case anything  should happen.

## What is TrunkFriends

I started writing TrunkFriends as a result of suddenly being disconnected 
from a friend when the server I was on, defederated the server they were on.

I decided I needed a little tool that could make me aware of similar things
happening in the future, so I would not loose touch with friends just because
of defederation. Of course, our servers were still not communicating, but
I could find other ways to reach them. The important part was to be aware

Given that I had a few other issues with some shortcomings of the Mastodon UI
in regard to how the friends UI is working, I thought I could combine the ideas
into one app that may be helpful.

## Installing Trunkfriends

[Installing from source](docs/installing_source.md)

Installation packages will be added shortly.

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

## Using Trunk Friends

First time, the view will be rather empty. Press "Refresh against server" to start 
importing your followers / following accounts. A status window will show the progress,
and switch back to the list view. You can here see anyone you follow, and anyone following
you. Finger pointing at you means status if they follow you, finger pointing right
means that you follow them, green means they follow / you follow, red means you don't follow /
they don't follow you.

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
