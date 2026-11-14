#!/usr/bin/env python3
"""Reapply the Universal Maps rebrand after an upstream sync."""
from pathlib import Path
import os, shutil

SKIP_DIRS = {'.git', '3party', '.github', 'scripts'}

def newpath(rel):
    nr = rel.replace('/app/organicmaps', '/com/universalmaps/app')
    nr = nr.replace('OrganicMaps', 'UniversalMaps').replace('organicmaps', 'universalmaps').replace('organic_maps', 'universal_maps')
    return nr

def sweep():
    pairs = [
        ('https://organicmaps.app', 'https://github.com/yashhasan570/UniversalMaps'),
        ('app/organicmaps', 'com/universalmaps/app'),
        ('app.organicmaps', 'com.universalmaps.app'),
        ('ORGANICMAPS', 'UNIVERSALMAPS'),
        ('OrganicMaps', 'UniversalMaps'),
        ('Organic Maps', 'Universal Maps'),
        ('organicmaps.app', 'universalmaps.app'),
        ('organicMaps', 'universalMaps'),
        ('organicmaps', 'universalmaps'),
        ('organic_maps', 'universal_maps'),
        ('organicapp', 'universalapp'),
    ]
    binary_ext = {'.png','.jpg','.jpeg','.gif','.ico','.webp','.pdf','.zip','.mwm','.bin','.so','.a','.dll','.dylib','.kmz','.ttf','.otf','.woff','.woff2','.mp3','.ogg','.wav','.obj','.dat','.lst'}
    for root, dirs, files in os.walk('.'):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for f in files:
            p = Path(root) / f
            if p.suffix.lower() in binary_ext or p.name in ('migrate.py', 'README.md'): continue
            try: t = p.read_text(encoding='utf-8')
            except Exception: continue
            orig = t
            for old, new in pairs:
                t = t.replace(old, new)
            if t != orig: p.write_text(t)

def renames():
    targets = []
    for root, dirs, files in os.walk('.'):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for name in dirs + files:
            if 'organicmaps' in name.lower() or 'organic_maps' in name:
                targets.append(Path(root) / name)
    targets.sort(key=lambda p: len(p.parts), reverse=True)
    for p in targets:
        if not p.exists(): continue
        tgt = Path(newpath(str(p)))
        if p == tgt: continue
        if p.is_dir() and tgt.exists() and tgt.is_dir():
            for child in list(p.iterdir()):
                ct = Path(newpath(str(child)))
                ct.parent.mkdir(parents=True, exist_ok=True)
                if ct.exists():
                    if child.is_dir():
                        for sc in list(child.iterdir()):
                            s2 = Path(newpath(str(sc)))
                            s2.parent.mkdir(parents=True, exist_ok=True)
                            shutil.move(str(sc), str(s2))
                        child.rmdir()
                    else:
                        child.unlink()
                else:
                    shutil.move(str(child), str(ct))
            try: p.rmdir()
            except OSError: pass
        else:
            tgt.parent.mkdir(parents=True, exist_ok=True)
            if tgt.exists():
                if p.is_dir(): shutil.rmtree(p, ignore_errors=True)
                else: p.unlink()
            else:
                p.rename(tgt)

def cleanup():
    for item in ['AGENTS.md','CLAUDE.md','CONTRIBUTING.md','INSTALL.md','gcovr.cfg','docs','package.json',
                 'package-lock.json','.agents','.claude','.gemini','.vscode','.git-blame-ignore-revs',
                 'tools/python/borders/README.md','tools/python/maps_generator/README.md','tools/python/issues_db/README.md',
                 'android/sdk/README.md','.github/FUNDING.yml']:
        p = Path(item)
        if p.is_dir(): shutil.rmtree(p, ignore_errors=True)
        elif p.exists(): p.unlink()

def readme():
    Path('README.md').write_text("""# Universal Maps

Offline maps and navigation app with OpenStreetMap data, turn-by-turn routing, bookmarks, and offline search. No account or internet connection needed after the maps are downloaded.

## Build

```
./configure.sh
cmake --preset default && cmake --build --preset default
```

## Contact

Telegram: [@johnconstantine570](https://t.me/johnconstantine570)
""")

if __name__ == '__main__':
    sweep(); renames(); cleanup(); readme()
    print('rebrand reapplied')
