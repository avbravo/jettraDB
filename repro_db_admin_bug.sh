#!/bin/bash
# Reproduction script for database-level admin permissions bug

PD_URL="http://localhost:8080"
WEB_URL="http://localhost:8081" # Jettra-web runs on 8081

echo "--- 1. Login as super-user ---"
SU_TOKEN=$(curl -s -X POST $PD_URL/api/auth/login -H "Content-Type: application/json" -d '{"username":"super-user","password":"adminadmin"}' | grep -oP '(?<="token":")[^"]+')

if [ -z "$SU_TOKEN" ]; then
    echo "FAILED: Could not login as super-user"
    exit 1
fi
echo "SU_TOKEN acquired."

echo "--- 2. Create database 'mydb' as super-user ---"
curl -s -X POST $PD_URL/api/internal/pd/databases -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"name":"mydb","engine":"Document","storage":"Disk"}'
echo ""

echo "--- 3. Create user 'avbravo' (end-user profile) ---"
curl -s -X POST $PD_URL/api/web-auth/users -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"username":"avbravo","password":"password123","profile":"end-user","roles":[]}'
echo ""

echo "--- 4. Assign 'admin' role for 'mydb' to 'avbravo' ---"
# We use sync-roles or direct assignment. The UI uses sync-roles.
curl -s -X POST $PD_URL/api/web-auth/databases/mydb/sync-roles -H "Content-Type: application/json" -H "Authorization: Bearer $SU_TOKEN" -d '{"avbravo":"admin"}'
echo ""

echo "--- 5. Login as 'avbravo' ---"
AV_TOKEN=$(curl -s -X POST $PD_URL/api/auth/login -H "Content-Type: application/json" -d '{"username":"avbravo","password":"password123"}' | grep -oP '(?<="token":")[^"]+')

if [ -z "$AV_TOKEN" ]; then
    echo "FAILED: Could not login as avbravo"
    exit 1
fi
echo "AV_TOKEN acquired."

echo "--- 6. Attempt to SYNC ROLES as 'avbravo' for 'mydb' ---"
# We'll use -w to get status code and -o to capture body
SYNC_RESPONSE=$(curl -s -i -X POST $PD_URL/api/web-auth/databases/mydb/sync-roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $AV_TOKEN" \
  -d '{"avbravo":"admin", "otheruser":"read"}')

echo "Full Response:"
echo "$SYNC_RESPONSE"

if echo "$SYNC_RESPONSE" | grep -q "403 Forbidden"; then
    echo "BUG REPRODUCED: avbravo (mydb admin) was denied access (403)."
elif echo "$SYNC_RESPONSE" | grep -q "200 OK"; then
    echo "Access GRANTED (200 OK)."
else
    echo "Unexpected response."
fi

echo "--- CLEANUP ---"
curl -s -X DELETE $PD_URL/api/auth/users/avbravo -H "Authorization: Bearer $SU_TOKEN"
# curl -s -X DELETE $PD_URL/api/internal/pd/databases/mydb -H "Authorization: Bearer $SU_TOKEN"
