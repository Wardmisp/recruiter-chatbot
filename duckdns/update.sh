#!/bin/sh
# Met à jour l'IPv4 publique de $DOMAIN chez DuckDNS toutes les $DUCKDNS_INTERVAL_SECONDS secondes.
# DuckDNS déduit l'IP de l'origine de la requête (ip= vide), d'où -4 pour forcer l'IPv4.
# Seule la réponse de DuckDNS (OK / KO) est journalisée ; le token est masqué par précaution.
set -u

SUBDOMAIN="${DOMAIN%.duckdns.org}"

while true; do
  result=$(curl -4 -sS --max-time 30 \
    "https://www.duckdns.org/update?domains=${SUBDOMAIN}&token=${DUCKDNS_TOKEN}&ip=" 2>&1 \
    | sed "s/${DUCKDNS_TOKEN}/***/g")
  echo "$(date '+%Y-%m-%d %H:%M:%S') DuckDNS : ${result}"
  sleep "${DUCKDNS_INTERVAL_SECONDS}"
done
