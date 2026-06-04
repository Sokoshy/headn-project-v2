#!/usr/bin/env bash
#
# Re-index Spring Boot 4.0.x and Spring Security 7.0.x docs into the local
# docmancer index (.docmancer/docmancer.db at the project root).
#
# Versions are pinned to the branches matching pom.xml:
#   - spring-boot-starter-parent 4.0.x  -> spring-boot 4.0.x
#   - spring-security.version   7.0.4   -> spring-security 7.0.x
#
# Re-runnable. Uses --skip-known so unchanged files are not re-embedded.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCMANCER="/home/sokoslay/.local/share/pipx/venvs/docmancer/bin/docmancer"
ASCIIDOCTOR_CLI="/home/sokoslay/.local/bin/asciidoctor"
ASCIIDOCTOR_NODE_LIB="/home/sokoslay/.local/share/mise/installs/node/latest/lib/node_modules"
WORKDIR="${DOCMANCER_WORKDIR:-/tmp/docmancer-spring}"

# --- 1. Spring Boot 4.0.x reference (sources AsciiDoc -> Markdown) ---------

if [[ ! -d "$WORKDIR/spring-boot" ]]; then
  echo "[spring-boot] sparse-cloning 4.0.x ..."
  git clone --depth 1 --branch 4.0.x --filter=blob:none --sparse \
      https://github.com/spring-projects/spring-boot.git "$WORKDIR/spring-boot"
  (cd "$WORKDIR/spring-boot" \
      && git sparse-checkout init --no-cone \
      && git sparse-checkout set documentation)
fi

mkdir -p "$WORKDIR/spring-boot-md"
echo "[spring-boot] converting AsciiDoc to Markdown ..."
NODE_PATH="$ASCIIDOCTOR_NODE_LIB" node -e '
  const { createRequire } = require("node:module");
  const req = createRequire(process.env.ASCIIDOCTOR_NODE_LIB + "/");
  const asciidoctor = req("@asciidoctor/core");
  const fs = require("fs"), path = require("path"), { execSync } = require("child_process");
  const conv = asciidoctor();
  const root = process.argv[1], out = process.argv[2];
  const files = execSync(`find ${root}/documentation -name "*.adoc" -type f`, { encoding: "utf8" })
    .trim().split("\n").filter(f => f && !f.includes("partials/") && !f.includes("/nav"));
  let ok = 0, fail = 0;
  for (const src of files) {
    const rel = src.replace(root + "/documentation/", "");
    const dest = path.join(out, rel.replace(/\.adoc$/, ".md"));
    fs.mkdirSync(path.dirname(dest), { recursive: true });
    try {
      const html = conv.convert(fs.readFileSync(src, "utf8"), { safe: "unsafe", backend: "html5" });
      const text = html
        .replace(/<pre[^>]*>([\s\S]*?)<\/pre>/g, (_, c) => "```\n" + c.replace(/<[^>]+>/g, "").trim() + "\n```")
        .replace(/<code[^>]*>([\s\S]*?)<\/code>/g, "`$1`")
        .replace(/<h([1-6])[^>]*>(.*?)<\/h[1-6]>/g, (_, n, t) => "#".repeat(+n) + " " + t.replace(/<[^>]+>/g, "").trim() + "\n\n")
        .replace(/<(strong|b)[^>]*>(.*?)<\/\1>/g, "**$2**")
        .replace(/<(em|i)[^>]*>(.*?)<\/\1>/g, "*$2*")
        .replace(/<a[^>]*href="([^"]+)"[^>]*>(.*?)<\/a>/g, "[$2]($1)")
        .replace(/<li[^>]*>(.*?)<\/li>/g, "- $1\n")
        .replace(/<\/?(ul|ol|p|div|span|br|hr|table|tr|td|th|tbody|thead)[^>]*>/g, "\n")
        .replace(/<[^>]+>/g, "")
        .replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&").replace(/&quot;/g, "\"").replace(/&#39;/g, "\x27")
        .replace(/\n{3,}/g, "\n\n").trim();
      fs.writeFileSync(dest, text);
      ok++;
    } catch { fail++; }
  }
  console.log(`  -> ${ok} converted, ${fail} failed`);
