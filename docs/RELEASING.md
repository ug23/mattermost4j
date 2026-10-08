# Releasing

This document describes how to publish this fork to Maven Central under the `io.github.ug23` namespace.
Releases go through the [Sonatype Central Portal](https://central.sonatype.com/) with `central-publishing-maven-plugin`.
The release workflow (`.github/workflows/release.yml`) uploads a deployment and stops at the `VALIDATED` state.
A maintainer then publishes it manually from the Portal.

## One-time setup

### Central Portal account and namespace

1. Sign up at <https://central.sonatype.com/> with the **GitHub** login of the `ug23` account.
   The `io.github.ug23` namespace is verified automatically for accounts created through GitHub.
   Check that it is listed as verified under **Namespaces**.
2. Generate a user token from **View Account** > **Generate User Token**.
   The token consists of a username part and a password part.
   They are used as `CENTRAL_USERNAME` and `CENTRAL_TOKEN` below.

### GPG key

Central requires a detached `.asc` signature for every file.

1. Generate a key pair.
   Choose RSA 4096 or ed25519 and set an expiration date (for example 2 years).

   ```sh
   gpg --full-generate-key
   gpg --list-secret-keys --keyid-format long
   ```

   Use the long key ID (or the fingerprint) of the new key as `<KEYID>` below.
2. Publish the public key so that Central can verify the signatures.

   ```sh
   gpg --keyserver hkps://keyserver.ubuntu.com --send-keys <KEYID>
   gpg --export <KEYID> | curl -T - https://keys.openpgp.org
   ```

   keys.openpgp.org prints a link to verify the e-mail address of the key.
   Open it, otherwise the key is published without its user ID.
3. Export the private key in ASCII armor for the GitHub secret.

   ```sh
   gpg --armor --export-secret-keys <KEYID> > private-key.asc
   ```

   Delete `private-key.asc` after registering the secret.
   When the key is about to expire, extend it with `gpg --quick-set-expire` and send the public key to the key servers again.

### GitHub secrets

Register four repository secrets with the GitHub CLI.
`gh secret set` reads the value from standard input or from a prompt, so the values do not end up in the shell history.
**Never paste these values into a chat, an issue, a pull request or a commit.**

```sh
gh secret set CENTRAL_USERNAME --repo ug23/mattermost4j
gh secret set CENTRAL_TOKEN --repo ug23/mattermost4j
gh secret set GPG_PRIVATE_KEY --repo ug23/mattermost4j < private-key.asc
gh secret set GPG_PASSPHRASE --repo ug23/mattermost4j
```

| Secret | Value |
| --- | --- |
| `CENTRAL_USERNAME` | Username part of the Central Portal user token |
| `CENTRAL_TOKEN` | Password part of the Central Portal user token |
| `GPG_PRIVATE_KEY` | ASCII-armored private key (`private-key.asc`) |
| `GPG_PASSPHRASE` | Passphrase of the GPG key |

## Release procedure

The example below releases 0.25.1 from the `release/0.25.x` branch.

1. Update the version in all three POM files (`pom.xml`, `mattermost-models/pom.xml` and `mattermost4j-core/pom.xml`) and the `<scm><tag>` element of `pom.xml`.

   ```sh
   mvn versions:set -DnewVersion=0.25.1 -DgenerateBackupPoms=false
   ```

   `versions:set` does not update `<scm><tag>`.
   Change it to `v0.25.1` in `pom.xml` by hand.

2. Update `CHANGELOG.md`.
   Replace `Unreleased` in the `## [0.25.1]` heading with the release date (`YYYY-MM-DD`).
   The release workflow copies this section into the GitHub Release notes.
3. Commit and push to `release/0.25.x`.

   ```sh
   git push origin release/0.25.x
   ```

4. Wait until the CI workflow (`ci.yml`) is green for the pushed commit.
5. Optionally, run the release workflow as a dry run (see [Dry run on GitHub Actions](#dry-run-on-github-actions)).
6. Create and push the tag.
   The tag name must be `v` followed by the project version, otherwise the release workflow fails.

   ```sh
   git tag v0.25.1
   git push origin v0.25.1
   ```

7. The release workflow (`release.yml`) runs the unit tests, signs the artifacts, uploads the bundle to the Central Portal and waits until the deployment is `VALIDATED`.
   It then creates a GitHub Release for the tag with the `CHANGELOG.md` section as the notes.
8. Open <https://central.sonatype.com/publishing/deployments> and check the deployment.
   It must contain `mattermost4j-parent`, `mattermost-models` and `mattermost4j-core` under `io/github/ug23`, each with its signatures and checksums.
   - If the content is correct, click **Publish**.
     It usually takes some time until the artifacts appear on Maven Central.
   - If something is wrong, click **Drop**, fix the problem, delete the tag and the GitHub Release, and start again from step 1.
   - If several deployments of the same version remain on the Portal, publish only the latest one and **Drop** the older ones.
     A version that has been published to Maven Central cannot be replaced, so use a new version number after a successful publish.

## Dry run on GitHub Actions

The release workflow can be started manually from the **Actions** tab (**Release** > **Run workflow**).
When `dry_run` is checked (the default), the workflow builds the bundle without signing and stores `central-bundle.zip` as a workflow artifact.
It needs no secrets, does not upload anything and does not create a GitHub Release.
Use it to check the CI wiring and the bundle content before tagging.

If `dry_run` is unchecked, the workflow behaves in the same way as a tag push.
In that case, select the release tag in **Use workflow from**.
The workflow refuses to publish from a branch and fails with an error message.

## Local verification

### Inspect the bundle

`central-publishing-maven-plugin` 0.11.0 applies `-DskipPublishing=true` to each module.
As a result, nothing is staged and no bundle is created when it is set for the whole build.
To inspect the bundle locally, point the upload at an unreachable address instead.
The plugin writes the bundle first and then fails at the upload step, which is expected here.

The plugin also requires a `central` server entry in the Maven settings even when it does not upload anything.
Use a settings file with placeholder credentials (do not put real credentials here):

```xml
<!-- /tmp/central-dummy-settings.xml -->
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>dummy</username>
      <password>dummy</password>
    </server>
  </servers>
</settings>
```

```sh
mvn -s /tmp/central-dummy-settings.xml -P release -Dgpg.skip -DskipITs \
  -DcentralBaseUrl=http://127.0.0.1:1 clean deploy
# The build ends with "Unable to upload bundle for deployment". This is expected.
unzip -l target/central-publishing/central-bundle.zip
```

The bundle must contain the POM files of the three modules and the jar, sources jar and javadoc jar of `mattermost-models` and `mattermost4j-core`, with `.md5` and `.sha1` checksums, under `io/github/ug23/`.
`.asc` signatures are missing because of `-Dgpg.skip`.
The plugin also adds `.sha256` and `.sha512` checksums, and copies the `_remote.repositories` and `maven-metadata-local.xml` files of its staging repository into the bundle.

### Integration tests

The integration tests (`mattermost4j-core/src/test/java/net/bis5/mattermost/client4/api`) need a running Mattermost server and are not run on CI.
Run them locally before a release.

```sh
DOCKER_DEFAULT_PLATFORM=linux/amd64 MATTERMOST_VERSION=6.3.0 docker-compose up -d
mvn verify
docker-compose down
```

`DOCKER_DEFAULT_PLATFORM=linux/amd64` is needed on Apple Silicon, because the `mysql:5.7` image has no arm64 variant.

## Deploying from a local machine

Use this only when the release workflow cannot be used.

1. Add the Central Portal user token to `~/.m2/settings.xml`.

   ```xml
   <settings>
     <servers>
       <server>
         <id>central</id>
         <username><!-- username part of the user token --></username>
         <password><!-- password part of the user token --></password>
       </server>
     </servers>
   </settings>
   ```

2. Import the GPG key into the local keyring (or use the key you created above).
3. Pass the GPG passphrase in the `MAVEN_GPG_PASSPHRASE` environment variable and deploy.

   ```sh
   read -rs MAVEN_GPG_PASSPHRASE && export MAVEN_GPG_PASSPHRASE
   mvn -P release -DskipITs clean deploy
   unset MAVEN_GPG_PASSPHRASE
   ```

   If `gpg-agent` already caches the passphrase, the environment variable is not required.
   **Do not** pass the passphrase with `-Dgpg.passphrase` or write it into `settings.xml`.
   The project enables `bestPractices` of `maven-gpg-plugin`.
   With it, the plugin fails the build when the passphrase is given as `-Dgpg.passphrase`, and it never reads a passphrase from `settings.xml`.
4. Check and publish the deployment on <https://central.sonatype.com/publishing/deployments> as described in the release procedure.
   Create the tag and the GitHub Release by hand in this case.
