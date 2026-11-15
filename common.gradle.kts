import com.vanniktech.maven.publish.MavenPublishBaseExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

val mcVersion = (project.extra["mcVersion"] as Number).toInt()
val unobfuscated = mcVersion >= 26_00_00

pluginManager.apply(if (unobfuscated) "net.fabricmc.fabric-loom" else "net.fabricmc.fabric-loom-remap")
pluginManager.apply("com.replaymod.preprocess")
pluginManager.apply("me.fallenbreath.yamlang")
pluginManager.apply("com.vanniktech.maven.publish")

fun gradleProperty(name: String): String = project.findProperty(name).toString()

fun gradlePropertyOrNull(name: String): String? = project.findProperty(name)?.toString()

val minecraftVersion = gradleProperty("minecraftVersion")
val parchmentVersion = gradleProperty("parchmentVersion")
val carpetVersion = gradleProperty("carpetVersion")
val modVersion = gradleProperty("modVersion")
val modId = gradleProperty("modId")
val modName = gradleProperty("modName")
val modDescription = gradleProperty("modDescription")
val modSource = gradleProperty("modSource")
val modWebsite = gradleProperty("modWebsite")
val minecraftDependency = gradleProperty("minecraftDependency")
val fabricloaderDependency = gradleProperty("fabricloaderDependency")
val mavenGroup = gradleProperty("mavenGroup")
val archivesBaseName = gradleProperty("archivesBaseName")
val modrinthUrl = gradleProperty("modrinthUrl")
val curseforgeUrl = gradleProperty("curseforgeUrl")
val issueTrackerUrl: String = "$modSource/issues"

repositories {
    // Local mirror for artifacts that some networks cannot download directly (parchment mappings).
    maven {
        url = uri(rootProject.file("local-repo"))
        content { includeGroup("org.parchmentmc.data") }
    }
    maven {
        url = uri("https://maven.parchmentmc.org")
        content { includeGroup("org.parchmentmc.data") }
    }
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github")
            includeGroupByRegex("com\\.github\\..+")
        }
    }
    maven {
        url = uri("https://maven.fallenbreath.me/releases")
        content { includeGroup("me.fallenbreath") }
    }
    // Mod Menu (optional client dependency)
    maven {
        url = uri("https://maven.terraformersmc.com/releases")
        content { includeGroup("com.terraformersmc") }
    }
    maven {
        url = uri("https://masa.dy.fi/maven")
        content {
            includeGroup("carpet")
            // the 1.21.1 malilib build is only published on the main masa repository,
            // the sakura-ryoko repository starts at 1.21.3
            includeGroupAndSubgroups("fi.dy.masa")
        }
    }
    // malilib / litematica (sakura-ryoko builds), only needed by the client jar
    maven {
        url = uri("https://masa.dy.fi/maven/sakura-ryoko")
        content { includeGroupAndSubgroups("fi.dy.masa") }
    }
}

val loomExtension = extensions.getByType(LoomGradleExtensionAPI::class)

dependencies {
    fun processDependency(dep: Dependency?): Dependency? {
        // https://github.com/FabricMC/fabric-loader/issues/783
        if (dep is ModuleDependency && !(dep.group == "net.fabricmc" && dep.name == "fabric-loader")) {
            dep.exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-loader"))
        }
        return dep
    }

    fun autoImplementation(dep: Any): Dependency? = processDependency(add(if (unobfuscated) "implementation" else "modImplementation", dep))

    fun autoRuntimeOnly(dep: Any): Dependency? = processDependency(add(if (unobfuscated) "runtimeOnly" else "modRuntimeOnly", dep))

    fun autoCompileOnly(dep: Any): Dependency? = processDependency(add(if (unobfuscated) "compileOnly" else "modCompileOnly", dep))

    fun includeDependency(dep: Any?): Dependency? = processDependency(add("include", requireNotNull(dep)))

    // loom
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    if (!unobfuscated) {
        @Suppress("UnstableApiUsage")
        add(
            "mappings",
            loomExtension.layered {
                officialMojangMappings()
                if (parchmentVersion.isNotEmpty()) {
                    parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
                }
            },
        )
    }
    autoImplementation(libs.fabric.loader.get())
    autoImplementation("carpet:fabric-carpet:$carpetVersion")

    // The obfuscated (1.21.x) server sources touch carpet/fabric-api types, so fabric-api has to be
    // on the main compile classpath as well. The unobfuscated (26.x) sources do not reference it.
    if (!unobfuscated) {
        val mainFabricApiVersion = gradlePropertyOrNull("clientFabricApiVersion")
        if (mainFabricApiVersion != null) {
            autoImplementation("net.fabricmc.fabric-api:fabric-api:$mainFabricApiVersion")
        }
    }

    // https://central.sonatype.com/artifact/org.jspecify/jspecify
    autoCompileOnly(libs.jspecify.get())
    // runtime mods
    autoRuntimeOnly("me.fallenbreath:mixin-auditor:0.2.0-${if (unobfuscated) "u" else "o"}")
}

