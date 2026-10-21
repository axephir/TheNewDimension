#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 - <<'PY'
import hashlib
import os
from pathlib import Path
import platform
import tarfile
import urllib.request

root = Path('/workspace/.toolchains')
jdk = Path(os.environ.get('TND_JDK_HOME', str(root / 'jdk-25.0.4.1+1')))
if not (jdk / 'bin/javac').is_file():
    if platform.machine() != 'x86_64' or platform.system() != 'Linux':
        raise SystemExit('Provide an appropriate Java 25 JDK through TND_JDK_HOME on this platform.')
    root.mkdir(parents=True, exist_ok=True)
    archive = root / 'downloads/OpenJDK25U-jdk_x64_linux_hotspot_25.0.4.1_1.tar.gz'
    archive.parent.mkdir(parents=True, exist_ok=True)
    expected = 'dbb698396d478e7fa2b1e50f4103324b2a99b90569ee27c33f2261f9215cf41e'
    if not archive.is_file():
        url = 'https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.4.1%2B1/' + archive.name
        urllib.request.urlretrieve(url, archive)
    with archive.open('rb') as source:
        actual = hashlib.file_digest(source, 'sha256').hexdigest()
    if actual != expected:
        raise SystemExit('JDK archive SHA-256 mismatch; preserving it for diagnosis without extracting it.')
    with tarfile.open(archive) as source:
        source.extractall(root, filter='data')
    if not (jdk / 'bin/javac').is_file():
        raise SystemExit('JDK was extracted under /workspace/.toolchains; correct TND_JDK_HOME.')
print('Java toolchain available; artifact verification kept enabled.')
PY
export JAVA_HOME="${TND_JDK_HOME:-/workspace/.toolchains/jdk-25.0.4.1+1}"
python3 scripts/check_resources.py
bash scripts/test-core.sh
python3 scripts/cloud_gradle.py build
