# sync-repo.ps1 - Synchronizes files into Chaos-Toolkit-Repo for GitHub repository
$sourceRoot = "C:\Users\hp\OneDrive\Desktop"
$repoRoot = "C:\Users\hp\OneDrive\Desktop\Chaos-Toolkit-Repo"

Write-Host "Creating clean repository directory at $repoRoot..."
if (Test-Path $repoRoot) {
    Remove-Item -Path $repoRoot -Recurse -Force -ErrorAction SilentlyContinue
}

New-Item -ItemType Directory -Path $repoRoot -Force | Out-Null
New-Item -ItemType Directory -Path "$repoRoot\chaos-agent" -Force | Out-Null
New-Item -ItemType Directory -Path "$repoRoot\chaos-toolkit" -Force | Out-Null
New-Item -ItemType Directory -Path "$repoRoot\docs" -Force | Out-Null

Write-Host "Copying chaos-agent..."
# Copy chaos-agent excluding target, .idea, .git
robocopy "$sourceRoot\chaos-agent" "$repoRoot\chaos-agent" /E /XD target .idea .git /XF *.iml /NFL /NDL /NJH /NJS

Write-Host "Copying chaos-toolkit..."
# Copy chaos-toolkit excluding target, logs, .idea, .git
robocopy "$sourceRoot\chaos-toolkit" "$repoRoot\chaos-toolkit" /E /XD target logs .idea .git /XF *.iml /NFL /NDL /NJH /NJS

Write-Host "Copying docs..."
# Copy chaos-toolkit-docs into docs/
robocopy "$sourceRoot\chaos-toolkit-docs" "$repoRoot\docs" /E /XD .idea .git /NFL /NDL /NJH /NJS

Write-Host "Copying root scripts..."
Copy-Item "$sourceRoot\chaos-toolkit\start-all.bat" "$repoRoot\start-all.bat" -Force
Copy-Item "$sourceRoot\chaos-toolkit\start-all.ps1" "$repoRoot\start-all.ps1" -Force
Copy-Item "$sourceRoot\chaos-toolkit\stop-all.bat" "$repoRoot\stop-all.bat" -Force
Copy-Item "$sourceRoot\chaos-toolkit\stop-all.ps1" "$repoRoot\stop-all.ps1" -Force
Copy-Item "$sourceRoot\chaos-toolkit\test-e2e.ps1" "$repoRoot\test-e2e.ps1" -Force

Write-Host "Sync completed successfully!"
