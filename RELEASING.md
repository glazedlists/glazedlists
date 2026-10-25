# Publishing to Maven Central

Artifacts are published through the [Sonatype Central Publisher Portal](https://central.sonatype.com).
Release versions are uploaded manually as a bundle. Snapshots are deployed directly to the Portal
snapshot repository.

## Prerequisites

- A Portal account with access to the verified namespace `com.glazedlists`.
- A Portal user token (Portal → *View Account* → *Generate User Token*) in `~/.gradle/gradle.properties`:
  ```
  sonatypeUsername=<token username>
  sonatypePassword=<token password>
  ```
  Old OSSRH credentials do not work with the Portal.
- For snapshots: *Enable SNAPSHOTs* for the namespace on the Portal's namespaces page.
- A GPG signing key whose public key is published on a keyserver (e.g. `keyserver.ubuntu.com`,
  `keys.openpgp.org`), configured in `~/.gradle/gradle.properties`:
  ```
  signing.keyId=<last 8 hex digits of the key id>
  signing.password=<passphrase>
  signing.secretKeyRingFile=<path to secring.gpg>
  ```
- JDK 8 or 11 to run Gradle 5.3.1.

## Snapshots

```
./gradlew :uploadArchives
```

Deploys the current `-SNAPSHOT` version to `https://central.sonatype.com/repository/maven-snapshots/`.
Snapshots are removed by Sonatype after 90 days.

## Releases

1. Build the signed bundle for the version in `gradle.properties` (`nextReleaseVersion`):
   ```
   ./gradlew clean centralBundle -PisSnapshot=false
   ```
   This deploys the jar, sources, javadoc, dist zip and POM, with signatures and checksums, into the
   local repository `target/central-staging`, and zips it into
   `target/glazedlists-<version>-central-bundle.zip`.
2. Upload the bundle: Portal → *Publish Component* → enter a deployment name (e.g.
   `com.glazedlists:glazedlists:<version>`) → upload the zip.
   Alternatively, upload it with curl, using the base64 encoding of `<token username>:<token password>`:
   ```
   curl -H "Authorization: Bearer <base64 token>" -F bundle=@target/glazedlists-<version>-central-bundle.zip "https://central.sonatype.com/api/v1/publisher/upload?publishingType=USER_MANAGED"
   ```
3. Wait until the deployment is **VALIDATED** (on **FAILED**, the Portal lists the problems). Nothing is
   public yet.
4. Optional smoke test: a validated deployment can be consumed before publishing, e.g. from a
   throwaway Gradle project. The Bearer token is built from the Portal user token in
   `~/.gradle/gradle.properties`, so no credentials end up in the build file:
   ```groovy
   repositories {
       maven {
           url "https://central.sonatype.com/api/v1/publisher/deployments/download/"
           credentials(HttpHeaderCredentials) {
               name = "Authorization"
               value = "Bearer " + "${sonatypeUsername}:${sonatypePassword}".bytes.encodeBase64().toString()
           }
           authentication { header(HttpHeaderAuthentication) }
       }
   }
   dependencies {
       implementation "com.glazedlists:glazedlists:<version>"
   }
   ```
5. In the Portal, click **Publish** to release to Maven Central, or **Drop** to discard the
   deployment.
