#!/bin/bash
# Script para parar a aplicação Spring Boot

echo "🛑 Parando aplicação Spring Boot..."

# Matar processos na porta 8080
lsof -ti:8080 2>/dev/null | xargs kill -9 2>/dev/null
fuser -k 8080/tcp 2>/dev/null

# Matar processos Java relacionados
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "TeleconsultaApplication" 2>/dev/null
pkill -f "mvnw.*spring-boot" 2>/dev/null

# Verificar se parou
sleep 1
if ss -tlnp 2>/dev/null | grep -q 8080 || netstat -tlnp 2>/dev/null | grep -q 8080; then
    echo "⚠️  Ainda há algo na porta 8080"
    ss -tlnp 2>/dev/null | grep 8080 || netstat -tlnp 2>/dev/null | grep 8080
else
    echo "✅ Porta 8080 está livre!"
fi

