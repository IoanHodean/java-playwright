#!/usr/bin/env pwsh

param(
    [switch]$Build,
    [switch]$Debug,
    [string]$TestType = "smoke"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Docker Container Debug Script" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

# Function to check Docker setup
function Test-DockerSetup {
    Write-Host "Checking Docker setup..." -ForegroundColor Blue
    
    # Check if Docker is running
    try {
        docker version | Out-Null
        Write-Host "✅ Docker is running" -ForegroundColor Green
    } catch {
        Write-Host "❌ Docker is not running or not accessible" -ForegroundColor Red
        exit 1
    }
    
    # Check current directory structure
    Write-Host "`nCurrent directory structure:" -ForegroundColor Yellow
    Get-ChildItem -Path . | Format-Table Name, Mode, Length -AutoSize
    
    # Check if pom.xml exists
    if (Test-Path "pom.xml") {
        Write-Host "✅ pom.xml found in current directory" -ForegroundColor Green
    } else {
        Write-Host "❌ pom.xml NOT found in current directory" -ForegroundColor Red
        Write-Host "Current location: $(Get-Location)" -ForegroundColor Yellow
    }
}

# Function to build test image
function Build-TestImage {
    Write-Host "`nBuilding test automation Docker image..." -ForegroundColor Blue
    
    try {
        docker build -f Dockerfile.test -t test-automation:debug .
        Write-Host "✅ Docker image built successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to build Docker image" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        exit 1
    }
}

# Function to debug container
function Debug-Container {
    Write-Host "`nDebugging Docker container..." -ForegroundColor Blue
    
    $currentPath = Get-Location
    Write-Host "Host working directory: $currentPath" -ForegroundColor Yellow
    
    Write-Host "`n--- Container File System Debug ---" -ForegroundColor Magenta
    docker run --rm -it `
        -v "${currentPath}:/app" `
        test-automation:debug `
        bash -c "
            echo '=== Container Debug Information ==='
            echo 'Working directory:' && pwd
            echo 'Files in /app:' && ls -la /app
            echo 'Looking for pom.xml:' && find /app -name 'pom.xml' -type f
            echo 'Maven version:' && mvn --version
            echo 'Java version:' && java --version
            echo 'Environment variables:' && env | grep -E '(PATH|JAVA|MAVEN)'
        "
}

# Function to test Maven execution
function Test-MavenExecution {
    Write-Host "`nTesting Maven execution in container..." -ForegroundColor Blue
    
    $currentPath = Get-Location
    
    Write-Host "`n--- Maven Test Execution ---" -ForegroundColor Magenta
    docker run --rm `
        -v "${currentPath}:/app" `
        -v "${currentPath}/target:/app/target" `
        -e ENVIRONMENT=local `
        -e BROWSER=chromium `
        -e HEADLESS=true `
        test-automation:debug `
        bash -c "
            echo '=== Maven Test Execution ==='
            cd /app
            echo 'Current directory:' && pwd
            echo 'Files in current directory:' && ls -la
            if [ -f pom.xml ]; then
                echo 'Found pom.xml, attempting to run tests...'
                mvn clean test -Dtest=*${TestType}*Test -Denvironment=local -Dbrowser=chromium -Dheadless=true -Dmaven.test.failure.ignore=true
            else
                echo 'ERROR: pom.xml not found!'
                exit 1
            fi
        "
}

# Main execution
Write-Host "Starting Docker debug process..." -ForegroundColor Green

Test-DockerSetup

if ($Build) {
    Build-TestImage
}

if ($Debug) {
    if (-not (docker images test-automation:debug -q)) {
        Write-Host "Test image not found, building it first..." -ForegroundColor Yellow
        Build-TestImage
    }
    
    Debug-Container
    
    Write-Host "`nDo you want to test Maven execution? (y/N)" -ForegroundColor Yellow
    $response = Read-Host
    if ($response -eq 'y' -or $response -eq 'Y') {
        Test-MavenExecution
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Debug process completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nUsage examples:" -ForegroundColor White
Write-Host "  .\debug-docker.ps1 -Build                    # Build the test image" -ForegroundColor Gray
Write-Host "  .\debug-docker.ps1 -Debug                    # Debug container setup" -ForegroundColor Gray
Write-Host "  .\debug-docker.ps1 -Build -Debug             # Build and debug" -ForegroundColor Gray
Write-Host "  .\debug-docker.ps1 -Debug -TestType api      # Debug with API tests" -ForegroundColor Gray 