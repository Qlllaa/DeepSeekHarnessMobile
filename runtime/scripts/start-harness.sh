#!/bin/bash
set -e

cd /root/deepseek-harness

if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    pnpm install
fi

echo "Building Harness..."
pnpm run build

echo "Starting Harness web server on 127.0.0.1:3080..."
exec pnpm dsh web --host 127.0.0.1 --port 3080
