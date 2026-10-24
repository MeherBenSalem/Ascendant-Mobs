#!/usr/bin/env python3
"""Runs the whole QA pass for one workspace and loader: dev server, QA client, server smoke test with the player,
client checks with screenshots, then stops both.

  python3 tools/qa/run_all.py 26.2 fabric [--ras] [--mc 26] [--keep]
Logs and screenshots go to $QA_LOGS (default /tmp/remnants-qa).
"""
from __future__ import annotations

import argparse
import os
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent.parent


def wait_for(path: Path, needle: str, seconds: int, fail=('BUILD FAILED',)) -> bool:
    deadline = time.time() + seconds
    while time.time() < deadline:
        if path.exists():
            text = path.read_text(errors='replace')
            if needle in text:
                return True
            if any(f in text for f in fail):
                return False
        time.sleep(3)
    return False


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('workspace')
    ap.add_argument('loader')
    ap.add_argument('--ras', action='store_true')
    ap.add_argument('--mc', default=None)
    ap.add_argument('--keep', action='store_true', help='leave server and client running')
    args = ap.parse_args()
    logs = Path(os.environ.get('QA_LOGS', '/tmp/remnants-qa')) / f'{args.workspace}-{args.loader}{"-ras" if args.ras else ""}'
    logs.mkdir(parents=True, exist_ok=True)
    env = dict(os.environ, QA_LOGS=str(logs))
    devrun = str(HERE / 'devrun.sh')
    mc = args.mc or ('26' if args.workspace.startswith('26') else args.workspace)
    subprocess.run([devrun, 'stop-client'], env=env)
    subprocess.run([devrun, 'stop-server'], env=env)
    time.sleep(5)
    for name in ('server', 'client'):
        log = logs / f'{args.loader}-{name}.log'
        if log.exists():
            log.unlink()
    subprocess.run([devrun, 'start-server', args.workspace, args.loader], env=env)
    if not wait_for(logs / f'{args.loader}-server.log', 'RCON running', 900):
        print('server did not start; see', logs / f'{args.loader}-server.log')
        sys.exit(2)
    subprocess.run([devrun, 'start-client', args.workspace, args.loader], env=env)
    if not wait_for(logs / f'{args.loader}-server.log', 'RemnantHunter joined', 900):
        print('client did not join; see', logs / f'{args.loader}-client.log')
        sys.exit(2)
    time.sleep(5)
    run_dir = ROOT / args.workspace / args.loader / 'runs' / 'server'
    smoke = [sys.executable, str(HERE / 'server_smoke.py'), '--run-dir', str(run_dir), '--with-player', '--mc', mc]
    checks = [sys.executable, str(HERE / 'client_checks.py'), '--out', str(logs / 'shots'), '--mc', mc]
    if args.ras:
        smoke.append('--ras')
        checks.append('--ras')
    code = subprocess.run(smoke).returncode
    code |= subprocess.run(checks).returncode
    if not args.keep:
        subprocess.run([devrun, 'stop-client'], env=env)
        subprocess.run([devrun, 'stop-server'], env=env)
    print(f'QA {args.workspace} {args.loader}{" + RAS" if args.ras else ""}: {"PASS" if code == 0 else "FAIL"}  (logs: {logs})')
    sys.exit(code)


if __name__ == '__main__':
    main()
