# Changelog

All notable changes to this fork are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

This project is a fork of [maruTA-bis5/mattermost4j](https://github.com/maruTA-bis5/mattermost4j), which has not been maintained since January 2023.
For the history up to 0.25.0, see the [upstream repository](https://github.com/maruTA-bis5/mattermost4j/releases).

## [0.25.1] - 2026-10-08

The first release of this fork, based on the upstream [v0.25.0](https://github.com/maruTA-bis5/mattermost4j/tree/v0.25.0) release.

### Fixed

- Support Jackson 2.20 and later.
  `MattermostPropertyNamingStrategy` now uses the `PropertyNamingStrategies.SNAKE_CASE` and `PropertyNamingStrategies.UPPER_CAMEL_CASE` constants instead of the `PropertyNamingStrategy` constants that Jackson 2.20 removed.
  This fixes the `java.lang.NoSuchFieldError` thrown by 0.25.0 on Jackson 2.20 or later ([upstream issue #540](https://github.com/maruTA-bis5/mattermost4j/issues/540), backport proposed in [upstream pull request #541](https://github.com/maruTA-bis5/mattermost4j/pull/541)).

### Changed

- The group ID is now `io.github.ug23` instead of `net.bis5.mattermost4j`.
  The artifact IDs, the Java package names and the module names are unchanged.
- Jackson versions are managed through `com.fasterxml.jackson:jackson-bom`, so every Jackson artifact is aligned to one version.
  The version is exposed as the `jackson.version` property (default: 2.13.3) and can be overridden, for example with `-Djackson.version=2.21.4`.
- Releases are published to Maven Central through the Sonatype Central Portal instead of the retired OSSRH.
  A release is triggered by pushing a `v*` tag.

[0.25.1]: https://github.com/ug23/mattermost4j/compare/v0.25.0...v0.25.1
