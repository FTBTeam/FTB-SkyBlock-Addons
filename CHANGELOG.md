# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.4]

### Changed

* AutoHammer can not be disabled with a redstone signal 

### Fixed

* Poor performance with the hammer overlay due to it rendering on every level and not the selected level
* Texture on the hammer overlay having a random black box on the output slot

## [1.2.3]

### Fixed
* Fixed Fusing Machine & Super Cooler always restarting their recipes on an item change
  * Even piping in items caused a restart, which is annoying
* Increased Fusing Machine & Super Cooler energy buffers from 100K FE to 1M FE

## [1.2.2]

### Fixed
* Some logic fixes to Fusing Machine & Super Cooler recipe handling
* Recipes are now checked in order of most to fewest input ingredients
  * Allows for similar recipes, where the recipe with most ingredients takes priority

## [1.2.1]

### Fixed
* Fixed recipe sync for Fusing Machine and Super Cooler on dedicated server

## [1.2.0]

### Added
* Added new Fusing Machine and Super Cooler machines

## [1.1.0]

### Changed
* KubeJS 6.1 support
  * This release is *not* compatible with KubeJS 6.0

## [1.0.0]

### Changed
* Released the mod
