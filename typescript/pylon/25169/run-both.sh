#!/bin/bash

set -e

echo "========================================="
echo "Running main.ts from 1.11.7 (Temporal 1.11.7)"
echo "========================================="
cd 1.11.7
npm run main
cd ..

echo ""
echo "========================================="
echo "Running main.ts from 1.14.1 (Temporal 1.14.1)"
echo "========================================="
cd 1.14.1
npm run main
cd ..

echo ""
echo "========================================="
echo "Both executions completed"
echo "========================================="