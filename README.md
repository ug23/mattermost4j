mattermost4j
============

[![CI Status](https://github.com/ug23/mattermost4j/actions/workflows/ci.yml/badge.svg)](https://github.com/ug23/mattermost4j/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ug23/mattermost4j-core)](https://central.sonatype.com/artifact/io.github.ug23/mattermost4j-core)
[![Javadocs](https://javadoc.io/badge2/io.github.ug23/mattermost4j-core/javadoc.svg)](https://javadoc.io/doc/io.github.ug23/mattermost4j-core)

Mattermost API v4 client for Java.

## About this fork

This repository is a fork of [maruTA-bis5/mattermost4j](https://github.com/maruTA-bis5/mattermost4j), published to Maven Central under the `io.github.ug23` group ID.

**Why this fork exists.**
The upstream project has not been maintained since January 2023.
Its last 0.x release, 0.25.0, fails with `java.lang.NoSuchFieldError` when it runs with Jackson 2.20 or later, because it refers to `PropertyNamingStrategy` constants that Jackson 2.20 removed.
Applications hit this as soon as their dependency management upgrades Jackson, for example with Spring Boot 3.5.13 or later.

**Policy.**

- This fork is maintained on an ongoing basis, starting from the upstream 0.25.0 release.
- Issues and pull requests are welcome in [ug23/mattermost4j](https://github.com/ug23/mattermost4j).
- The fix is also proposed to upstream as a backport pull request ([maruTA-bis5/mattermost4j#541](https://github.com/maruTA-bis5/mattermost4j/pull/541)).

**New coordinates.**
Replace the `net.bis5.mattermost4j` group ID with `io.github.ug23`.
The artifact IDs and the Java package names (`net.bis5.mattermost.*`) are unchanged.

Apache Maven:

```xml
<dependency>
	<groupId>io.github.ug23</groupId>
	<artifactId>mattermost4j-core</artifactId>
	<version>0.25.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.ug23:mattermost4j-core:0.25.1'
```

See [CHANGELOG.md](CHANGELOG.md) for the changes since the upstream 0.25.0 release.

## Requirement
- JDK 8 or 11
	- for JDK 11 users: You may add these dependencies for runtime
		- Jakarta XML Binding
		- Jakarta Activation
- Mattermost Server
    - Please check mattermost4j version compatible with your server instance:
    https://github.com/maruTA-bis5/mattermost4j/wiki#what-version-shoud-i-use
    
## Usage
### Basic API Client
```java
// Create client instance
MattermostClient client;
// case 1. use constructor - log disable and prohibit unknown properties
client = new MattermostClient("YOUR-MATTERMOST-URL");
// case 2. use builder
client = MattermostClient.builder()
    .url("YOUR-MATTERMOST-URL")
	.logLevel(Level.INFO)
	.ignoreUnknownProperties()
	.build();

// Login by id + password
client.login(loginId, password);
// Login by Personal Access Token
client.setAccessToken(token);
```

### Use Incoming Webhook
```
// You can also use builder for create client instance.
IncomingWebhookClient client = new IncomingWebhookClient("YOUR-MATTERMOST-URL");

IncomingWebhookRequest payload = new IncomingWebhookRequest();
payload.setText("Hello World!");
payload.setUsername("Override Username");

client.postByIncomingWebhook(payload);
```

## Install
### Apache Maven:
```xml
<dependency>
	<groupId>io.github.ug23</groupId>
	<artifactId>mattermost4j-core</artifactId>
	<version>0.25.1</version>
</dependency>
```

### Gradle:
```
implementation 'io.github.ug23:mattermost4j-core:0.25.1'
```

## Contribution
1. Fork it ( https://github.com/ug23/mattermost4j/fork )
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create new Pull Request

### Code Formatter
use https://github.com/google/styleguide/ {intellij,eclipse}-java-google-style.xml .

### CheckStyle
Currently, use CheckStyle's built-in `google_checks.xml`.

## Test
### Unit test
- `mvn test`

### Integration with Mattermost Server
1. `docker-compose up`
2. `mvn verify`

See [docs/RELEASING.md](docs/RELEASING.md) for the exact commands, including the workaround for Apple Silicon.

## Contact
- Create GitHub Issue (https://github.com/ug23/mattermost4j/issues/new)

## License
[Apache Software License, Version 2.0](LICENSE.txt)

