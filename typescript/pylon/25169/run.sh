#!/bin/bash

echo "========================================="
echo "Running main.ts from 1.11.7 (Temporal 1.11.7)"
echo "========================================="
cd 1.11.7
timeout 2 npm run main || echo "Process timed out or exited with error"
cd ..

echo ""
echo "========================================="
echo "Running main.ts from 1.14.1 (Temporal 1.14.1)"
echo "========================================="
cd 1.14.1
timeout 10 npm run main || echo "Process timed out or exited with error"
cd ..

echo ""
echo "========================================="
echo "Both executions completed"
echo "========================================="