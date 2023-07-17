plugins {
    id("com.android.application") version "7.3.0" apply false
    id("com.android.library") version "7.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}
System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3")