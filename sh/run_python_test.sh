#!/bin/bash
echo "Installing requirements..."
pip install requests

echo "Running Python Load Test..."
python3 sh/load_test.py
