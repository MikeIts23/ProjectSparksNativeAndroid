@echo off
"C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\java" ^
  --class-path ^
  "C:\\Users\\mikid\\.gradle\\caches\\modules-2\\files-2.1\\com.google.prefab\\cli\\2.0.0\\f2702b5ca13df54e3ca92f29d6b403fb6285d8df\\cli-2.0.0-all.jar" ^
  com.google.prefab.cli.AppKt ^
  --build-system ^
  cmake ^
  --platform ^
  android ^
  --abi ^
  arm64-v8a ^
  --os-version ^
  23 ^
  --stl ^
  c++_shared ^
  --ndk-version ^
  27 ^
  --output ^
  "C:\\Users\\mikid\\AppData\\Local\\Temp\\agp-prefab-staging3049440863576701049\\staged-cli-output" ^
  "C:\\Users\\mikid\\.gradle\\caches\\8.9\\transforms\\fbef5ff47a2323e2ab414107ba2f3380\\transformed\\games-activity-3.0.5\\prefab"
