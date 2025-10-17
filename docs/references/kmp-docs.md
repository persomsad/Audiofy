Kotlin Multiplatform 入门：构建你的第一个跨平台应用
引言
本文面向对 Kotlin Multiplatform (KMP) 感兴趣的开发者，特别是那些希望探索如何使用单一代码库开发跨平台应用的程序员。无论您是移动开发者、前端工程师还是全栈开发者，只要您对提高开发效率和跨平台开发感兴趣，了解一下并体验一下 KMP 我想都是值得的。

在本教程中，相对新手向。我们将详细介绍以下内容:

环境准备

项目初始化

gradle 命令

启动桌面应用

配置和启动 Android 应用

配置和启动 iOS 应用

启动 Web 应用

设置和启动服务器应用

环境准备
在开始我们的 KMP 之旅之前，我们需要先搭建好开发环境。

首先我们需要安装 Android Studio / Xcode / cocoapods。有了这些基础环境我们便可以体验 kmp 所有平台的开发。

对于 Windows、Linux 用户，由于系统限制可以跳过 Xcode / cocoapods 的安装，也因此只能体验桌面、Android 和 Web 平台的开发。

Android Studio: 这是我们的主要集成开发环境(IDE)。Android Studio 不仅支持 Android 开发，还完美集成了 Kotlin 和 KMP 的支持。

下载地址: https://developer.android.com/studio

Xcode (仅限 Mac 用户): 用于 iOS 应用开发和测试。即使你主要使用 Android Studio，Xcode 也是 iOS 开发不可或缺的工具。

下载地址: https://developer.apple.com/xcode/

CocoaPods (仅限Mac用户): iOS 开发中常用的依赖管理工具。

安装命令: brew install cocoapods

注意: Windows 和 Linux 用户可以跳过 Xcode 和 CocoaPods 的安装。虽然这意味着你暂时无法开发 iOS 应用，但你仍然可以充分体验 KMP 在桌面、Android 和 Web 平台的强大功能。

对于 Mac 用户来说，也可以安装 kdoctor 工具，来进行环境检查

brew install kdoctor
kdoctor

Environment diagnose (to see all details, use -v option):
[✓] Operation System
[✓] Java
[!] Android Studio
  ! Android Studio (AI-242.23339.11.2421.12483815)
    Bundled Java: openjdk 21.0.3 2024-04-16
    Kotlin Plugin: 242.23339.11.2421.12483815-AS
    Kotlin Multiplatform Mobile Plugin: not installed
    Install Kotlin Multiplatform Mobile plugin - https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile
  ! Android Studio (AI-242.21829.142.2421.12366423)
    Bundled Java: openjdk 21.0.3 2024-04-16
    Kotlin Plugin: 242.21829.142.2421.12366423-AS
    Kotlin Multiplatform Mobile Plugin: not installed
    Install Kotlin Multiplatform Mobile plugin - https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile
[✓] Xcode
[✓] CocoaPods

Conclusion:
  ✓ Your operation system is ready for Kotlin Multiplatform Mobile Development!
kdoctor 会详细列出你的环境状况，包括操作系统、Java版本、Android Studio配置、Xcode和CocoaPods 等。如果有任何问题，它会给出明确的提示和修复建议。

使用 JetBrains KMP 脚手架初始化项目
JetBrains 提供了一个非常方便的在线工具 - Kotlin Multiplatform Wizard，它可以帮助我们快速生成一个包含所有必要配置的 KMP 项目模板。

让我们一步步来:

打开 Kotlin Multiplatform wizard

在 wizard 界面上，你会看到多个选项，允许你选择要支持的平台。为了充分展示 KMP 的强大功能，我们这里选择所有可用的平台。你可以根据自己的项目需求进行选择。

图片

配置完成后，点击下载按钮。这将生成一个包含所有必要文件和配置的 zip 文件。

下载完成后，解压这个 zip 文件到你的工作目录。

使用 Android Studio 打开这个项目目录。

当你首次打开项目时，Gradle 会开始同步和下载必要的依赖。这个过程可能需要一些时间，取决于你的网速。

如果遇到网络问题导致同步失败，可以考虑添加国内的 Maven 镜像或者配置 Gradle 代理。



图片

图片

现在，让我们来看一下项目的结构，特别是 composeApp 模块，这是我们多平台应用的核心:

composeApp
├── build.gradle.kts
└── src
    ├── androidMain
    ├── commonMain
    ├── desktopMain
    ├── iosMain
    └── wasmJsMain
你会注意到，src 目录下有多个子目录，每个都对应一个特定的平台:

commonMain: 这里存放所有平台共享的代码。这是KMP的精髓所在，你可以在这里编写一次代码，然后在所有平台上运行。

androidMain: Android 特定的代码。

desktopMain: 桌面应用 (如 Windows， macOS， Linux) 特定的代码。

iosMain: iOS 特定的代码。

wasmJsMain: Web 应用特定的代码。

除了 composeApp 模块，你还会看到一个 shared 模块，它的结构与 composeApp 类似。shared 模块通常用于存放可以在多个模块间共享的代码，而 composeApp 依赖于shared。这种结构允许你进一步模块化你的应用，提高代码的可维护性和复用性。

在 composeApp 的 build.gradle.kts 文件中，你会看到类似这样的依赖声明:

commonMain.dependencies {
    implementation(projects.shared)
}
这行代码表示 composeApp 模块依赖于 shared 模块。这种设置允许你在 shared 模块中定义共享的数据模型、业务逻辑或工具类，然后在 composeApp 中使用它们。

理解这个项目结构对于充分利用KMP的优势至关重要。它允许你最大化代码复用，同时仍然为每个平台提供定制化的实现。

项目 gradle 命令
Gradle 是 KMP 项目的构建工具，它提供了许多有用的命令来帮助我们构建、测试、运行和发布多平台应用。首先，让我们看一下项目根目录下的 build.gradle.kts 文件:

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}
这些插件为我们的 KMP 项目提供了必要的功能支持，如Android应用开发、Jetpack Compose UI框架、Kotlin 多平台支持等等。

我们可以通过 gradle tasks 查看这些插件提供的所有命令

输出如下

Android tasks
-------------
androidDependencies - Displays the Android dependencies of the project.
signingReport - Displays the signing info for the base and test modules
sourceSets - Prints out all the source sets defined in this project.

Application tasks
-----------------
run - Runs this project as a JVM application
runShadow - Runs this project as a JVM application using the shadow jar
startShadowScripts - Creates OS specific scripts to run the project as a JVM application using the shadow jar

... 输出过长忽略
这些任务涵盖了从构建到测试，再到运行和部署的各个方面，当我们需要某些功能时，便可以基于此按需查找。

虽然我们可以在命令行中运行这些 Gradle 任务，但我个人更推荐使用 IDE 来执行这些任务。使用IDE有以下优势

更好的项目隔离: IDE可以为每个项目维护独立的配置，避免全局设置可能带来的冲突。

更直观的界面: IDE提供图形化界面，使任务执行更加直观和方便。

更好的调试支持: 当遇到问题时，IDE提供更详细的日志和调试工具。

图片

启动桌面应用
让我们来启动我们的第一个平台应用 - 桌面应用。这将展示 KMP 如何无缝地将我们的代码转换为可在 Windows、macOS 和 Linux 上运行的应用程序。

你可能会尝试直接通过 IDE 运行主函数来启动应用，实际上这并不能成功执行。这是因为 KMP 应用需要在运行前进行大量的预处理工作，而 IDE 本身并没有预置这些功能。

图片

正确的做法还是需要使用上文提到的 gradle 命令。我们可以轻松的查找到相关命令 run - Runs this project as a JVM application

gradle run
执行这个命令后，Gradle 会编译我们的代码，处理所有必要的依赖，然后启动应用。几秒钟后，你应该能看到一个窗口弹出，显示我们的应用界面:

图片

恭喜！你刚刚成功运行了你的第一个 KMP 桌面应用！

虽然命令行运行很方便，但如果我们能直接在 IDE 中点击运行按钮岂不是更好？让我们来保存刚刚执行的运行配置，以便后续可以选择任务点击直接执行。

图片

有了这个配置，你就可以像运行普通 Kotlin / Java 应用一样轻松地运行和调试你的 KMP 桌面应用了。

配置和启动 Android 应用
接下来，让我们将注意力转向移动平台，从 Android 开始。KMP 允许我们重用大部分代码，同时为 Android 平台提供特定的实现。

在运行 Android 应用之前，我们需要确保已经安装了正确的Android SDK 和工具。

在 Android Studio 中，转到 Settings/Preferences -> Languages & Frameworks -> Android SDK

在这里，你可以看到已安装的SDK版本和工具。确保你至少安装了一个较新的 Android SDK 版本。

如果需要，SDK Tools 标签页安装额外的工具，如 Android Emulator 和 SDK Platform-Tools。

图片

KMP 项目初始化后，Android Studio 会自动为 composeApp 模块创建 Android 运行配置。你可以在IDE顶部的运行配置下拉菜单中找到它：

图片

你可以使用真机或者虚拟机启动 Android 应用。

如果选择虚拟机，你可以在 Device Manager 中创建一个图片

如果使用真机，你需要在 Android 设备上启用开发者选项和 USB 调试。

转到设置 -> 关于手机，连续点击 "版本号" 7 次以启用开发者选项。

返回到设置主页，你应该能看到 "开发者选项"

在开发者选项中启用 "USB调试"

更多细节你可以查看官方文档

运行效果

图片

配置和启动 iOS 应用
注意：iOS 开发需要 Mac 电脑。如果你使用的是 Windows 或Linux，可以跳过这一部分。

