# Naturblick Android

This project contains the code for the [Android app
Naturblick](https://play.google.com/store/apps/details?id=com.mfn_berlin_stadtnatur_entdecken.naturblick). The
project is hosted at the [Museum für Naturkunde
Berlin](https://www.museumfuernaturkunde.berlin/en).  The code is
licensed under MIT license (see [LICENSE.txt](LICENSE.txt) for details). If you want
to contribute please take a look at [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) and
[CONTRIBUTING.md](CONTRIBUTING.md).

## Open Source

We believe in the advantages of open source to foster transparency and
accountability. Anyone interested can view and verify our work. While
reusability of the code is not our primary goal, we welcome and
appreciate any feedback on the security and quality of our code. Feel
free to open up an issue or just contact us <naturblick@mfn.berlin>.

## Release a new version

* Checkout the `main` branch `git checkout main`
* Sync the repo with origin `git pull`
* Update the version file [`version.properties`](version.properties) with the new version
* Add the version file to the commit `git add version.properties`
* Commit the version update `git commit -m "Releasing version <x.y.z>"`, e.g. "Releasing version 1.2.3"
* Push the commit  `git push`
* Tag the commit `git tag v<x.y.z>`, e.g. "v1.2.3"
* Push the tag `git push origin v<x.y.z>`
* The new version will now be built by the CI and the signed bundle will be available as a build artefact for the tag
* Upload the bundle to the play console

## Building

There are two possible ways to build this project. Either it can be
opened using [android studio](https://developer.android.com/studio)
or using gradle by manually installing the required build tools.

### Mapbox download key

As described
[here](https://docs.mapbox.com/android/maps/guides/install/#configure-credentials)
it is required to configure a mapbox download key to be able to
compile the project with mapbox android SDK. The simplest way to
achieve this is to put it into the `.gradle/gradle.properties` file of
your home folder.