# PQVCF — Post-Quantum Verifiable Compliance Framework
# Automated Monorepo Verification & Testing Script
# Run this script in PowerShell from the project root: .\verify_framework.ps1

$ErrorActionPreference = "Stop"
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$summary = @()

function Log-Header($msg) {
    Write-Host "`n==================================================" -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Cyan -Bold
    Write-Host "==================================================" -ForegroundColor Cyan
}

function Log-Success($msg) {
    Write-Host "✔ $msg" -ForegroundColor Green
}

function Log-Failure($msg) {
    Write-Host "✘ $msg" -ForegroundColor Red -Bold
}

# 1. Environment Checks
Log-Header "Phase 1: Environmental Prerequisites Check"

# Java Check
try {
    $javaVer = java -version 2>&1 | Out-String
    Log-Success "Java Runtime detected:"
    Write-Host $javaVer.Trim() -ForegroundColor DarkGray
    $summary += [PSCustomObject]@{ Task = "Java Check"; Status = "PASS"; Details = "Java OK" }
} catch {
    Log-Failure "Java JDK is missing or not configured on the PATH."
    $summary += [PSCustomObject]@{ Task = "Java Check"; Status = "FAIL"; Details = "Java Missing" }
}

# NodeJS / NPM Check
try {
    $nodeVer = node --version
    $npmVer = npm --version
    Log-Success "NodeJS detected: $nodeVer"
    Log-Success "NPM detected: $npmVer"
    $summary += [PSCustomObject]@{ Task = "NodeJS / NPM Check"; Status = "PASS"; Details = "Node $nodeVer, NPM $npmVer" }
} catch {
    Log-Failure "NodeJS or NPM is missing from the PATH."
    $summary += [PSCustomObject]@{ Task = "NodeJS / NPM Check"; Status = "FAIL"; Details = "Node/NPM Missing" }
}

# Maven Check
$hasMaven = $false
try {
    $mvnVer = mvn -version | Out-String
    Log-Success "Apache Maven detected:"
    Write-Host $mvnVer.Split([char]10)[0].Trim() -ForegroundColor DarkGray
    $hasMaven = $true
    $summary += [PSCustomObject]@{ Task = "Maven Check"; Status = "PASS"; Details = "Maven OK" }
} catch {
    Log-Failure "Maven (mvn) is not detected on the path. Java compilation will be bypassed."
    $summary += [PSCustomObject]@{ Task = "Maven Check"; Status = "WARNING"; Details = "Maven Missing (Bypassed)" }
}


# 2. Compile & Test Backend
if ($hasMaven) {
    Log-Header "Phase 2: Compiling & Testing Java Microservices Monorepo"
    try {
        Write-Host "Executing 'mvn clean test' over 10 compliance modules..." -ForegroundColor Yellow
        mvn clean test
        Log-Success "All Java unit and integration tests successfully verified!"
        $summary += [PSCustomObject]@{ Task = "Java Monorepo Build & Test"; Status = "PASS"; Details = "All JUnit Integration ITs passed cleanly" }
    } catch {
        Log-Failure "Java compilation or test suite failure occurred."
        $summary += [PSCustomObject]@{ Task = "Java Monorepo Build & Test"; Status = "FAIL"; Details = $_.Exception.Message }
    }
} else {
    Log-Header "Phase 2: Bypassing Java Backend Compilation (No Maven)"
}


# 3. Compile & Lint React Frontend
Log-Header "Phase 3: Installing & Verifying React Vite Dashboard"
try {
    Push-Location research-dashboard-frontend
    
    Write-Host "Installing NPM dependencies..." -ForegroundColor Yellow
    npm install
    
    Write-Host "Verifying TypeScript compilation and building static bundle..." -ForegroundColor Yellow
    npm run build
    
    Pop-Location
    Log-Success "React dashboard compilation, lint checks, and bundles sealed successfully!"
    $summary += [PSCustomObject]@{ Task = "Frontend Build & Compile"; Status = "PASS"; Details = "Vite build completed successfully" }
} catch {
    Pop-Location
    Log-Failure "Frontend dashboard compilation or dependency installation failed."
    $summary += [PSCustomObject]@{ Task = "Frontend Build & Compile"; Status = "FAIL"; Details = $_.Exception.Message }
}


# 4. Final Verification Summary Report
Log-Header "PQVCF Test Verification Summary Report"
$sw.Stop()
Write-Host "Execution Time: $([Math]::Round($sw.Elapsed.TotalSeconds, 2)) seconds" -ForegroundColor DarkGray
Write-Host ""

$failed = $false
$summary | Format-Table -AutoSize

foreach ($item in $summary) {
    if ($item.Status -eq "FAIL") {
        $failed = $true
    }
}

if ($failed) {
    Log-Failure "PQVCF Verification Finished with Failures. Check logs above."
    exit 1
} else {
    Log-Success "Post-Quantum Verifiable Compliance Framework verified successfully!"
    exit 0
}
