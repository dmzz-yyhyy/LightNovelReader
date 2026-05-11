package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import dalvik.system.PathClassLoader

/**
 * 适用于 Android API 27 (Android 8.1) 以下版本的 [dalvik.system.DelegateLastClassLoader] 向下兼容实现。
 *
 * ## 背景
 *
 * Android 标准类加载器（[PathClassLoader] / [dalvik.system.DexClassLoader]）遵循 Java
 * **亲父委托模型（Parent-First Delegation）**：
 * 1. 检查已缓存的已加载类
 * 2. 委托给父类加载器（host app）
 * 3. 在自身的 DEX 文件中查找
 *
 * 这一模型在插件场景中会引发 **类冲突**：若宿主应用与插件包含同名类，
 * 父类加载器（宿主）的版本总会优先被加载，导致插件无法覆盖宿主类。
 *
 * ## DelegateLastClassLoader 的解决方案
 *
 * [dalvik.system.DelegateLastClassLoader]（API 27+）反转了委托顺序，实现
 * **自身优先（Self-First / Delegate-Last）** 策略：
 * 1. 检查已缓存的已加载类
 * 2. **优先**在自身的 DEX 文件中查找
 * 3. 若未找到，再委托给父类加载器
 *
 * 这样可确保插件自身的类定义始终优先于宿主中的同名类，彻底避免插件与宿主的类冲突。
 *
 * ## 此类的用途
 *
 * 本类在 API < 27 的设备上模拟上述行为，仅重写 [loadClass] 以改变委托顺序，
 * 其余所有行为（DEX 加载、native 库查找等）完全继承自 [PathClassLoader]。
 *
 * 在 API ≥ 27 的设备上，应直接使用系统提供的 [dalvik.system.DelegateLastClassLoader]，
 * 参见 [indi.dmzz_yyhyy.lightnovelreader.utils.classLoader]。
 *
 * ## 类加载顺序对比
 *
 * ```
 * PathClassLoader（标准委托，父优先）：
 *   已缓存 → 父类加载器 → 自身 DEX
 *
 * DesugarDelegateLastClassLoader（委托延后，自身优先）：
 *   已缓存 → 自身 DEX → 父类加载器
 * ```
 *
 * @param dexPath           包含类与资源的 jar/apk 文件列表，以 [java.io.File.pathSeparator] 分隔
 * @param librarySearchPath 包含 native 库的目录列表，以 [java.io.File.pathSeparator] 分隔；可为 `null`
 * @param parent            父类加载器；可为 `null` 以使用 boot class loader
 *
 * @see dalvik.system.DelegateLastClassLoader
 * @see dalvik.system.PathClassLoader
 * @see indi.dmzz_yyhyy.lightnovelreader.utils.classLoader
 */
class DesugarDelegateLastClassLoader(
    dexPath: String,
    librarySearchPath: String?,
    parent: ClassLoader?
) : PathClassLoader(dexPath, librarySearchPath, parent) {

    /**
     * 以「自身优先、父类延后」的顺序加载类，模拟 [dalvik.system.DelegateLastClassLoader] 的行为。
     *
     * 加载步骤：
     * 1. **已缓存检查**：若类已由本加载器加载过，直接返回缓存结果（快速路径）。
     * 2. **自身 DEX 查找**：调用 [findClass] 在本加载器持有的 DEX 文件中查找。
     *    找到则立即返回，**不再询问父类加载器**，从而保证插件类优先于宿主同名类。
     * 3. **父类委托**：仅当本地 DEX 中不存在该类时，才将请求转发给父类加载器。
     *    若父类也找不到，则由父类负责抛出 [ClassNotFoundException]。
     * 4. **无父类情况**：若 [parent] 为 `null`，直接抛出 [ClassNotFoundException]。
     *
     * @param name    需要加载的类的二进制名称（如 `"java.lang.String"`）
     * @param resolve 是否需要解析该类（Android 上此参数通常被忽略）
     * @return 已加载的 [Class] 对象
     * @throws ClassNotFoundException 在本地 DEX 与父类加载器中均未找到指定类时抛出
     */
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        // 步骤 1：检查此类是否已被加载（快速路径，避免重复加载）
        findLoadedClass(name)?.let { return it }

        // 步骤 2：优先在自身 DEX 文件中查找（Delegate-Last 核心逻辑）
        try {
            return findClass(name)
        } catch (_: ClassNotFoundException) {
            // 本地未找到，继续委托给父类加载器
        }

        // 步骤 3：委托给父类加载器；若无父类则抛出异常
        val parent = parent
        if (parent != null) {
            return parent.loadClass(name)
        }
        throw ClassNotFoundException(name)
    }
}