' "$WORKDIR/spring-boot" "$WORKDIR/spring-boot-md"

# --- 2. Spring Security 7.0.x reference -----------------------------------

if [[ ! -d "$WORKDIR/spring-security" ]]; then
  echo "[spring-security] sparse-cloning 7.0.x ..."
  git clone --depth 1 --branch 7.0.x --filter=blob:none --sparse \
      https://github.com/spring-projects/spring-security.git "$WORKDIR/spring-security"
  (cd "$WORKDIR/spring-security" \
      && git sparse-checkout init --no-cone \
      && git sparse-checkout set docs)
fi

mkdir -p "$WORKDIR/spring-security-md"
echo "[spring-security] converting AsciiDoc to Markdown ..."
NODE_PATH="$ASCIIDOCTOR_NODE_LIB" node -e '
  const { createRequire } = require("node:module");
  const req = createRequire(process.env.ASCIIDOCTOR_NODE_LIB + "/");
  const asciidoctor = req("@asciidoctor/core");
  const fs = require("fs"), path = require("path"), { execSync } = require("child_process");
  const conv = asciidoctor();
  const root = process.argv[1], out = process.argv[2];
  const files = execSync(`find ${root}/docs -name "*.adoc" -type f`, { encoding: "utf8" })
    .trim().split("\n").filter(f => f && !f.includes("partials/") && !f.includes("/nav"));
  let ok = 0, fail = 0;
  for (const src of files) {
    const rel = src.replace(root + "/docs/", "");
    const dest = path.join(out, rel.replace(/\.adoc$/, ".md"));
    fs.mkdirSync(path.dirname(dest), { recursive: true });
    try {
      const html = conv.convert(fs.readFileSync(src, "utf8"), { safe: "unsafe", backend: "html5" });
      const text = html
        .replace(/<pre[^>]*>([\s\S]*?)<\/pre>/g, (_, c) => "```\n" + c.replace(/<[^>]+>/g, "").trim() + "\n```")
        .replace(/<code[^>]*>([\s\S]*?)<\/code>/g, "`$1`")
        .replace(/<h([1-6])[^>]*>(.*?)<\/h[1-6]>/g, (_, n, t) => "#".repeat(+n) + " " + t.replace(/<[^>]+>/g, "").trim() + "\n\n")
        .replace(/<(strong|b)[^>]*>(.*?)<\/\1>/g, "**$2**")
        .replace(/<(em|i)[^>]*>(.*?)<\/\1>/g, "*$2*")
        .replace(/<a[^>]*href="([^"]+)"[^>]*>(.*?)<\/a>/g, "[$2]($1)")
        .replace(/<li[^>]*>(.*?)<\/li>/g, "- $1\n")
        .replace(/<\/?(ul|ol|p|div|span|br|hr|table|tr|td|th|tbody|thead)[^>]*>/g, "\n")
        .replace(/<[^>]+>/g, "")
        .replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&").replace(/&quot;/g, "\"").replace(/&#39;/g, "\x27")
        .replace(/\n{3,}/g, "\n\n").trim();
      fs.writeFileSync(dest, text);
      ok++;
    } catch { fail++; }
  }
  console.log(`  -> ${ok} converted, ${fail} failed`);
' "$WORKDIR/spring-security" "$WORKDIR/spring-security-md"

# --- 3. Ingest into local docmancer index ---------------------------------

cd "$PROJECT_ROOT"
echo "[docmancer] ingesting Spring Boot 4.0.x ..."
"$DOCMANCER" --config ./docmancer.yaml ingest "$WORKDIR/spring-boot-md" \
    --format md --recursive --skip-known

echo "[docmancer] ingesting Spring Security 7.0.x ..."
"$DOCMANCER" --config ./docmancer.yaml ingest "$WORKDIR/spring-security-md" \
    --format md --recursive --skip-known

echo "[docmancer] done. Sources:"
"$DOCMANCER" --config ./docmancer.yaml list
