#!/bin/bash

# =============================================
# build.sh — Compila e executa o projeto
# Uso: ./build.sh (a partir do diretório src/)
# =============================================

SRC_DIR="main"
BIN_DIR="../bin"
MAIN_CLASS="main.Main"

# ===== STEP 1: LIMPAR COMPILAÇÃO ANTERIOR =====
if [ -d "$BIN_DIR" ]; then
    echo "[1/3] Limpando compilação anterior..."
    rm -rf "$BIN_DIR"
fi

mkdir -p "$BIN_DIR"

# ===== STEP 2: COMPILAR =====
echo "[2/3] Compilando..."

javac -d "$BIN_DIR" \
    "$SRC_DIR"/main/Main.java \
    "$SRC_DIR"/models/*.java \
    "$SRC_DIR"/structures/*.java \
    "$SRC_DIR"/controllers/*.java \
    "$SRC_DIR"/sorting/*.java

# Verifica se a compilação foi bem-sucedida
if [ $? -ne 0 ]; then
    echo ""
    echo "[ERRO] Compilação falhou. Verifique os erros acima."
    exit 1
fi

echo "[2/3] Compilação concluída."

# ===== STEP 3: EXECUTAR =====
echo "[3/3] Iniciando o programa..."
echo ""

java -cp "$BIN_DIR" "$MAIN_CLASS"
