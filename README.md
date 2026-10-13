# MathMantra

Maths tutor for children, built around listening, speaking and moving, so it works for kids
who are blind or have low vision as well as sighted kids. Every prompt is read aloud, every
control has a screen-reader label, and answers are announced.

Runs on **Android, iOS and desktop** from one Kotlin Multiplatform codebase with a shared
Compose Multiplatform UI.

| Activity | What the child does |
| --- | --- |
| Ring the Bell | Shakes the phone *N* times (tap on desktop) |
| Play the Drum | Taps the drum *N* times |
| Count Aloud | Counts a range out loud; speech recognition checks it and names missed numbers |
| Lessons | Listens to short stories about zero, the four operations, percentages and time |
| Number Line | Answers `a ± b` by walking a mole along a number line |
| Pick the Answer | Multiple-choice arithmetic |
| Quick Quiz | Types the answer to an arithmetic question |

## Project layout

```
shared/          Kotlin Multiplatform module: all UI, game logic and resources
  commonMain/    Compose UI, ViewModels, domain logic, composeResources/
  androidMain/   TextToSpeech, SoundPool, SensorManager, SpeechRecognizer
  iosMain/       AVSpeechSynthesizer, AVAudioPlayer, CoreMotion, SFSpeechRecognizer
  desktopMain/   OS speech engine, Java Sound
  commonTest/    Unit tests for game logic and ViewModels
  desktopTest/   Renders every screen headlessly (light + dark) to shared/build/screens/
androidApp/      Android application shell (MainActivity, manifest, launcher icon)
iosApp/          Xcode project (generated from project.yml with XcodeGen)
desktopApp/      Desktop application shell
```

Platform features sit behind four small interfaces in
`shared/src/commonMain/.../platform/AppServices.kt` (`Speaker`, `SoundPlayer`, `MotionSensor`,
`SpeechRecognizer`), so the games themselves are plain common code.

## Tech stack

Kotlin 2.4 · Compose Multiplatform 1.12 · Material 3 · Navigation Compose (type-safe routes) ·
AndroidX ViewModel + StateFlow · Gradle 9.8 with version catalog and configuration cache ·
AGP 9.4 · compile/target SDK 37, min SDK 26 · iOS 17+ · JDK 21.

## Building

Requirements: JDK 21 and the Android SDK; for iOS, a Mac with Xcode and
[XcodeGen](https://github.com/yonaskolb/XcodeGen).

```sh
./gradlew :androidApp:installDebug          # Android device or emulator
./gradlew :desktopApp:run                   # Desktop
./gradlew :shared:desktopTest               # Unit tests + screen renders
./gradlew :androidApp:lintDebug             # Lint (warnings fail the build)
```

iOS:

```sh
cd iosApp && xcodegen generate && open MathMantra.xcodeproj
```

Xcode's build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` to build the
shared framework. Set your signing team in Xcode before running on a device.

## Credits

Font: [Nunito](https://github.com/googlefonts/nunito), SIL Open Font License (`licenses/Nunito-OFL.txt`).
