@echo off
echo ==========================================
echo   Teras Rumah Kayu - MongoDB API Server
echo ==========================================
echo.

if not exist "mongodb-api\package.json" (
    echo ❌ MongoDB API not found!
    echo.
    echo Please run setup first:
    echo   setup.bat
    echo.
    pause
    exit /b 1
)

echo Starting MongoDB API Server...
cd mongodb-api

echo.
echo Server will be available at: http://localhost:3000
echo.
echo API Endpoints:
echo   📊 Food Items: http://localhost:3000/api/foodItems
echo   📂 Categories: http://localhost:3000/api/categories
echo   🔍 Health Check: http://localhost:3000/health
echo   🔄 Legacy (Android): http://localhost:3000/foodItems
echo.
echo Press Ctrl+C to stop the server
echo.

npm start
