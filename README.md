
# Trunkfriends

## IMPORTANT INFORMATION

Trunk Friends is NOT something close to be considered ready. I am making this
public now, so people can use it if they want. You need to build the project
locally before running it,

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
This way, if you see that you have unfollowed someone you definitly did not
mean to unfollow, you can check if they were unfollowed as a result of 
server defederation. They may of course have blocked you, if so, please
leave them alone, but if server defederation got between you and a friend,
it is nice to know

## Is TrunkFriends limited to Mastodon? 

For now, Trunk Friends is only tested against Mastodon servers and only have an API
client for Mastodon. However, the software is written to be flexible, and it should
be pretty straight forward to implement support for instance for Firefish. This may
come soon.


