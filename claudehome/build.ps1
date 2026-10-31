$ErrorActionPreference='Stop'
$env:JAVA_HOME='C:\Android\jdk\jdk-17.0.21.0.2+1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:JAVA_TOOL_OPTIONS=''
$SDK='C:\Android\sdk'; $BT="$SDK\build-tools\34.0.0"; $JAR="$SDK\platforms\android-30\android.jar"
$P='C:\Android\claudehome'; $B="$P\build"
if (Test-Path $B) { Remove-Item $B -Recurse -Force }; New-Item -ItemType Directory -Force "$B\gen","$B\classes","$B\res" | Out-Null
& "$BT\aapt2.exe" compile --dir "$P\res" -o "$B\res.zip"; if ($LASTEXITCODE) { throw "aapt2 compile failed" }
& "$BT\aapt2.exe" link -o "$B\base.apk" -I $JAR --manifest "$P\AndroidManifest.xml" --java "$B\gen" "$B\res.zip" --auto-add-overlay; if ($LASTEXITCODE) { throw "aapt2 link failed" }
$srcs = Get-ChildItem "$P\src","$B\gen" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& javac --release 8 -encoding UTF-8 -cp $JAR -Xlint:-options -d "$B\classes" $srcs; if ($LASTEXITCODE) { throw "javac failed" }
$cls = Get-ChildItem "$B\classes" -Recurse -Filter *.class | ForEach-Object { $_.FullName }
& "$BT\d8.bat" --min-api 25 --lib $JAR --output "$B" $cls; if ($LASTEXITCODE) { throw "d8 failed" }
Copy-Item "$B\base.apk" "$B\unsigned.zip"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[IO.Compression.ZipFile]::Open("$B\unsigned.zip",'Update'); $e=$z.CreateEntry('classes.dex',[IO.Compression.CompressionLevel]::Optimal)
$s=$e.Open(); $f=[IO.File]::OpenRead("$B\classes.dex"); $f.CopyTo($s); $s.Close(); $f.Close(); $z.Dispose()
Move-Item "$B\unsigned.zip" "$B\unsigned.apk" -Force
& "$BT\zipalign.exe" -f 4 "$B\unsigned.apk" "$B\aligned.apk"; if ($LASTEXITCODE) { throw "zipalign failed" }
$KS='C:\Android\claudehome.keystore'
if (!(Test-Path $KS)) { & keytool -genkeypair -keystore $KS -storepass claudehome -keypass claudehome -alias claudehome -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Claude Home" | Out-Null }
& "$BT\apksigner.bat" sign --ks $KS --ks-pass pass:claudehome --key-pass pass:claudehome --out "$P\ClaudeHome.apk" "$B\aligned.apk"; if ($LASTEXITCODE) { throw "apksigner failed" }
"BUILD OK: $P\ClaudeHome.apk $((Get-Item "$P\ClaudeHome.apk").Length/1KB) KB"
