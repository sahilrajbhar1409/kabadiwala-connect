@echo off
REM ==============================================================================
REM Kabadiwala Connect (SIH 26229) - Person 4 Git Push Script
REM ==============================================================================
echo.
echo ==============================================================================
echo   Pushing Person 4 (Kabadiwala Connect) to GitHub
echo ==============================================================================
echo.

REM Check if git is available
where git >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Git is not installed or not in your system PATH.
    echo Please install Git from https://git-scm.com/download/win and try again.
    pause
    exit /b 1
)

REM Initialize git repository if not already initialized
if not exist ".git" (
    echo [*] Initializing Git repository...
    git init
    git branch -M main
)

REM Add all files
echo [*] Staging all files including Person 4 module...
git add .

REM Commit changes
echo [*] Committing changes...
git commit -m "feat(person4): Implement Kabadiwala Connect informal-to-formal module (SIH 26229)"

REM Check if remote exists
git remote get-url origin >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo No GitHub remote 'origin' found.
    set /p REPO_URL="Enter your GitHub Repository URL (e.g., https://github.com/username/repo.git): "
    if not "%REPO_URL%"=="" (
        git remote add origin %REPO_URL%
    ) else (
        echo [!] No remote URL entered. Skipping push.
        pause
        exit /b 0
    )
)

echo [*] Pushing to GitHub (main branch)...
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Person 4 module successfully pushed to GitHub!
) else (
    echo.
    echo [!] If push failed due to remote conflicts, try:
    echo     git pull origin main --rebase
    echo     git push -u origin main
)

pause
