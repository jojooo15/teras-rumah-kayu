@echo off
echo ==========================================
echo   Teras Rumah Kayu - MongoDB Setup
echo ==========================================
echo.

echo 1. Installing Node.js dependencies...
cd mongodb-api
call npm install
if %errorlevel% neq 0 (
    echo ❌ Failed to install dependencies
    echo.
    echo Please ensure Node.js is installed:
    echo https://nodejs.org/
    pause
    exit /b 1
)
echo ✅ Dependencies installed

echo.
echo 2. Testing MongoDB connection...
call npm run test
if %errorlevel% neq 0 (
    echo ❌ MongoDB connection failed
    echo.
    echo 🔧 MongoDB Setup Options:
    echo.
    echo Option 1 - Local MongoDB:
    echo   1. Download: https://www.mongodb.com/try/download/community
    echo   2. Install and start MongoDB service
    echo   3. Run this setup again
    echo.
    echo Option 2 - MongoDB Atlas (Cloud):
    echo   1. Create account: https://cloud.mongodb.com/
    echo   2. Create cluster and get connection string
    echo   3. Update MONGODB_URI in .env file
    echo   4. Run this setup again
    echo.
    pause
    exit /b 1
)
echo ✅ MongoDB connection successful

echo.
echo 3. Seeding database with menu data...
call npm run seed
if %errorlevel% neq 0 (
    echo ❌ Failed to seed database
    pause
    exit /b 1
)
echo ✅ Database seeded with 16 food items

echo.
echo ==========================================
echo   Setup Complete! 🎉
echo ==========================================
echo.
echo Your MongoDB API is ready!
echo.
echo Next steps:
echo   1. Start server: start-api-server.bat
echo   2. Test API: test-api.bat
echo   3. Run Android app
echo.
echo Database contains:
echo   📊 16 Food Items (Paket Sop, Pecak, Geprek, etc.)
echo   📂 9 Categories
echo   🖼️  Images with fallback support
echo.
pause
