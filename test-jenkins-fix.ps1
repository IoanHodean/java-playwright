#!/usr/bin/env pwsh

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Testing Jenkins Fix" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

# Test 1: Verify Jenkins container can see project files
Write-Host "`n1. Testing Jenkins container file access..." -ForegroundColor Blue
try {
    $result = docker exec jenkins-simple bash -c "ls -la /workspace/pom.xml"
    if ($result) {
        Write-Host "✅ Jenkins can access pom.xml in /workspace" -ForegroundColor Green
        Write-Host "   $result" -ForegroundColor Gray
    } else {
        Write-Host "❌ Jenkins cannot access pom.xml" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Error testing Jenkins file access: $_" -ForegroundColor Red
}

# Test 2: Test Docker command from within Jenkins
Write-Host "`n2. Testing Docker execution from Jenkins..." -ForegroundColor Blue
try {
    $result = docker exec jenkins-simple docker run --rm -v /workspace:/app alpine:latest ls -la /app/pom.xml
    if ($result) {
        Write-Host "✅ Jenkins can run Docker containers with volume mounts" -ForegroundColor Green
        Write-Host "   $result" -ForegroundColor Gray
    } else {
        Write-Host "❌ Jenkins cannot run Docker containers properly" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Error testing Docker from Jenkins: $_" -ForegroundColor Red
}

# Test 3: Check if test automation image exists
Write-Host "`n3. Testing test automation image..." -ForegroundColor Blue
try {
    $result = docker exec jenkins-simple docker images test-automation:latest
    if ($result -match "test-automation") {
        Write-Host "✅ Test automation image exists" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Test automation image not found, building it..." -ForegroundColor Yellow
        docker exec jenkins-simple bash -c "cd /workspace && docker build -f Dockerfile.test -t test-automation:latest ."
    }
} catch {
    Write-Host "❌ Error checking test automation image: $_" -ForegroundColor Red
}

# Test 4: Try a simple Maven test run
Write-Host "`n4. Testing Maven execution in container..." -ForegroundColor Blue
try {
    Write-Host "   Running a quick Maven validation..." -ForegroundColor Yellow
    $result = docker exec jenkins-simple docker run --rm -v /workspace:/app test-automation:latest bash -c "cd /app && mvn validate"
    if ($result -match "BUILD SUCCESS") {
        Write-Host "✅ Maven execution works correctly" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Maven execution may have issues" -ForegroundColor Yellow
        Write-Host "   Output: $($result -split "`n" | Select-Object -Last 5)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Error testing Maven execution: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Jenkins Fix Test Completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nNext steps:" -ForegroundColor White
Write-Host "1. Log into Jenkins: http://localhost:8080" -ForegroundColor Gray
Write-Host "2. Run the QA-Test-Automation-Pipeline" -ForegroundColor Gray
Write-Host "3. Select 'smoke' test suite" -ForegroundColor Gray
Write-Host "4. Check if pom.xml is now found" -ForegroundColor Gray 