现在让我们转向 Apple 的生态系统，看看如何在 iOS 设备上运行我们的 KMP 应用。

打开 iosApp 启动配置

图片

在 Execution target 下拉菜单中，你可以选择要运行的iOS 设备或模拟器。如果你连接了真实的 iOS 设备，它也会出现在这个列表中。

使用真机启动应用，同样需要开启开发者模式。这里以 iPhone 为例，设置 -> 隐式与安全性 -> 开发者模式，可以找到对应开关（系统版本不同可能设置路径略有不同）。

配置开发者账号
首次在真机上运行 iOS 应用时，你可能会遇到以下错误：

error: Signing for "iosApp" requires a development team. Select a development team in the Signing & Capabilities editor. (in target 'iosApp' from project 'iosApp')
warning: Run script build phase 'Compile Kotlin Framework' will be run during every build because it does not specify any outputs. To address this issue, either add output dependencies to the script phase, or configure it to run in every build by unchecking "Based on dependency analysis" in the script phase. (in target 'iosApp' from project 'iosApp')
** BUILD FAILED **
这是因为 iOS 应用需要有开发者签名。让我们来配置它：

打开 Xcode

选择 Open Existing Project，然后导航到你的 KMP 项目目录

在 iosApp 文件夹中找到并打开 iosApp.xcodeproj 文件

在 Xcode 中，选择左侧的项目导航器中的 iosApp

在主编辑区域，选择 Signing & Capabilities 标签

在 Team 下拉菜单中，选择你的开发者账号。如果你还没有Apple 开发者账号，需要先在 developer.apple.com 注册一个

图片

设置完成团队 ID 后，我们可以重新启动应用。图片

你现在已经成功地将同一个 KMP 应用部署到了 Desktop / Android / iOS 平台。

启动 Web 应用
KMP 的魅力不仅限于移动和桌面平台，它还能帮助我们构建 Web 应用。Web 应用的优势在于可以用更低的代价触达用户，让用户体验应用的初步功能，吸引用户下载客户端。

在这一部分，我们将看到如何将我们的应用部署为一个 Web 应用。

运行 Web 应用的过程与桌面应用类似，我们将使用 Gradle 命令：

gradle wasmJsBrowserRun
这个命令会做几件事：

编译你的 Kotlin 代码为 WebAssembly (Wasm)

设置一个本地开发服务器

在默认浏览器中打开你的应用

第一次执行命令，你可能会查看到执行卡在： > Task :kotlinNpmInstall

这个任务可能需要一些时间，因为它在下载必要的依赖。耐心等待，完成后你应该能在浏览器中看到你的应用：

图片

Web应用的优势
将你的 KMP 应用部署为 Web 应用有几个显著的优势：

广泛的可访问性：用户无需安装任何东西就能使用你的应用。

即时更新：你可以随时更新应用，用户总是使用最新版本。

跨平台兼容性：Web应用可以在任何有现代浏览器的设备上运行。

通过 KMP，你可以在保持核心业务逻辑一致的同时，为 Web 平台提供定制的 UI 和功能。这大大提高了开发效率，同时确保了各平台间的一致性。

运行服务器应用
最后，让我们来看看如何运行 KMP 项目中的服务器应用。服务器端的 Kotlin 应用通常使用 Ktor 框架，这是一个强大而灵活的Kotlin Web 框架。

在KMP项目中，server 模块通常是一个标准的 Kotlin JVM 应用。它的主要优势在于可以与客户端应用共享代码，特别是数据模型和业务逻辑。

打开 server 模块的 build.gradle.kts 文件，你可能会看到类似这样的依赖：

dependencies {
    implementation(projects.shared)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    // ... 其他依赖
}
注意 implementation(projects.shared) 这一行，它表示服务器模块依赖于 shared 模块，这就是我们实现代码共享的方式。

在 KMP 项目中包含服务器模块有几个主要优势：

代码共享：服务器和客户端可以共享数据模型、验证逻辑等。

类型安全：由于使用相同的语言和模型，可以在编译时捕获许多潜在错误。

开发效率：开发者不需要重复定义，避免不一致的问题，方便全栈开发。

结语
在这篇教程中，我们从环境搭建开始，一步步完成了 KMP 项目的创建和多平台运行。通过实践我们看到，KMP 不仅能够显著提升开发效率，更重要的是它让跨平台开发变得优雅而自然。如果你想看到 KMP 在实际项目中的应用，可以参考我的开源项目 https://github.com/CrossPaste/crosspaste-desktop。这是一个基于Compose Multiplatform 开发的统一桌面剪贴板工具，支持Windows、macOS 和 Linux 平台。不止可以作为单机的粘贴板管理工具，更重要的是它支持跨设备的复制粘贴，在任意设备间复制粘贴，就像在同一台设备上操作一样自然流畅。

