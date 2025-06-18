param(
    [switch]$Build,
    [switch]$Stop,
    [switch]$Status
)

$compose_file = "docker-compose.dind.yml"

if ($Status) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "   Jenkins Docker-in-Docker Status" -ForegroundColor Cyan  
    Write-Host "========================================" -ForegroundColor Cyan
    
    docker-compose -f $compose_file ps
    
    Write-Host ""
    Write-Host "Jenkins URL: http://localhost:8080" -ForegroundColor Green
    Write-Host "Default credentials: admin / admin123" -ForegroundColor Yellow
    exit 0
}

if ($Stop) {
    Write-Host "🛑 Stopping Jenkins and Docker-in-Docker setup..." -ForegroundColor Red
    docker-compose -f $compose_file down
    Write-Host "✅ Jenkins stopped" -ForegroundColor Green
    exit 0
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Starting Jenkins with Docker-in-Docker" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

if ($Build) {
    Write-Host "🔨 Building Jenkins image..." -ForegroundColor Blue
    docker-compose -f $compose_file build --no-cache
}

Write-Host "🚀 Starting Jenkins and Docker-in-Docker..." -ForegroundColor Blue
docker-compose -f $compose_file up -d

Write-Host ""
Write-Host "⏳ Waiting for Jenkins to start..." -ForegroundColor Yellow

# Wait for Jenkins to be ready
$retries = 30
$ready = $false

for ($i = 1; $i -le $retries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $ready = $true
            break
        }
    } catch {
        # Jenkins not ready yet
    }
    
    Write-Host "." -NoNewline -ForegroundColor Yellow
    Start-Sleep 2
}

Write-Host ""

if ($ready) {
    Write-Host ""
    Write-Host "✅ Jenkins is ready!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🌐 Jenkins URL: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "🔑 Username: admin" -ForegroundColor Yellow
    Write-Host "🔑 Password: admin123" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Open http://localhost:8080 in your browser" -ForegroundColor Gray
    Write-Host "  2. Login with admin/admin123" -ForegroundColor Gray
    Write-Host "  3. Create a new pipeline job pointing to Jenkinsfile.dind" -ForegroundColor Gray
    Write-Host ""
    Write-Host "🐳 Docker-in-Docker is configured and ready for test execution" -ForegroundColor Green
} else {
    Write-Host "❌ Jenkins failed to start within expected time" -ForegroundColor Red
    Write-Host "Check logs with: docker-compose -f $compose_file logs" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Useful commands:" -ForegroundColor Cyan
Write-Host "  .\start-jenkins.ps1 -Status              # Check status" -ForegroundColor Gray
Write-Host "  .\start-jenkins.ps1 -Stop                # Stop Jenkins" -ForegroundColor Gray
Write-Host "  .\start-jenkins.ps1 -Build               # Rebuild and start" -ForegroundColor Gray
Write-Host "  docker-compose -f $compose_file logs -f  # View logs" -ForegroundColor Gray 