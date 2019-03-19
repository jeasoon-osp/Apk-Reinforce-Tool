@echo off

set DIRNAME=%~dp0

mshta vbscript:createobject("wscript.shell").run("java -jar -splash:%DIRNAME%/icon/app_icon.png  %DIRNAME%/libs/ApkReinforceTool.jar %*",0)(window.close) && exit
