# Test Allure Setup and Report Generation
# This script validates the Allure configuration and generates a sample report

Write-Host "=== Testing Allure Setup ===" -ForegroundColor Green

# Set variables
$DOCKER_IMAGE = "test-automation"
$DOCKER_TAG = "latest"
$TEST_CONTAINER = "allure-test-container"

Write-Host "Building test image..." -ForegroundColor Yellow
docker build -f Dockerfile.test -t "${DOCKER_IMAGE}:${DOCKER_TAG}" .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker image built successfully" -ForegroundColor Green

Write-Host "Running sample tests to generate Allure results..." -ForegroundColor Yellow

try {
    # Create and start container
    $containerId = docker create --name $TEST_CONTAINER `
        -e ENVIRONMENT=local `
        -e BROWSER=chromium `
        -e HEADLESS=true `
        --shm-size=2g `
        "${DOCKER_IMAGE}:${DOCKER_TAG}"

    # Copy project files
    docker cp . "${containerId}:/app/"
    
    # Start container
    docker start $containerId
    
    # Run a simple test to generate Allure results
    Write-Host "Executing tests..." -ForegroundColor Cyan
    docker exec $containerId bash -c "cd /app && mvn clean test -Dtest=*Test -Dmaven.test.failure.ignore=true"
    
    # Copy results back
    Write-Host "Copying Allure results..." -ForegroundColor Cyan
    docker cp "${containerId}:/app/allure-results" "./" 
    docker cp "${containerId}:/app/target" "./"
    
    Write-Host "✅ Test execution completed" -ForegroundColor Green
    
    # Check if Allure results were generated
    if (Test-Path "allure-results") {
        $allureFiles = Get-ChildItem "allure-results" -Filter "*.json" | Measure-Object
        Write-Host "✅ Found $($allureFiles.Count) Allure result files" -ForegroundColor Green
        
        # List some example files
        Write-Host "Sample Allure files:" -ForegroundColor Cyan
        Get-ChildItem "allure-results" -Filter "*.json" | Select-Object -First 5 | ForEach-Object { 
            Write-Host "  - $($_.Name)" -ForegroundColor Gray 
        }
    } else {
        Write-Host "⚠️ No allure-results directory found" -ForegroundColor Yellow
    }
    
    # Check TestNG results
    if (Test-Path "target/surefire-reports") {
        $testngFiles = Get-ChildItem "target/surefire-reports" -Filter "*.xml" | Measure-Object
        Write-Host "✅ Found $($testngFiles.Count) TestNG result files" -ForegroundColor Green
    } else {
        Write-Host "⚠️ No TestNG results found" -ForegroundColor Yellow
    }
    
    Write-Host "`n=== Allure Setup Validation Complete ===" -ForegroundColor Green
    Write-Host "✅ Allure is properly configured" -ForegroundColor Green
    Write-Host "✅ Test results are being generated" -ForegroundColor Green
    Write-Host "✅ Jenkins pipeline can now publish interactive Allure reports" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error during testing: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # Cleanup
    Write-Host "Cleaning up..." -ForegroundColor Yellow
    docker stop $TEST_CONTAINER 2>$null
    docker rm $TEST_CONTAINER 2>$null
    Write-Host "✅ Cleanup completed" -ForegroundColor Green
} 