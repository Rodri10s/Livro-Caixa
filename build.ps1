$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceRoot = Join-Path $projectRoot 'src'
$buildRoot = Join-Path $projectRoot 'build'
$classRoot = Join-Path $buildRoot 'classes'
New-Item -ItemType Directory -Force -Path $classRoot | Out-Null
$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter '*.java' | ForEach-Object FullName
if (-not $sources) { throw 'Nenhum arquivo Java encontrado.' }
& javac -encoding UTF-8 -d $classRoot $sources
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilação.' }
$manifest = Join-Path $buildRoot 'MANIFEST.MF'
Set-Content -LiteralPath $manifest -Encoding ascii -Value "Main-Class: livrocaixa.LivroCaixaApp`r`n"
& jar --create --file (Join-Path $buildRoot 'LivroCaixa.jar') --manifest $manifest -C $classRoot .
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o JAR.' }
Write-Host "Compilado: $buildRoot\LivroCaixa.jar"
