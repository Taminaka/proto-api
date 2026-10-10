#!/bin/sh
set -eu

if [ -z "${SPRING_DATASOURCE_URL:-}" ] && [ -n "${DATABASE_URL:-}" ]; then
  case "$DATABASE_URL" in
    jdbc:*) export SPRING_DATASOURCE_URL="$DATABASE_URL" ;;
    postgres://*|postgresql://*)
      rest="${DATABASE_URL#*://}"
      if printf '%s' "$rest" | grep -q '@'; then
        userinfo="${rest%%@*}"
        host_and_query="${rest#*@}"
        export SPRING_DATASOURCE_USERNAME="${userinfo%%:*}"
        export SPRING_DATASOURCE_PASSWORD="${userinfo#*:}"
        export SPRING_DATASOURCE_URL="jdbc:postgresql://${host_and_query}"
      else
        export SPRING_DATASOURCE_URL="jdbc:postgresql://${rest}"
      fi
      ;;
    *) echo "Unsupported DATABASE_URL scheme" >&2; exit 1 ;;
  esac
fi

exec java -jar app.jar
