#!/bin/bash

# 1. Login as super-user to initialize
echo "--- Initializing test user and database as super-user ---"
SU_TOKEN=$(curl -s -X POST http://localhost:8081/api/web-auth/login -H "Content-Type: application/json" -d '{"username":"super-user","password":"superuser-jettra"}' | jq -r '.token')
echo "Super-user Token obtained."

# Create user avbravo (if not exists)
curl -s -X POST http://localhost:8081/api/web-auth/users -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"username":"avbravo","password":"password123","email":"av@test.com","profile":"end-user"}'
echo "User avbravo created."

# Create database db1 (if not exists)
curl -s -X POST http://localhost:8080/api/internal/pd/databases -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"name":"db1","engine":"Document","storage":"Disk"}'
echo "Database db1 created."

# Assign avbravo as admin of db1
curl -s -X POST http://localhost:8081/api/web-auth/databases/db1/sync-roles -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"avbravo":"admin"}'
echo "Assigned avbravo as admin of db1."

# 2. Login as avbravo
echo "--- Testing as avbravo ---"
AV_TOKEN=$(curl -s -X POST http://localhost:8081/api/web-auth/login -H "Content-Type: application/json" -d '{"username":"avbravo","password":"password123"}' | jq -r '.token')
echo "avbravo Token obtained."

# Test Rename db1 -> db1_new
echo "Testing Rename db1 to db1_new..."
curl -s -o /dev/null -w "%{http_code}\n" -X PUT http://localhost:8080/api/internal/pd/databases/db1 -H "Content-Type: application/json" -H "Authorization: Bearer $AV_TOKEN" -d '{"name":"db1_new","engine":"Document","storage":"Disk"}'

# Test Sync Roles on db1_new
echo "Testing Sync Roles on db1_new..."
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8081/api/web-auth/databases/db1_new/sync-roles -H "Content-Type: application/json" -H "Authorization: Bearer $AV_TOKEN" -d '{"avbravo":"admin","osi":"reader"}'

# Test Delete db1_new
echo "Testing Delete db1_new..."
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE http://localhost:8080/api/internal/pd/databases/db1_new -H "Content-Type: application/json" -H "Authorization: Bearer $AV_TOKEN"

echo "--- Verification complete ---"
