#!/bin/bash

# 一键启动前后端开发环境
# 用法: ./dev.sh

cleanup() {
  echo ""
  echo "正在停止服务..."
  kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
  wait $BACKEND_PID $FRONTEND_PID 2>/dev/null
  echo "已停止"
  exit 0
}

trap cleanup SIGINT SIGTERM

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== 启动后端 ==="
cd "$PROJECT_DIR/backend"
JAVA_HOME=/usr/local/opt/openjdk@17 mvn spring-boot:run &
BACKEND_PID=$!

echo "=== 启动前端 ==="
cd "$PROJECT_DIR/frontend"
pnpm dev &
FRONTEND_PID=$!

echo ""
echo "后端: http://localhost:8080"
echo "前端: http://localhost:8848"
echo "按 Ctrl+C 停止所有服务"
echo ""

wait
