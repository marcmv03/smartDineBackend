#!/bin/bash
# Script to restart SmartDine backend with SSL certificate from Let's Encrypt

echo "🛑 Stopping containers..."
sudo docker compose down

echo "🏗️  Rebuilding application..."
sudo docker compose build --no-cache springboot-app

echo "🚀 Starting containers..."
sudo docker compose up -d

echo "⏳ Waiting for services to be healthy..."
sleep 10

echo "🔍 Checking container logs..."
sudo docker compose logs springboot-app --tail=50

echo ""
echo "✅ Done! Check the logs above for any errors."
echo ""
echo "To view live logs, run: sudo docker compose logs -f springboot-app"
echo "To check health: curl -k https://localhost:8443/actuator/health"