val mixinConfigPath = "remote-bot-fetch-server.mixins.json"
val langDir = "assets/remotebotfetchserver/lang"
val javaCompatibility =
    when {
        mcVersion >= 26_00_00 -> JavaVersion.VERSION_25
        mcVersion >= 12005 -> JavaVersion.VERSION_21
        mcVersion >= 11800 -> JavaVersion.VERSION_17
        mcVersion >= 11700 -> JavaVersion.VERSION_16
        else -> JavaVersion.VERSION_1_8
    }
val mixinCompatibilityLevel = javaCompatibility

val commonVmArgs = listOf("--sun-misc-unsafe-memory-access=allow", "-Dmixin.debug.export=true")
loomExtension.runConfigs.configureEach {
    runDirectory.set(
        file(
            if (unobfuscated) "../../run" else "../../run-obsuscated",
        ),
    )
    jvmArguments.addAll(commonVmArgs)
}
loomExtension.runs {
    val auditVmArgs = "-DmixinAuditor.audit=true"
    create("serverMixinAudit") {
        server()
        jvmArguments.add(auditVmArgs)
    }
    create("clientMixinAudit") {
        client()
        jvmArguments.add(auditVmArgs)
    }
}

tasks.matching { it.name.startsWith("run") }.configureEach {
    dependsOn(rootProject.subprojects.map { subproject -> subproject.tasks.matching { it.name == "downloadAssets" } })
}

loomExtension.accessWidenerPath.set(file("remote-bot-fetch-server.accesswidener"))

var modVersionSuffix = ""
val artifactVersion = modVersion
var artifactVersionSuffix = ""
// detect github action environment variables
// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
if (System.getenv("BUILD_RELEASE") != "true") {
    val buildNumber = System.getenv("BUILD_ID")
    modVersionSuffix += if (buildNumber != null) "+build.$buildNumber" else "-SNAPSHOT"
    artifactVersionSuffix = "-SNAPSHOT" // A non-release artifact is always a SNAPSHOT artifact
}
val fullModVersion = modVersion + modVersionSuffix
var fullProjectVersion = ""
var fullArtifactVersion = ""

// Example version values:
//   project.mod_version     1.0.3                      (the base mod version)
//   modVersionSuffix        +build.88                  (use github action build number if possible)
//   artifactVersionSuffix   -SNAPSHOT
//   fullModVersion          1.0.3+build.88             (the actual mod version to use in the mod)
//   fullProjectVersion      v1.0.3-mc1.15.2+build.88   (in build output jar name)
//   fullArtifactVersion     1.0.3-mc1.15.2-SNAPSHOT    (maven artifact version)

group = mavenGroup
val baseExtension = extensions.getByType(BasePluginExtension::class)
if (System.getenv("JITPACK") == "true") {
    // move mc version into archivesBaseName, so jitpack will be able to organize archives from multiple subprojects correctly
    baseExtension.archivesName.set("$archivesBaseName-mc$minecraftVersion")
    fullProjectVersion = "v$modVersion$modVersionSuffix"
    fullArtifactVersion = artifactVersion + artifactVersionSuffix
} else {
    baseExtension.archivesName.set(archivesBaseName)
    fullProjectVersion = "v$modVersion-mc$minecraftVersion$modVersionSuffix"
    fullArtifactVersion = "$artifactVersion-mc$minecraftVersion$artifactVersionSuffix"
}
version = fullProjectVersion

