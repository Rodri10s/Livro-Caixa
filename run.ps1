$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $projectRoot 'build\LivroCaixa.jar'
if (-not (Test-Path -LiteralPath $jar)) { & (Join-Path $projectRoot 'build.ps1') }
& java -jar $jar
