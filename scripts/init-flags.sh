#!/usr/bin/env bash
set -euo pipefail

UNLEASH_URL="${UNLEASH_URL:-http://localhost:4242}"
ADMIN_TOKEN="${UNLEASH_ADMIN_TOKEN:-admin-token}"
PROJECT="default"
ENV="development"

create_flag () {
  local NAME="$1"
  local DESC="$2"

  echo "Creating flag: $NAME"
  # Create feature (ignore if already exists)
  curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$UNLEASH_URL/api/admin/projects/$PROJECT/features" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$NAME\",\"description\":\"$DESC\",\"type\":\"release\"}" \
  | grep -E "^(200|201|409)$" >/dev/null
}

enable_flag () {
  local NAME="$1"
  echo "Enabling flag: $NAME in $ENV"
  curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$UNLEASH_URL/api/admin/projects/$PROJECT/features/$NAME/environments/$ENV/on" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
  | grep -E "^(200|202)$" >/dev/null
}

create_flag "premium-pricing" "Enable premium pricing response behavior"
create_flag "order-notifications" "Enable order creation logging notifications"
create_flag "bulk-order-discount" "Enable 15% discount when qty > 5"

# optional: start OFF by default (don’t enable) or enable them:
# enable_flag "premium-pricing"
# enable_flag "order-notifications"
# enable_flag "bulk-order-discount"

echo "Done."