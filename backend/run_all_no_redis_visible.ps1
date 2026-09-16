$env:TEAMFLOW_JWT_SECRET="4Fv8Qn7LpX2mRs9ZaB6KdE1WtY5UhJ3NcP8GxL0VmR2QaF7S"
$env:MYSQL_PASSWORD="cdac"
$env:RABBITMQ_USER="guest"
$env:RABBITMQ_PASSWORD="guest"
$env:SPRING_CACHE_TYPE="none"
$env:SPRING_AUTOCONFIGURE_EXCLUDE="org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
$env:TEAMFLOW_CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
$env:TEAMFLOW_AI_PROVIDER="rule-based"

Write-Host "Starting Service Registry..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k mvn spring-boot:run -pl service-registry" -WindowStyle Normal
Start-Sleep -Seconds 10

Write-Host "Starting API Gateway..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k mvn spring-boot:run -pl api-gateway" -WindowStyle Normal

Write-Host "Starting Identity Service..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k mvn spring-boot:run -pl identity-service" -WindowStyle Normal

Write-Host "Starting Project Service..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k mvn spring-boot:run -pl project-service" -WindowStyle Normal

Write-Host "Starting AI Service..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k mvn spring-boot:run -pl ai-service" -WindowStyle Normal

Write-Host "All services have been launched in normal windows!"
