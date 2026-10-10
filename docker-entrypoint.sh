#!/bin/sh
set -eu

normalize_database_url() {
  value="$1"

  case "$value" in
    jdbc:*) printf '%s' "$value" ;;
    postgres://*) rest="${value#postgres://}"; scheme="jdbc:postgresql://" ;;
    postgresql://*) rest="${value#postgresql://}"; scheme="jdbc:postgresql://" ;;
    *) echo "Unsupported PostgreSQL URL scheme" >&2; exit 1 ;;
  esac

  if printf '%s' "$rest" | grep -q '@'; then
    userinfo="${rest%%@*}"
    host_and_query="${rest#*@}"
    userinfo=$(printf '%s' "$userinfo" | sed 's/@/%40/g')
    printf '%s%s@%s' "$scheme" "$userinfo" "$host_and_query"
  else
    printf '%s%s' "$scheme" "$rest"
  fi
}

if [ -z "${SPRING_DATASOURCE_URL:-}" ] && [ -n "${DATABASE_URL:-}" ]; then
  export SPRING_DATASOURCE_URL="$(normalize_database_url "$DATABASE_URL")"
fi

if [ -n "${DIRECT_URL:-}" ]; then
  export DIRECT_URL="$(normalize_database_url "$DIRECT_URL")"
fi

exec java -jar app.jar
