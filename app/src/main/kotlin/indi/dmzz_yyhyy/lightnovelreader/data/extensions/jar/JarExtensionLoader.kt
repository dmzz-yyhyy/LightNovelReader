package indi.dmzz_yyhyy.lightnovelreader.data.extensions.jar

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads JAR-based extensions
 */
@Singleton
class JarExtensionLoader @Inject constructor() {

    /**
     * Load an extension from a JAR file
     */
    fun loadExtension(file: File, extensionEntity: InstalledExtensionEntity): Extension? {
        return try {
            val jarFile = JarFile(file)
            val mainClass = findExtensionClass(jarFile)
            
            if (mainClass != null) {
                val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()))
                val clazz = classLoader.loadClass(mainClass)
                
                // Check if the class implements Extension interface
                if (Extension::class.java.isAssignableFrom(clazz)) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.newInstance() as Extension
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Find the main extension class in the JAR manifest or by scanning
     */
    private fun findExtensionClass(jarFile: JarFile): String? {
        // First, try to get the main class from manifest
        val manifest = jarFile.manifest
        val mainClass = manifest?.mainAttributes?.getValue("Extension-Class")
        if (mainClass != null) {
            return mainClass
        }

        // If not found in manifest, scan for classes implementing Extension
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.name.endsWith(".class") && !entry.isDirectory) {
                val className = entry.name
                    .replace("/", ".")
                    .removeSuffix(".class")
                
                // This is a simple heuristic - in a real implementation,
                // you'd want to actually load and check the class
                if (className.contains("Extension") && !className.contains("$")) {
                    return className
                }
            }
        }
        
        return null
    }

    /**
     * Validate a JAR file for extension loading
     */
    fun validateJarExtension(file: File): ValidationResult {
        return try {
            val jarFile = JarFile(file)
            val mainClass = findExtensionClass(jarFile)
            
            if (mainClass != null) {
                ValidationResult.Success
            } else {
                ValidationResult.NoExtensionClass
            }
        } catch (e: Exception) {
            ValidationResult.InvalidJar(e.message ?: "Unknown error")
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        object NoExtensionClass : ValidationResult()
        data class InvalidJar(val error: String) : ValidationResult()
    }
}