// See https://youtrack.jetbrains.com/issue/IDEA-296490
// if IDEA complains about "Cannot resolve resource filtering of MatchingCopyAction" and you want to know why
val modProperties =
    mapOf(
        "id" to modId,
        "name" to modName,
        "version" to fullModVersion,
        "description" to modDescription,
        "source" to modSource,
        "website" to modWebsite,
        "issues" to issueTrackerUrl,
        "modrinth" to modrinthUrl,
        "curseforge" to curseforgeUrl,
        "minecraft_dependency" to minecraftDependency,
        "fabricloader_dependency" to fabricloaderDependency,
    )

tasks.named<ProcessResources>("processResources") {
    from(project.file("remote-bot-fetch-server.accesswidener"))
    inputs.properties(modProperties)

    filesMatching("fabric.mod.json") {
        expand(modProperties)
    }

    filesMatching(mixinConfigPath) {
        filter { line: String ->
            line.replace("{{COMPATIBILITY_LEVEL}}", "JAVA_${mixinCompatibilityLevel.ordinal + 1}")
        }
    }
}

// ---------------------------------------------------------------------------
// "Remote Bot Fetch Client" - client-only mod jar
//
// The project ships two mods from one source tree:
//   * server jar -> src/main  (mod id "remote-bot-fetch", needs carpet)
//   * client jar -> src/client (mod id "remote-bot-fetch-client", needs malilib + carpet)
//
// The "client" source set is picked up by the preprocessor plugin automatically,
// so the client code is preprocessed for every MC version just like src/main.
// ---------------------------------------------------------------------------
val clientModId = gradlePropertyOrNull("clientModId") ?: "remote-bot-fetch-client"
val clientModName = gradlePropertyOrNull("clientModName") ?: "Remote Bot Fetch Client"
val clientMixinConfigPath = "remote-bot-fetch-client.mixins.json"
val malilibVersion = gradlePropertyOrNull("malilibVersion")
val litematicaVersion = gradlePropertyOrNull("litematicaVersion")
// 1.21.1 shares the 1.21 builds of malilib / litematica (there is no 1.21.1 artifact), so the
// artifact suffix can be overridden per version.
val malilibMinecraftVersion = gradlePropertyOrNull("malilibMinecraftVersion") ?: minecraftVersion
val litematicaMinecraftVersion = gradlePropertyOrNull("litematicaMinecraftVersion") ?: minecraftVersion
// The client mod is built for every MC version that has a malilib build available.
val clientEnabled = !malilibVersion.isNullOrEmpty() && gradlePropertyOrNull("clientFabricApiVersion") != null

// The "client" mixin list is version dependent, it is injected into the mixin config by
// processClientResources below:
//   * the litematica integration is only compiled when a litematica artifact exists
//   * the IME preedit hook (PreeditEvent) only exists since 26.1.2
val clientMixinEntries =
    buildList {
        add("lms.AbstractContainerScreenMixin")
        add("lms.CreativeModeInventoryScreenMixin")
        add("rbf.AbstractRecipeBookScreenMixin")
        add("rbf.ContainerScreenAccessor")
        add("rbf.ContainerScreenPanelInputMixin")
        add("rbf.InventoryScreenMixin")
        add("rbf.RecipeBookComponentAccessor")
        add("rbf.RecipeBookComponentMixin")
        if (!litematicaVersion.isNullOrEmpty()) {
            add("lms.MaterialListEntryMixin")
            add("lms.WorldUtilsMixin")
        }
        if (mcVersion >= 26_01_01) {
            add("rbf.ScreenPreeditMixin")
        }
        // 1.21.1 does not override mouseScrolled in AbstractContainerScreen, the default
        // ContainerEventHandler implementation has to be hooked instead there.
        if (mcVersion < 12103) {
            add("rbf.ContainerEventHandlerScrollMixin")
        }
    }
val clientMixinListJson = clientMixinEntries.joinToString(",\n    ") { "\"$it\"" }

