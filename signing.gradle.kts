import java.io.FileInputStream
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

extra["releaseStoreFile"] = keystoreProperties.getProperty("keyStore.file") ?: "release.keystore"
extra["releaseStorePassword"] = keystoreProperties.getProperty("keyStore.password") ?: ""
extra["releaseKeyAlias"] = keystoreProperties.getProperty("key.alias") ?: ""
extra["releaseKeyPassword"] = keystoreProperties.getProperty("key.password") ?: ""
