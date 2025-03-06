#!/bin/bash
set -e

echo "Building Auto-Service..."
cd auto-service
./mvnw clean package
cd ..

echo "Building Validation-Service..."
cd validation-service
./mvnw clean package
cd ..

echo "Building Docker images..."
docker-compose build

echo "Done! You can now start the system with: docker-compose up -d"