val clientSourceSet: SourceSet? =
    if (clientEnabled) {
        val sourceSets = extensions.getByType(SourceSetContainer::class)
        val client = sourceSets.create("client")
        client.compileClasspath += configurations["compileClasspath"]
        client.runtimeClasspath += configurations["runtimeClasspath"]

        val clientFabricApiVersion = gradleProperty("clientFabricApiVersion")
        // Obfuscated (<= 1.21.11) projects need Loom's remapped configurations for the client source
        // set as well, otherwise malilib / litematica / fabric-api would stay in the official
        // namespace while the rest of the client classpath is remapped.
        // Loom names them "modClient*" for a non-main source set named "client".
        if (!unobfuscated) {
            loomExtension.createRemapConfigurations(client)
        }
        val clientImplementationName = if (unobfuscated) "clientImplementation" else "modClientImplementation"
        val clientCompileOnlyName = if (unobfuscated) "clientCompileOnly" else "modClientCompileOnly"
        val clientRuntimeOnlyName = if (unobfuscated) "clientRuntimeOnly" else "modClientRuntimeOnly"
        dependencies {
            add(clientImplementationName, "net.fabricmc.fabric-api:fabric-api:$clientFabricApiVersion")
            add(clientImplementationName, "fi.dy.masa.malilib:malilib-fabric-$malilibMinecraftVersion:$malilibVersion")
            // litematica is optional at runtime (ClientMixinPlugin gates the two litematica mixins),
            // it is only on the dev classpath so that the integration can be tested with the dev runs.
            if (!litematicaVersion.isNullOrEmpty()) {
                add(clientCompileOnlyName, "fi.dy.masa.litematica:litematica-fabric-$litematicaMinecraftVersion:$litematicaVersion")
                add(clientRuntimeOnlyName, "fi.dy.masa.litematica:litematica-fabric-$litematicaMinecraftVersion:$litematicaVersion")
            }
            // Mod Menu is optional at runtime: the "modmenu" entrypoint is only called when Mod Menu
            // is installed, so the API is compile-only for the jar and on the dev classpath for testing.
            val modmenuVersion = gradlePropertyOrNull("modmenuVersion")
            if (!modmenuVersion.isNullOrEmpty()) {
                add(clientCompileOnlyName, "com.terraformersmc:modmenu:$modmenuVersion")
                add(clientRuntimeOnlyName, "com.terraformersmc:modmenu:$modmenuVersion")
            }
        }

        // The main source set already puts fabric-loader on the client classpath, drop the transitive one
        // pulled in by the client dependencies, otherwise the dev launch fails with duplicate loader classes.
        listOf(
            clientCompileOnlyName,
            clientRuntimeOnlyName,
            clientImplementationName,
            "clientCompileClasspath",
            "clientRuntimeClasspath",
        ).forEach { configurationName ->
            configurations.named(configurationName).configure {
                exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-loader"))
            }
        }

        val clientModProperties =
            modProperties + mapOf(
                "id" to clientModId,
                "name" to clientModName,
            )
        tasks.named<ProcessResources>("processClientResources") {
            inputs.properties(clientModProperties)
            // the placeholders below are resolved from constants, make sure a change to them
            // invalidates the (otherwise identical) copied resources
            inputs.property("rbfClientMixins", clientMixinListJson)
            inputs.property("rbfCompatibilityLevel", "JAVA_${mixinCompatibilityLevel.ordinal + 1}")

            filesMatching("fabric.mod.json") {
                expand(clientModProperties)
            }

            filesMatching(clientMixinConfigPath) {
                filter { line: String ->
                    line.replace("{{COMPATIBILITY_LEVEL}}", "JAVA_${mixinCompatibilityLevel.ordinal + 1}")
                        .replace("{{RBF_CLIENT_MIXINS}}", clientMixinListJson)
                }
            }
        }

        val clientJar =
            tasks.register<Jar>("clientJar") {
                group = "build"
                description = "Assembles the client-only mod jar (Remote Bot Fetch Client)."
                archiveClassifier.set("client")
                from(client.output)
                from(rootProject.file("LICENSE")) {
                    rename { name -> "${name}_${archivesBaseName}-client" }
                }
                manifest {
                    attributes(
                        mapOf(
                            "Implementation-Title" to "$archivesBaseName-client",
                            "Implementation-Version" to fullProjectVersion,
                        ),
                    )
                }
                if (!unobfuscated) {
                    // the remapped (shipped) jar is produced by remapClientJar below
                    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
                }
            }
        tasks.named("assemble") { dependsOn(clientJar) }

        // Obfuscated projects ship the client jar in the intermediary namespace, exactly like the
        // server jar (which Loom remaps in remapJar).
        if (!unobfuscated) {
            val remapClientJar =
                tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapClientJar") {
                    group = "build"
                    description = "Remaps the client-only mod jar (Remote Bot Fetch Client) to intermediary."
                    archiveClassifier.set("client")
                    inputFile.set(clientJar.flatMap { it.archiveFile })
                    addNestedDependencies.set(false)
                    manifest {
                        attributes(
                            mapOf(
                                "Implementation-Title" to "$archivesBaseName-client",
                                "Implementation-Version" to fullProjectVersion,
                            ),
                        )
                    }
                }
            tasks.named("assemble") { dependsOn(remapClientJar) }
        }

        // Universal jar: a single artifact that can be dropped into both the client and the
        // dedicated server. The client entrypoint is a guarded bootstrap (ClientBootstrap) and the
        // client mixins live in the "client" section of their config, so a dedicated server never
        // touches the client classes even though they are present in the jar.
        val universal = sourceSets.create("universal")
        val universalModId = gradlePropertyOrNull("universalModId") ?: "remote-bot-fetch"
        val universalModName = gradlePropertyOrNull("universalModName") ?: "Remote Bot Fetch"
        val universalModProperties =
            modProperties + mapOf(
                "id" to universalModId,
                "name" to universalModName,
            )
        tasks.named<ProcessResources>("processUniversalResources") {
            inputs.properties(universalModProperties)
            filesMatching("fabric.mod.json") {
                expand(universalModProperties)
            }
        }

        val universalJar =
            tasks.register<Jar>("universalJar") {
                group = "build"
                description = "Assembles the universal mod jar (client and server in one file)."
                archiveClassifier.set("universal")
                from(sourceSets.getByName("main").output) {
                    exclude("fabric.mod.json")
                }
                from(client.output) {
                    exclude("fabric.mod.json", "icon.png")
                }
                from(universal.output)
                from(rootProject.file("LICENSE")) {
                    rename { name -> "${name}_${archivesBaseName}-universal" }
                }
                manifest {
                    attributes(
                        mapOf(
                            "Implementation-Title" to "$archivesBaseName-universal",
                            "Implementation-Version" to fullProjectVersion,
                        ),
                    )
                }
                if (!unobfuscated) {
                    // the remapped (shipped) jar is produced by remapUniversalJar below
                    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
                }
            }
        tasks.named("assemble") { dependsOn(universalJar) }

        if (!unobfuscated) {
            val remapUniversalJar =
                tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapUniversalJar") {
                    group = "build"
                    description = "Remaps the universal mod jar to intermediary."
                    archiveClassifier.set("universal")
                    inputFile.set(universalJar.flatMap { it.archiveFile })
                    addNestedDependencies.set(false)
                    manifest {
                        attributes(
                            mapOf(
                                "Implementation-Title" to "$archivesBaseName-universal",
                                "Implementation-Version" to fullProjectVersion,
                            ),
                        )
                    }
                }
            tasks.named("assemble") { dependsOn(remapUniversalJar) }
        }

        // Dev runs that mirror the shipped client jar: only the "client" source set is on the classpath,
        // so the server side classes/mixins of this project are not present, just like in production.
        loomExtension.runs.create("clientMod") {
            client()
            source("client")
        }
        loomExtension.runs.create("clientModMixinAudit") {
            client()
            source("client")
            jvmArguments.add("-DmixinAuditor.audit=true")
        }

        // Mirrors the universal jar on a client: the server side classes/entrypoints are present
        // alongside the client ones, so this run proves that the combined jar boots a client.
        loomExtension.runs.create("universalClient") {
            client()
            source("main")
            source("client")
            jvmArguments.add("-DmixinAuditor.audit=true")
        }

        client
    } else {
        null
    }

