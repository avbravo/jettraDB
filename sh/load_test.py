import requests
import json
import time
import random
import concurrent.futures

# Configuration
PD_URL = "http://localhost:8080"
ENGINE_URL = "http://localhost:8081/api/document" # Assuming entry point for docs
TOTAL_RECORDS = 1000
CONCURRENCY = 10

def insert_document(i):
    payload = {
        "id": f"user-{i}",
        "collection": "users",
        "data": {
            "name": f"User {i}",
            "email": f"user{i}@example.com",
            "score": random.randint(1, 100),
            "timestamp": time.time()
        }
    }
    try:
        # In JettraDB, we normally query PD first, but for simplicity 
        # we go through the web/engine gateway.
        response = requests.post(f"{ENGINE_URL}/save", json=payload, timeout=5)
        return response.status_code == 200
    except Exception as e:
        print(f"Error inserting {i}: {e}")
        return False

def run_load_test():
    print(f"🚀 Starting Python Load Test: inserting {TOTAL_RECORDS} documents...")
    start_time = time.time()
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
        results = list(executor.map(insert_document, range(TOTAL_RECORDS)))
    
    end_time = time.time()
    success_count = sum(results)
    
    print("\n--- Load Test Results ---")
    print(f"Total Time: {end_time - start_time:.2f} seconds")
    print(f"Successful Inserts: {success_count}")
    print(f"Throughput: {success_count / (end_time - start_time):.2f} req/s")

if __name__ == "__main__":
    run_load_test()
