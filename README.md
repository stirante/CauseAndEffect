# CauseAndEffect

This is a simple plugin, that allows to find out the cause behind a player's damage.

### Example

Scenario:
* Player A flicks the lever
* Lever powers redstone
* Redstone powers dispenser
* Dispenser pours lava
* Lava lights up a tree
* Tree lights up other trees causing a forest fire
* Forest triggers TNT
* TNT damages Player B

This plugin will detect, that Player A damaged Player B with list of all things, that happened in between.

## Prerequisites

This plugin requires at least Java 8. In case of Java 9 and newer, you need to allow illegal reflective access.

## Installing

Just place plugin jar inside plugins folder.

## Disclaimer

This plugin is still in development and may change heavily. Right now it only displays in console the cause, but in the future it will have an easy to use interface.