// The plain "jar" task always produces the server mod jar, so every version uses the same
// "-server.jar" naming even when the client source set is not compiled for it. Obfuscated
// (<= 1.21.11) projects publish the remapped jar instead, which needs the classifier as well.
tasks.named<Jar>("jar") { archiveClassifier.set("server") }
tasks.matching { it.name == "remapJar" }.configureEach {
    (this as org.gradle.api.tasks.bundling.AbstractArchiveTask).archiveClassifier.set("server")
}

// https://github.com/Fallen-Breath/yamlang
val mainSourceSet = extensions.getByType(SourceSetContainer::class).getByName("main")
val yamlangExtension = extensions.getByName("yamlang")
val targetSourceSetsSetter =
    yamlangExtension.javaClass.methods.firstOrNull {
        it.name == "setTargetSourceSets" && it.parameterCount == 1
    } ?: error("yamlang extension does not expose setTargetSourceSets")
val targetParamType: Class<*> = targetSourceSetsSetter.parameterTypes[0]
val targetSourceSetsValue: Any =
    when {
        targetParamType.isAssignableFrom(Set::class.java) -> setOf(mainSourceSet)
        targetParamType.isAssignableFrom(List::class.java) -> listOf(mainSourceSet)
        targetParamType.isAssignableFrom(Collection::class.java) -> listOf(mainSourceSet)
        targetParamType.isAssignableFrom(Iterable::class.java) -> listOf(mainSourceSet)
        else -> listOf(mainSourceSet)
    }
