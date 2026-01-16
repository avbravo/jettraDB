#!/bin/bash

# 1. Login as Admin
echo "Logging in as admin..."
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"adminadmin"}' | jq -r '.token')
echo "Admin Token: $ADMIN_TOKEN"

# 2. Create User avbravo (if not exists)
echo "Creating user avbravo..."
curl -s -X POST http://localhost:8080/api/auth/users -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d '{"username":"avbravo","password":"password123","email":"avbravo@example.com","roles":[]}'

# 3. Create Database db2
echo "Creating database db2..."
curl -s -X POST http://localhost:8080/api/internal/pd/databases -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d '{"name":"db2","engine":"Document","storage":"Disk"}'

# 4. Assign avbravo as admin of db2
echo "Assigning avbravo as admin of db2..."
# Note: In the UI this is done via sync-roles.
# Mapping: {"avbravo": "admin"}
curl -s -X POST http://localhost:8080/api/auth/databases/db2/sync-roles -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d '{"avbravo":"admin"}'

# 5. Login as avbravo
echo "Logging in as avbravo..."
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"avbravo","password":"password123"}' | jq -r '.token')
echo "User Token: $USER_TOKEN"

# 6. Attempt to Rename db2 to db2_mod (as avbravo)
echo "Attempting to rename db2 to db2_mod as avbravo..."
RENAME_RESPONSE=$(curl -s -w "%{http_code}" -X PUT http://localhost:8080/api/internal/pd/databases/db2 -H "Content-Type: application/json" -H "Authorization: Bearer $USER_TOKEN" -d '{"name":"db2_mod","engine":"Document","storage":"Disk"}')
echo "Rename Response Code: $RENAME_RESPONSE"

# 7. Attempt to sync roles (users) on db2_mod (as avbravo)
echo "Attempting to sync roles on db2_mod as avbravo..."
SYNC_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/auth/databases/db2_mod/sync-roles -H "Content-Type: application/json" -H "Authorization: Bearer $USER_TOKEN" -d '{"avbravo":"admin","osi":"reader"}')
echo "Sync Response Code: $SYNC_RESPONSE"

# 8. Cleanup (optional, or manual)
