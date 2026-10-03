#!/usr/bin/env bash
# Reproducible minimal SDK build. Uses javac or optional Eclipse compiler.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to your Android SDK}"
sdk_tools="$ANDROID_SDK_ROOT/build-tools/35.0.0"
android_jar="$ANDROID_SDK_ROOT/platforms/android-35/android.jar"
out="$(mktemp -d "$PWD/build-sdk-XXXXXX")"
mkdir -p "$out/gen" "$out/classes" "$out/dex" "$out/tests"
# Gradle supplies the package; raw AAPT2 needs it in its generated manifest.
sed 's/<manifest xmlns:android=/<manifest package="com.local.simpleprint" xmlns:android=/' app/src/main/AndroidManifest.xml > "$out/AndroidManifest.xml"
"$sdk_tools/aapt2" compile --dir app/src/main/res -o "$out/resources.zip"
"$sdk_tools/aapt2" link -o "$out/base.apk" -I "$android_jar" --manifest "$out/AndroidManifest.xml" \
	--java "$out/gen" --min-sdk-version 31 --target-sdk-version 35 --version-code 7 --version-name 0.7 "$out/resources.zip"
mapfile -t sources < <(find app/src/main/java "$out/gen" -name '*.java')
if [[ -n "${ECJ_JAR:-}" ]]; then
	java -jar "$ECJ_JAR" -8 -cp "$android_jar" -d "$out/classes" "${sources[@]}"
	java -jar "$ECJ_JAR" -8 -cp "$out/classes:$android_jar" -d "$out/tests" tests/CoreTests.java
else
	javac -source 8 -target 8 -cp "$android_jar" -d "$out/classes" "${sources[@]}"
	javac -source 8 -target 8 -cp "$out/classes:$android_jar" -d "$out/tests" tests/CoreTests.java
fi
java -cp "$out/tests:$out/classes:$android_jar" com.local.simpleprint.CoreTests
mapfile -t classes < <(find "$out/classes" -name '*.class')
"$sdk_tools/d8" --lib "$android_jar" --min-api 31 --output "$out/dex" "${classes[@]}"
cp "$out/base.apk" "$out/unsigned.apk"
(cd "$out/dex" && zip -q "$out/unsigned.apk" classes.dex)
"$sdk_tools/zipalign" -f 4 "$out/unsigned.apk" "$out/aligned.apk"
# Use a private maintainer key via SIMPLEPRINT_KEYSTORE, or a local development key.
key="${SIMPLEPRINT_KEYSTORE:-$HOME/.android/debug.keystore}"
if [[ ! -f "$key" ]]; then
	mkdir -p "$(dirname "$key")"
	keytool -genkeypair -keystore "$key" -storepass android -alias androiddebugkey -keypass android \
		-dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -keysize 2048 -validity 10000
fi
"$sdk_tools/apksigner" sign --ks "$key" --ks-pass pass:android --key-pass pass:android \
	--out "$PWD/SimplePrint-v0.7.apk" "$out/aligned.apk"
"$sdk_tools/apksigner" verify --verbose "$PWD/SimplePrint-v0.7.apk"
"$sdk_tools/aapt2" dump badging "$PWD/SimplePrint-v0.7.apk"
