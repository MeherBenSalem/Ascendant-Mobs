#!/usr/bin/env python3
"""Sets up a version workspace for Remnants: RPG Mobs from a Mob Loot Bags workspace's build files and the 26.3
sources. Version-specific code changes are made afterwards by hand.

  python3 tools/port_setup.py 26.2 /home/user/Mob-Loot-Bags [--loader-dir forge]
"""
from __future__ import annotations

import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FIELDS = ['version', 'group', 'mod_name', 'mod_author', 'mod_id', 'license', 'credits', 'description']


def main():
    version, mlb = sys.argv[1], Path(sys.argv[2])
    rename = None
    if '--loader-dir' in sys.argv:
        rename = sys.argv[sys.argv.index('--loader-dir') + 1]
    src, dst = mlb / version, ROOT / version
    files = subprocess.run(['git', 'ls-files', '.'], cwd=src, capture_output=True, text=True, check=True).stdout.split()
    for rel in files:
        if re.match(r'(common|fabric|neoforge|forge)/src/', rel) or rel.endswith('README.md'):
            continue
        target = dst / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(src / rel, target)
        shutil.copymode(src / rel, target)
    reference = (ROOT / '26.3' / 'gradle.properties').read_text(encoding='utf-8')
    props = (dst / 'gradle.properties').read_text(encoding='utf-8')
    for field in FIELDS:
        value = re.search(rf'^{field}=(.*)$', reference, re.M).group(1)
        props = re.sub(rf'^{field}=.*$', lambda m: f'{field}={value}', props, flags=re.M)
    (dst / 'gradle.properties').write_text(props, encoding='utf-8')
    for build in dst.glob('*/build.gradle'):
        text = build.read_text(encoding='utf-8').replace('LootGoblin', 'RemnantHunter')
        build.write_text(text, encoding='utf-8')
    loader = 'neoforge'
    if rename:
        shutil.move(str(dst / 'neoforge'), str(dst / rename))
        settings = dst / 'settings.gradle'
        settings.write_text(settings.read_text(encoding='utf-8').replace("include('neoforge')", f"include('{rename}')"), encoding='utf-8')
        loader = rename
    for module, target in (('common', 'common'), ('fabric', 'fabric'), ('neoforge', loader)):
        source = ROOT / '26.3' / module / 'src'
        out = dst / target / 'src'
        if out.exists():
            shutil.rmtree(out)
        shutil.copytree(source, out)
    print(f'{version}: workspace ready at {dst}')


if __name__ == '__main__':
    main()