targetSourceSetsSetter.invoke(yamlangExtension, targetSourceSetsValue)
val inputDirSetter =
    yamlangExtension.javaClass.methods.firstOrNull {
        it.name == "setInputDir" && it.parameterCount == 1
    } ?: error("yamlang extension does not expose setInputDir")
val inputParamType: Class<*> = inputDirSetter.parameterTypes[0]
val inputDirValue: Any = if (inputParamType == File::class.java) file(langDir) else langDir
inputDirSetter.invoke(yamlangExtension, inputDirValue)

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    if (javaCompatibility <= JavaVersion.VERSION_1_8) {
        // suppressed "source/target value 8 is obsolete and will be removed in a future release"
        options.compilerArgs.add("-Xlint:-options")
    }
}

extensions.getByType(JavaPluginExtension::class).apply {
    sourceCompatibility = javaCompatibility
    targetCompatibility = javaCompatibility

    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    inputs.property("archives_base_name", archivesBaseName)
    from(rootProject.file("LICENSE")) {
        rename { name -> "${name}_${inputs.properties["archives_base_name"]}" }
    }
}

extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(
        groupId = group.toString(),
        artifactId = baseExtension.archivesName.get(),
        version = fullArtifactVersion,
    )

    pom {
        name.set(modName)
        description.set(modDescription)
        url.set(modWebsite)
        licenses {
            license {
                name.set("The GNU General Public License v3.0")
                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("Bdbmzwsc")
                name.set("bdbmzwsc")
                email.set("chinabdbmzwsc@outlook.com")
                url.set("https://github.com/Bdbmzwsc")
            }
            developer {
                id.set("jasonxue1")
                name.set("jasonxue")
                email.set("hi@jasonxue.dev")
                url.set("https://github.com/jasonxue1")
            }
            developer {
                id.set("LittleLemonJam")
                name.set("小柠檬lemon酱")
                url.set("https://github.com/LittleLemonJam")
            }
            developer {
                id.set("stupidOIO")
                name.set("stupidOIO")
                url.set("https://github.com/stupidOIO")
            }
        }
        scm {
            url.set(modSource)
            connection.set("scm:git:$modSource.git")

            val sshUrl =
                modSource
                    .removePrefix("https://")
                    .let { "ssh://git@$it.git" }

            developerConnection.set("scm:git:$sshUrl")
        }
        issueManagement {
            system = "Github Issues"
            url = issueTrackerUrl
        }
    }
}
