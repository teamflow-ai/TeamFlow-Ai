# TeamFlow AI - Complete Setup & Execution Guide

This package contains the complete, ready-to-run source code for the **TeamFlow AI Platform** (Frontend, Microservices Backend, AI Engine, and Databases).

---

## 📋 Prerequisites
Ensure the following tools are installed and running:
- **Java JDK 21+** (`java -version`)
- **Apache Maven 3.9+** (`mvn -v`)
- **Node.js 18+ & npm** (`node -v`)
- **MySQL Server 8.0+** running on port `3306` (root password: `cdac` by default)
- **MongoDB Community Server 6.0+** running on port `27017`

---

## 🗄️ Step 1: Initialize the Databases (Run Once)

Open PowerShell in the project root directory:

```powershell
# 1. Seed MySQL Database (Roles, Users, Projects, Sprints, Tasks, Work Logs, Milestones)
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pcdac -e "source ./database_seeds/01_mysql_seed_complete.sql"

# 2. Seed MongoDB (AI Recommendations, In-App Notifications, Snapshot Models)
mongosh ./database_seeds/02_mongo_ai_seed.js
```

---

## ⚡ Option A: One-Click Startup (Recommended)

Simply double-click the batch files in the root folder:
1. **Start Backend**: Double-click `start_backend.bat` (opens all 5 services in separate titled windows).
2. **Start Frontend**: Double-click `start_frontend.bat` (opens Vite on `http://localhost:5173`).

---

## 🛠️ Option B: Manual Step-by-Step Backend Startup

If you prefer starting each service manually in its own terminal, open **5 separate PowerShell windows** in `e:\Purvesh TeamFlow Ai\backend`:

### Terminal 1: Service Registry (Eureka - Port 8761)
*(Must be started first — wait 10 seconds before starting other services)*
```powershell
cd "backend"
mvn spring-boot:run -pl service-registry
```

### Terminal 2: API Gateway (Port 8080)
```powershell
cd "backend"
$env:TEAMFLOW_JWT_SECRET="4Fv8Qn7LpX2mRs9ZaB6KdE1WtY5UhJ3NcP8GxL0VmR2QaF7S"
$env:TEAMFLOW_CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
mvn spring-boot:run -pl api-gateway
```

### Terminal 3: Identity Service (Port 8081)
```powershell
cd "backend"
$env:TEAMFLOW_JWT_SECRET="4Fv8Qn7LpX2mRs9ZaB6KdE1WtY5UhJ3NcP8GxL0VmR2QaF7S"
$env:MYSQL_PASSWORD="cdac"
$env:SPRING_CACHE_TYPE="none"
$env:SPRING_AUTOCONFIGURE_EXCLUDE="org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
mvn spring-boot:run -pl identity-service
```

### Terminal 4: Project Service (Port 8082)
```powershell
cd "backend"
$env:TEAMFLOW_JWT_SECRET="4Fv8Qn7LpX2mRs9ZaB6KdE1WtY5UhJ3NcP8GxL0VmR2QaF7S"
$env:MYSQL_PASSWORD="cdac"
$env:SPRING_CACHE_TYPE="none"
$env:SPRING_AUTOCONFIGURE_EXCLUDE="org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
mvn spring-boot:run -pl project-service
```

### Terminal 5: AI Intelligence Service (Port 8083)
```powershell
cd "backend"
$env:TEAMFLOW_JWT_SECRET="4Fv8Qn7LpX2mRs9ZaB6KdE1WtY5UhJ3NcP8GxL0VmR2QaF7S"
$env:TEAMFLOW_AI_PROVIDER="rule-based"
$env:SPRING_CACHE_TYPE="none"
$env:SPRING_AUTOCONFIGURE_EXCLUDE="org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
mvn compile spring-boot:run -pl ai-service
```

---

## 🌐 Step 3: Start the Frontend

In a separate terminal:
```powershell
cd "frontend"
npm install
npm run dev
```

Visit: **`http://localhost:5173`**

---

## 🔑 Pre-Configured Demo Accounts

All accounts use password: **`Admin@123`**

| Role | Email | Capabilities |
| :--- | :--- | :--- |
| **Super Admin** | `purveshpatil1610@gmail.com` | Complete access, AI Insights Hub, User & Employee management, System-wide reports |
| **Project Manager** | `amodp@gmail.com` | Project & Sprint creation, Task assignments, Leave & Milestone approval center |
| **Developer / Employee** | `purveshhpatil@gmail.com` | Task kanban board, timesheet logging (`/worklogs`), leave applications |
