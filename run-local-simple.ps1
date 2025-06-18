param(
    [string]$TestSuite = "all",
    [string]$Environment = "local", 
    [string]$Browser = "chromium",
    [switch]$Build
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Docker Test Runner (Local)" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "Test Suite: $TestSuite" -ForegroundColor Yellow
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Browser: $Browser" -ForegroundColor Yellow
Write-Host ""

# Build image if requested or if it doesn't exist
$imageExists = docker images test-automation-local -q
if ($Build -or !$imageExists) {
    Write-Host "Building test automation Docker image..." -ForegroundColor Blue
    docker build -f Dockerfile.test -t test-automation-local .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to build Docker image" -ForegroundColor Red
        exit 1
    }
    Write-Host "Docker image built successfully" -ForegroundColor Green
}

# Run tests
Write-Host "Running tests in Docker container..." -ForegroundColor Blue

$testCommand = switch ($TestSuite.ToLower()) {
    "api" { "mvn test -Dtest=*ApiTest" }
    "ui" { "mvn test -Dtest=*UI*Test -Dbrowser=$Browser -Dheadless=true" }
    "performance" { "mvn test -Dtest=*PerformanceTest" }
    default { "mvn test -Dbrowser=$Browser -Dheadless=true" }
}

docker run --rm `
    -v ${PWD}:/app `
    -v ${PWD}/target:/app/target `
    -v ${PWD}/allure-results:/app/allure-results `
    -v ${PWD}/logs:/app/logs `
    -v ${PWD}/screenshots:/app/screenshots `
    -v ${PWD}/test-results:/app/test-results `
    -e ENVIRONMENT=$Environment `
    -e BROWSER=$Browser `
    -e HEADLESS=true `
    --shm-size=2g `
    test-automation-local `
    /bin/bash -c "$testCommand"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Tests completed successfully!" -ForegroundColor Green
    Write-Host "Generate reports with: docker run --rm -v `"$PWD`":/app test-automation-local mvn allure:serve" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "Tests failed!" -ForegroundColor Red
}

Write-Host ""
Write-Host "Usage examples:" -ForegroundColor Cyan
Write-Host "  .\run-local-simple.ps1                          # Run all tests" -ForegroundColor Gray
Write-Host "  .\run-local-simple.ps1 -TestSuite api           # Run API tests only" -ForegroundColor Gray
Write-Host "  .\run-local-simple.ps1 -TestSuite ui -Browser firefox  # Run UI tests with Firefox" -ForegroundColor Gray
Write-Host "  .\run-local-simple.ps1 -Build                   # Rebuild image and run tests" -ForegroundColor Gray 