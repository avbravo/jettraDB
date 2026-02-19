#!/bin/bash

# Configuration
PD_URL="http://localhost:8080"
WEB_URL="http://localhost:8081"

# 1. Login as super-user to create a management user
echo "--- Logging in as super-user ---"
SU_TOKEN=$(curl -s -X POST $WEB_URL/api/web-auth/login -H "Content-Type: application/json" -d '{"username":"super-user","password":"superuser-jettra"}' | jq -r '.token')

if [ "$SU_TOKEN" == "null" ] || [ -z "$SU_TOKEN" ]; then
    echo "Failed to obtain super-user token. Is the server running?"
    exit 1
fi

echo "Creating management user 'mgr1'..."
curl -s -X POST $WEB_URL/api/web-auth/users -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"username":"mgr1","password":"password123","email":"mgr1@test.com","profile":"management"}'
echo ""

# 2. Login as mgr1
echo "--- Logging in as mgr1 ---"
MGR_TOKEN=$(curl -s -X POST $WEB_URL/api/web-auth/login -H "Content-Type: application/json" -d '{"username":"mgr1","password":"password123"}' | jq -r '.token')

if [ "$MGR_TOKEN" == "null" ] || [ -z "$MGR_TOKEN" ]; then
    echo "Failed to obtain mgr1 token."
    exit 1
fi

# 3. Attempt to create a database as mgr1
echo "--- Attempting to create database 'testdb_mgr' as mgr1 ---"
HTTP_CODE_MGR=$(curl -s -o /dev/null -w "%{http_code}" -X POST $PD_URL/api/internal/pd/databases -H "Content-Type: application/json" -H "Authorization: Bearer $MGR_TOKEN" -d '{"name":"testdb_mgr","engine":"Document","storage":"Disk"}')
echo "Result Code (mgr1): $HTTP_CODE_MGR"

# 4. Create an end-user and test
echo "Creating end-user 'user1'..."
curl -s -X POST $WEB_URL/api/web-auth/users -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"username":"user1","password":"password123","email":"user1@test.com","profile":"end-user"}'
echo ""

echo "--- Logging in as user1 ---"
USER_TOKEN=$(curl -s -X POST $WEB_URL/api/web-auth/login -H "Content-Type: application/json" -d '{"username":"user1","password":"password123"}' | jq -r '.token')

echo "--- Attempting to create database 'testdb_user' as user1 ---"
HTTP_CODE_USER=$(curl -s -o /dev/null -w "%{http_code}" -X POST $PD_URL/api/internal/pd/databases -H "Content-Type: application/json" -H "Authorization: Bearer $USER_TOKEN" -d '{"name":"testdb_user","engine":"Document","storage":"Disk"}')
echo "Result Code (user1): $HTTP_CODE_USER"

# Summary
if [ "$HTTP_CODE_MGR" == "200" ]; then
    echo "VERIFICATION: mgr1 COULD create a database"
else
    echo "VERIFICATION: mgr1 COULD NOT create a database ($HTTP_CODE_MGR)"
fi

if [ "$HTTP_CODE_USER" == "200" ]; then
    echo "VERIFICATION: user1 COULD create a database"
else
    echo "VERIFICATION: user1 COULD NOT create a database ($HTTP_CODE_USER)"
fi

# 5. Clean up (as super-user)
echo "--- Cleaning up ---"
curl -s -X DELETE $PD_URL/api/internal/pd/databases/testdb_mgr -H "Authorization: Bearer $SU_TOKEN"
curl -s -X DELETE $PD_URL/api/internal/pd/databases/testdb_user -H "Authorization: Bearer $SU_TOKEN"
curl -s -X DELETE $WEB_URL/api/web-auth/users/mgr1 -H "Authorization: Bearer $SU_TOKEN"
curl -s -X DELETE $WEB_URL/api/web-auth/users/user1 -H "Authorization: Bearer $SU_TOKEN"
echo ""
