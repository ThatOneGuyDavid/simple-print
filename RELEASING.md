# Maintainer release process

Keep the existing signing keystore in private storage outside the repository.
Public source, tags, archives, and automation must never include it.

1. Update versionCode/versionName in app/build.gradle and tools/build-sdk.sh.
2. Run the SDK build with SIMPLEPRINT_KEYSTORE pointing to the private key.
   The development key uses the conventional Android debug alias/password.
3. Check APK signature, package/version, tests, and hardware as available.
   Record exactly what was tested in TESTING.md and release notes.
4. Place only the signed APK and SHA256SUMS in artifacts/vVERSION/.
5. Commit source, then map the version to that commit in release-manifest.json.
6. Publication automation creates tags and releases from the already-built APKs.
   It never signs or rebuilds binaries and needs no private key.

Do not overwrite an existing tag or asset with different bytes. Make a new
version for changes. Independent local builds use a different signing key unless
explicitly configured with the maintainer's private key.
