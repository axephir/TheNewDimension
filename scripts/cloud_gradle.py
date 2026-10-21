#!/usr/bin/env python3
"""Activate the cloud JDK, trusted proxy certificate and Gradle's HTTP proxy.

Never prints environment values or disables TLS/checksum verification.
Outside Codex cloud, use ./gradlew normally with a Java 25 JDK.
"""
import os
from pathlib import Path
import subprocess
import sys
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[1]
jdk = Path(os.environ.get('TND_JDK_HOME', '/workspace/.toolchains/jdk-25.0.4.1+1'))
if not (jdk / 'bin/javac').is_file():
    raise SystemExit('JDK 25 missing. Run bash scripts/setup-cloud.sh first, or set TND_JDK_HOME.')
cache = Path(os.environ.get('GRADLE_USER_HOME', '/workspace/.gradle-cache'))
cache.mkdir(parents=True, exist_ok=True)
cert = os.environ.get('CODEX_PROXY_CERT')
if cert:
    keytool = str(jdk / 'bin/keytool')
    store = str(jdk / 'lib/security/cacerts')
    alias = 'codex-environment-proxy'
    common = ['-keystore', store, '-storepass', 'changeit']
    # This JDK is task-local. Refresh only our alias; retain all vendor trust roots.
    subprocess.run([keytool, '-delete', '-alias', alias, *common], capture_output=True)
    result = subprocess.run([keytool, '-importcert', '-noprompt', '-alias', alias, '-file', cert, *common], capture_output=True)
    if result.returncode:
        raise SystemExit('Could not import the platform-provided proxy certificate; TLS remains enforced.')
proxy = urlsplit(os.environ.get('HTTPS_PROXY', os.environ.get('https_proxy', '')))
env = dict(os.environ, JAVA_HOME=str(jdk), GRADLE_USER_HOME=str(cache))
env['PATH'] = str(jdk / 'bin') + os.pathsep + env.get('PATH', '')
if proxy.hostname:
    properties = cache / 'gradle.properties'
    keys = ['systemProp.https.proxyHost', 'systemProp.https.proxyPort',
            'systemProp.http.proxyHost', 'systemProp.http.proxyPort']
    # Preserve unrelated user properties and never print this file.
    lines = properties.read_text().splitlines() if properties.exists() else []
    lines = [line for line in lines if not any(line.startswith(key + '=') for key in keys)]
    lines += [f'{keys[0]}={proxy.hostname}', f'{keys[1]}={proxy.port or 80}',
              f'{keys[2]}={proxy.hostname}', f'{keys[3]}={proxy.port or 80}']
    properties.write_text('\n'.join(lines) + '\n')
    # The wrapper downloads before Gradle itself can read gradle.properties.
    env['JAVA_OPTS'] = env.get('JAVA_OPTS', '') + f' -Dhttps.proxyHost={proxy.hostname} -Dhttps.proxyPort={proxy.port or 80}'
args = sys.argv[1:] or ['build']
raise SystemExit(subprocess.run([str(ROOT / 'gradlew'), *args], cwd=ROOT, env=env).returncode)
