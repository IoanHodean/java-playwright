# Test Docker Copy Approach for Jenkins Pipeline
# This script tests the exact approach used in the updated Jenkinsfile

Write-Host "=== Testing Docker Copy Approach ===" -ForegroundColor Green

# Set variables (similar to Jenkins)
$DOCKER_IMAGE = "test-automation"
$DOCKER_TAG = "latest"
$BUILD_NUMBER = "test"
$TEST_ENVIRONMENT = "local"
$BROWSER = "chromium"
$HEADLESS = "true"

Write-Host "Building test image..." -ForegroundColor Yellow
docker build -f Dockerfile.test -t "${DOCKER_IMAGE}:${DOCKER_TAG}" .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker image built successfully" -ForegroundColor Green

Write-Host "Testing Docker copy approach..." -ForegroundColor Yellow

try {
    # Create a temporary container (exactly like in Jenkinsfile)
    Write-Host "Creating container..." -ForegroundColor Cyan
    $CONTAINER_ID = docker create --name test-smoke-$BUILD_NUMBER `
        -e ENVIRONMENT=$TEST_ENVIRONMENT `
        -e BROWSER=$BROWSER `
        -e HEADLESS=$HEADLESS `
        --shm-size=2g `
        "${DOCKER_IMAGE}:${DOCKER_TAG}"
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create container"
    }
    
    Write-Host "✅ Container created: $CONTAINER_ID" -ForegroundColor Green
    
    # Copy project files to container
    Write-Host "Copying files to container..." -ForegroundColor Cyan
    docker cp . "${CONTAINER_ID}:/app/"
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to copy files to container"
    }
    
    Write-Host "✅ Files copied successfully" -ForegroundColor Green
    
    # Start container and run a simple validation
    Write-Host "Starting container and validating setup..." -ForegroundColor Cyan
    docker start $CONTAINER_ID
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start container"
    }
    
    Write-Host "✅ Container started" -ForegroundColor Green
    
    # Test Maven validation (not full test run)
    Write-Host "Testing Maven validation..." -ForegroundColor Cyan
    docker exec $CONTAINER_ID bash -c "cd /app && pwd && ls -la pom.xml && mvn validate"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Maven validation successful!" -ForegroundColor Green
        $testResult = "PASSED"
    } else {
        Write-Host "❌ Maven validation failed!" -ForegroundColor Red
        $testResult = "FAILED"
    }
    
    # Cleanup
    Write-Host "Cleaning up container..." -ForegroundColor Cyan
    docker stop $CONTAINER_ID
    docker rm $CONTAINER_ID
    
    Write-Host "✅ Cleanup completed" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    # Cleanup on error
    docker stop $CONTAINER_ID -ErrorAction SilentlyContinue
    docker rm $CONTAINER_ID -ErrorAction SilentlyContinue
    $testResult = "ERROR"
}

Write-Host "`n=== Test Results ===" -ForegroundColor Yellow
Write-Host "Docker Copy Approach: $testResult" -ForegroundColor $(if ($testResult -eq "PASSED") { "Green" } else { "Red" })

if ($testResult -eq "PASSED") {
    Write-Host "`n🎉 The Docker copy approach is working correctly!" -ForegroundColor Green
    Write-Host "You can now run the Jenkins pipeline with confidence." -ForegroundColor Green
    Write-Host "Navigate to: http://localhost:8080" -ForegroundColor Cyan
} else {
    Write-Host "`n❌ There are still issues with the Docker copy approach." -ForegroundColor Red
    Write-Host "Please check the errors above before running the Jenkins pipeline." -ForegroundColor Red
} 