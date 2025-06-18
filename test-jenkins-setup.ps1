#!/usr/bin/env pwsh

param(
    [switch]$Build,
    [switch]$Restart,
    [string]$TestSuite = "smoke"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Jenkins Setup Verification" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

# Stop existing containers if restart requested
if ($Restart) {
    Write-Host "Stopping existing Jenkins containers..." -ForegroundColor Yellow
    docker-compose -f docker-compose.simple.yml down
    Start-Sleep -Seconds 5
}

# Build and start Jenkins if requested
if ($Build -or $Restart) {
    Write-Host "Building and starting Jenkins..." -ForegroundColor Blue
    docker-compose -f docker-compose.simple.yml up -d --build
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to start Jenkins" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Waiting for Jenkins to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

# Check Jenkins status
Write-Host "Checking Jenkins status..." -ForegroundColor Blue
$jenkinsStatus = docker ps --filter "name=jenkins-simple" --format "{{.Status}}"

if ($jenkinsStatus -like "*Up*") {
    Write-Host "✅ Jenkins is running" -ForegroundColor Green
} else {
    Write-Host "❌ Jenkins is not running" -ForegroundColor Red
    Write-Host "Jenkins container status: $jenkinsStatus" -ForegroundColor Yellow
    exit 1
}

# Check Docker CLI in Jenkins
Write-Host "Verifying Docker CLI in Jenkins..." -ForegroundColor Blue
$dockerCheck = docker exec jenkins-simple docker --version 2>&1

if ($dockerCheck -like "*Docker version*") {
    Write-Host "✅ Docker CLI is available in Jenkins" -ForegroundColor Green
} else {
    Write-Host "❌ Docker CLI not found in Jenkins" -ForegroundColor Red
    Write-Host "Installing Docker CLI..." -ForegroundColor Yellow
    docker exec -u root jenkins-simple bash -c "apt-get update && apt-get install -y docker.io"
}

# Verify workspace mount
Write-Host "Checking workspace mount..." -ForegroundColor Blue
$workspaceCheck = docker exec jenkins-simple ls -la /workspace/pom.xml 2>&1

if ($workspaceCheck -like "*pom.xml*") {
    Write-Host "✅ Workspace is properly mounted" -ForegroundColor Green
} else {
    Write-Host "❌ Workspace mount issue detected" -ForegroundColor Red
    Write-Host "Workspace check result: $workspaceCheck" -ForegroundColor Yellow
}

# Test Docker build
Write-Host "Testing Docker image build..." -ForegroundColor Blue
$buildTest = docker exec jenkins-simple bash -c "cd /workspace && docker build -f Dockerfile.test -t test-automation:test . 2>&1"

if ($buildTest -like "*Successfully built*" -or $buildTest -like "*Successfully tagged*") {
    Write-Host "✅ Docker image builds successfully" -ForegroundColor Green
} else {
    Write-Host "⚠️ Docker build had issues" -ForegroundColor Yellow
    Write-Host "Build output (last 5 lines):" -ForegroundColor Gray
    Write-Host ($buildTest -split "`n" | Select-Object -Last 5) -ForegroundColor Gray
}

# Test basic Maven command
Write-Host "Testing Maven in container..." -ForegroundColor Blue
$mavenTest = docker exec jenkins-simple bash -c "cd /workspace && docker run --rm -v /workspace:/app test-automation:test mvn --version 2>&1"

if ($mavenTest -like "*Apache Maven*") {
    Write-Host "✅ Maven works in container" -ForegroundColor Green
} else {
    Write-Host "❌ Maven test failed" -ForegroundColor Red
    Write-Host "Maven test result: $mavenTest" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Setup Verification Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Jenkins URL: http://localhost:8080" -ForegroundColor Green
Write-Host "Admin Password: admin123" -ForegroundColor Green
Write-Host "Pipeline Script: Use 'Jenkinsfile.simple'" -ForegroundColor Green
Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "1. Open Jenkins at http://localhost:8080" -ForegroundColor White
Write-Host "2. Create a new Pipeline job" -ForegroundColor White
Write-Host "3. Use 'Pipeline script' and paste content from Jenkinsfile.simple" -ForegroundColor White
Write-Host "4. Run with parameters: Environment=local, Suite=$TestSuite" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan 