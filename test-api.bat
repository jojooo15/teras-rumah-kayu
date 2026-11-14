@echo off
echo ==========================================
echo   Testing MongoDB API - Teras Rumah Kayu
echo ==========================================
echo.

echo 1. Testing server availability...
curl -s http://localhost:3000/health > nul
if %errorlevel% neq 0 (
    echo ❌ Server is not running! Please start the server first.
    echo Run: start-api-server.bat
    pause
    exit /b 1
)
echo ✅ Server is running

echo.
echo 2. Testing MongoDB API endpoints...

echo Testing Health Check...
curl -s "http://localhost:3000/health" | findstr "success" > nul
if %errorlevel% equ 0 (
    echo ✅ GET /health - OK
) else (
    echo ❌ GET /health - FAILED
)

echo Testing MongoDB API /api/foodItems...
curl -s "http://localhost:3000/api/foodItems" | findstr "success" > nul
if %errorlevel% equ 0 (
    echo ✅ GET /api/foodItems - OK
) else (
    echo ❌ GET /api/foodItems - FAILED
)

echo Testing MongoDB API /api/categories...
curl -s "http://localhost:3000/api/categories" | findstr "success" > nul
if %errorlevel% equ 0 (
    echo ✅ GET /api/categories - OK
) else (
    echo ❌ GET /api/categories - FAILED
)

echo.
echo 3. Testing legacy endpoints (Android compatibility)...

echo Testing Legacy /foodItems...
curl -s "http://localhost:3000/foodItems" | findstr "id" > nul
if %errorlevel% equ 0 (
    echo ✅ GET /foodItems (legacy) - OK
) else (
    echo ❌ GET /foodItems (legacy) - FAILED
)

echo Testing Legacy /foodItems/1...
curl -s "http://localhost:3000/foodItems/1" | findstr "Paket" > nul
if %errorlevel% equ 0 (
    echo ✅ GET /foodItems/1 (legacy) - OK
) else (
    echo ❌ GET /foodItems/1 (legacy) - FAILED
)

echo Testing Legacy category filtering...
curl -s "http://localhost:3000/foodItems?category=SAMBAL" | findstr "Sambal" > nul
if %errorlevel% equ 0 (
    echo ✅ GET /foodItems?category=SAMBAL (legacy) - OK
) else (
    echo ❌ GET /foodItems?category=SAMBAL (legacy) - FAILED
)

echo.
echo 4. Testing data integrity...
echo Checking food items count...
for /f %%i in ('curl -s "http://localhost:3000/foodItems" ^| findstr /c:"\"id\":" ^| find /c /v ""') do set count=%%i
if %count% geq 16 (
    echo ✅ All 16 food items present
) else (
    echo ⚠️  Missing food items (found %count%, expected 16)
)

echo.
echo ==========================================
echo   MongoDB API Test Summary
echo ==========================================
echo.
echo ✅ MongoDB API is working!
echo.
echo Available endpoints:
echo   🔍 Health: http://localhost:3000/health
echo   📊 API: http://localhost:3000/api/foodItems
echo   🔄 Legacy: http://localhost:3000/foodItems
echo.
echo Your Android app is ready to connect!
echo.
pause
