# Apk加固工具

## 简要介绍

用于apk加固的demo

## 模块划分

|模块名称|依赖|功能|
|:--:|:-|:-|
|DexShell|NA|dex壳, 用于加载真正的app Dex文件, 运行于Android中|
|ReinforceTool|DexShell|命令行加固工具, 需要使用DexShell的产物: dex壳. 主要功能是将真正apk的dex文件加密并隐藏起来, 还包含对齐和签名|
|AppReinforceTool|ReinforceTool|界面展示工具, 将ReinforceTool模块的命令行加固工具简单封装, 以界面展示|

## 编译

### 工程编译

工程编译使用了gradle工具, 主要编译命令包含如下:

* assembleDex: 编译DexShell模块, 主要生成app Dex文件
* assembleTool: 编译ReinforceTool模块, 主要生成可执行的jar包, ReinforceTool.jar
* assembleApp: 编译AppReinforceTool模块, 生成具有界面可执行的jar包加固工具, 以及脚本, 生成的产物在`工程根目录/Artifacts/ApkReinforceTool`下

### 直接编译出产物

gradle会自动打包依赖模块, 自动收集依赖模块的产物, 并将最终产物打包到`工程根目录/Artifacts/ApkReinforceTool`下

> 配置环境变量`ANDROID_SDK_HOME`

> 在工程根目录下, 执行:

```bash
gradle assembleApp
```

### 利用Exe4j生成window下exe文件

* window上需要安装Exe4j软件, 运行时导入配置文件`AppReinforceTool/tool/buildExe/buildExe.exe4j`打包, 因为个人系统配置不同, 可能需要调整
* 具体使用请查看`AppReinforceTool/tool/buildExe/README.md`

## 运行

***需要安装java***

### 支持java的系统

> 切换到`工程根目录/Artifacts/ApkReinforceTool`下, 运行如下命令: 

`java -jar -splash:icon/app_icon.png libs/ApkReinforceTool.jar`

### Windows系统

> 切换到`工程根目录/Artifacts/ApkReinforceTool`下, 运行`ApkReinforceTool.bat`

### Mac或者Linux系统

> 切换到`工程根目录/Artifacts/ApkReinforceTool`下,  运行`ApkReinforceTool`