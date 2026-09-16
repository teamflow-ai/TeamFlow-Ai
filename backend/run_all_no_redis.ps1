# ==============================================================================
# TEAMFLOW AI - AUTOMATED BACKEND LAUNCHER
# Starts Service Registry, API Gateway, Identity, Project, and AI Services
# ==============================================================================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  TEAMFLOW AI BACKEND SERVICES LAUNCHER              " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan

$env:TEAMFLOW_JWT_SECRET="4Fv8Qn7LpX2mRs9ZaB6KdE1WtY5UhJ3NcP8GxL0VmR2QaF7S"
$env:MYSQL_PASSWORD="cdac"
$env:RABBITMQ_USER="guest"
$env:RABBITMQ_PASSWORD="guest"
$env:SPRING_CACHE_TYPE="none"
$env:SPRING_AUTOCONFIGURE_EXCLUDE="org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
$env:TEAMFLOW_CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
$env:TEAMFLOW_AI_PROVIDER="rule-based"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "[1/5] Starting Service Registry (Eureka :8761)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$host.ui.RawUI.WindowTitle='Eureka Registry (8761)'; mvn spring-boot:run -pl service-registry"
Start-Sleep -Seconds 12

Write-Host "[2/5] Starting API Gateway (:8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:TEAMFLOW_JWT_SECRET='$($env:TEAMFLOW_JWT_SECRET)'; `$env:TEAMFLOW_CORS_ORIGINS='$($env:TEAMFLOW_CORS_ORIGINS)'; `$host.ui.RawUI.WindowTitle='API Gateway (8080)'; mvn spring-boot:run -pl api-gateway"

Write-Host "[3/5] Starting Identity Service (:8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:TEAMFLOW_JWT_SECRET='$($env:TEAMFLOW_JWT_SECRET)'; `$env:MYSQL_PASSWORD='$($env:MYSQL_PASSWORD)'; `$env:SPRING_CACHE_TYPE='none'; `$env:SPRING_AUTOCONFIGURE_EXCLUDE='$($env:SPRING_AUTOCONFIGURE_EXCLUDE)'; `$host.ui.RawUI.WindowTitle='Identity Service (8081)'; mvn spring-boot:run -pl identity-service"

Write-Host "[4/5] Starting Project Service (:8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:TEAMFLOW_JWT_SECRET='$($env:TEAMFLOW_JWT_SECRET)'; `$env:MYSQL_PASSWORD='$($env:MYSQL_PASSWORD)'; `$env:SPRING_CACHE_TYPE='none'; `$env:SPRING_AUTOCONFIGURE_EXCLUDE='$($env:SPRING_AUTOCONFIGURE_EXCLUDE)'; `$host.ui.RawUI.WindowTitle='Project Service (8082)'; mvn spring-boot:run -pl project-service"

Write-Host "[5/5] Starting AI Service (:8083)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:TEAMFLOW_JWT_SECRET='$($env:TEAMFLOW_JWT_SECRET)'; `$env:TEAMFLOW_AI_PROVIDER='rule-based'; `$env:SPRING_CACHE_TYPE='none'; `$env:SPRING_AUTOCONFIGURE_EXCLUDE='$($env:SPRING_AUTOCONFIGURE_EXCLUDE)'; `$host.ui.RawUI.WindowTitle='AI Service (8083)'; mvn compile spring-boot:run -pl ai-service"

Write-Host ""
Write-Host "All 5 Backend Microservices have been launched in separate titled windows!" -ForegroundColor Green
Write-Host "API Gateway will be ready at: http://localhost:8080" -ForegroundColor Green
