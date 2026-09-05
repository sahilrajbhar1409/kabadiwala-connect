# ==============================================================================
# Kabadiwala Connect (SIH 26229) - Person 4 PowerShell Push Script
# ==============================================================================

param(
    [string]$RepoUrl = ""
)

Write-Host "`n==============================================================================" -ForegroundColor Cyan
Write-Host "   Pushing Person 4 (Kabadiwala Connect) to GitHub" -ForegroundColor Green
Write-Host "==============================================================================`n" -ForegroundColor Cyan

# Check if git command exists
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "[ERROR] Git is not installed or not in your system PATH." -ForegroundColor Red
    Write-Host "Please download Git from https://git-scm.com/download/win and retry." -ForegroundColor Yellow
    exit 1
}

# Initialize git if not present
if (-not (Test-Path ".git")) {
    Write-Host "[*] Initializing Git repository..." -ForegroundColor Yellow
    git init
    git branch -M main
}

# Stage all files
Write-Host "[*] Staging all files including Person 4 module..." -ForegroundColor Yellow
git add .

# Commit
Write-Host "[*] Committing changes..." -ForegroundColor Yellow
git commit -m "feat(person4): Implement Kabadiwala Connect informal-to-formal module (SIH 26229)"

# Configure Remote if needed
$remoteUrl = git remote get-url origin 2>$null
if (-not $remoteUrl) {
    if (-not $RepoUrl) {
        $RepoUrl = Read-Host "Enter your GitHub Repository URL (e.g. https://github.com/username/repo.git)"
    }
    if ($RepoUrl) {
        git remote add origin $RepoUrl
    } else {
        Write-Host "[!] No remote URL entered. Repository committed locally on branch 'main'." -ForegroundColor Yellow
        exit 0
    }
}

# Push
Write-Host "[*] Pushing to GitHub (main branch)..." -ForegroundColor Yellow
git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[SUCCESS] Person 4 module successfully pushed to GitHub!" -ForegroundColor Green
} else {
    Write-Host "`n[!] Push encountered an issue. If your remote repo already has commits, run:" -ForegroundColor Yellow
    Write-Host "    git pull origin main --rebase" -ForegroundColor White
    Write-Host "    git push -u origin main" -ForegroundColor White
}
