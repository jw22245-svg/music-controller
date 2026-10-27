# 一键生成 APK 指南

## 方式一：无需电脑安装环境（GitHub Actions 云端自动打包 APK）
1. 在 GitHub 新建一个仓库（Repository），将本压缩包解压后的所有文件上传到仓库中。
2. 点击仓库顶部的 **Actions** 标签页，系统会自动运行 `Build Android APK` 工作流。
3. 等待约 1~2 分钟构建完成后，点击进入该次构建记录，在底部 **Artifacts** 区域即可直接下载编译好的 `MusicWidget-debug-apk`（内含可直接安装到手机的 `app-debug.apk`）。

## 方式二：使用 Android Studio 本地打包
1. 解压本压缩包，打开 Android Studio -> **Open** 选择解压后的文件夹。
2. 等待 Gradle 自动同步完成后，点击顶部菜单 **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**。
3. 编译完成后点击右下角弹窗的 **locate**，即可在 `app/build/outputs/apk/debug/app-debug.apk` 获取安装包。
