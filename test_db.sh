#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"adminadmin"}' | jq -r .token)
echo "Token: $TOKEN"

echo "Creating DB demo_curl_db..."
curl -v -X POST http://localhost:8081/api/db \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "demo_curl_db", "storage": "STORE"}'

echo -e "\n\nAdding Collection..."
curl -v -X POST http://localhost:8081/api/db/demo_curl_db/collections/test_col \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"engine": "Document"}'

echo -e "\n\nChecking Collection..."
curl -v -X GET http://localhost:8081/api/db/demo_curl_db/collections \
  -H "Authorization: Bearer $TOKEN"
