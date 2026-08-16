#!/bin/bash
set -e

export HOME=/root
export PATH=/usr/local/bin:/usr/bin:/bin
export TMPDIR=/tmp
export DEEPSEEK_HARNESS_HOME=/root/.dsh
export PROJECTS_HOME=/root/projects

echo "Bootstraping Ubuntu environment..."

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js not found"
    exit 1
fi

echo "Node.js: $(node --version)"

# Check pnpm
if ! command -v pnpm &> /dev/null; then
    echo "ERROR: pnpm not found"
    exit 1
fi

echo "pnpm: $(pnpm --version)"

# Check Harness
if [ ! -d "/root/deepseek-harness" ]; then
    echo "ERROR: Harness not installed at /root/deepseek-harness"
    exit 1
fi

echo "Bootstrap complete"
