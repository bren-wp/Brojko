# BROJKO — reproducibilna Gradle gradnja

Gradle Wrapper preuzet je iz izvornog Gradle GitHub repozitorija
(`v9.8.2`) i uspoređen sa službeno objavljenom SHA-256 vrijednosti.

- Gradle `9.8.2`; JDK 17; Android SDK 36 / Build Tools 36.0.0
- Wrapper JAR SHA-256: `3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98`
- Distribution ZIP SHA-256: `21bd5da2de49b642904077f38c0efdc7778e92b3ed0889392dca61b97e389607`
- Android Gradle Plugin 9.5.1

Linux/macOS: `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

Windows: `gradlew.bat --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

Distribucija i wrapper JAR imaju SHA-256 verifikaciju. CI s
`gradle/actions/setup-gradle` dodatno validira službeni wrapper JAR.
Privatni keystoreovi i `local.properties` nisu dio repozitorija.
Linux CI mora potvrditi čist checkout; Windows build zahtijeva zasebnu provjeru.
