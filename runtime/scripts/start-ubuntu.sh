#!/bin/bash
set -e

UBUNTU_ROOTFS="${1:-/data/data/com.deepseek.harnessmobile/files/linux/ubuntu}"
PROJECT_DIR="${2:-/storage/emulated/0/Projects}"

echo "Starting PRoot with Ubuntu rootfs: $UBUNTU_ROOTFS"

exec proot \
    --link2symlink \
    -0 \
    -r "$UBUNTU_ROOTFS" \
    -b /dev \
    -b /proc \
    -b /sys \
    -b "$PROJECT_DIR:/root/projects" \
    /usr/bin/env \
    -i \
    HOME=/root \
    USER=root \
    PATH=/usr/local/bin:/usr/bin:/bin \
    /bin/bash \
    --login
