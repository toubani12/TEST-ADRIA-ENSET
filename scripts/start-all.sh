#!/usr/bin/env bash
# Lance tous les micro-services (jars déjà construits par ./mvnw -DskipTests package).
# La clé du LLM est lue depuis .env (non versionné), jamais écrite dans le code.
cd "$(dirname "$0")/.."
mkdir -p logs
if [ -f .env ]; then set -a; . ./.env; set +a
elif [ -f ../enset-adria-stock-market-test-2026/.env ]; then set -a; . ../enset-adria-stock-market-test-2026/.env; set +a; fi
run() { nohup java -jar "$1"/target/"$1"-0.0.1-SNAPSHOT.jar > logs/"$1".log 2>&1 & echo "$1 -> pid $!"; }
wait_port() { for i in $(seq 1 60); do nc -z localhost "$1" 2>/dev/null && return 0; sleep 1; done; echo "port $1 KO"; }
run Config-service;     wait_port 8888
run discovery-service;  wait_port 8761
run account-service;    wait_port 8081
run transaction-service; wait_port 8082
run audit-service;      wait_port 8083
run gatway-service;     wait_port 9090
run chat-bot-service;   wait_port 8084
echo "Tous les services sont démarrés (logs/ pour le détail)."
