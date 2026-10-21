#!/usr/bin/env python3
"""Package an already tested build without including local worlds, caches or credentials."""
from pathlib import Path
import hashlib,re,shutil,struct,zipfile
ROOT=Path(__file__).resolve().parents[1]
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M)[1].strip()
jar=ROOT/f'build/libs/tnd-{version}.jar'
out=Path('/workspace/shared');out.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(jar) as archive:
    assert f'version="{version}"' in archive.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
    textures=[n for n in archive.namelist() if n.endswith('.png') and n.startswith('assets/tnd/textures/')]
    assert len(textures)>=42
    for name in textures:assert struct.unpack('>II',archive.read(name)[16:24])==(16,16),name
    assert archive.testzip() is None
shutil.copy2(jar,out/jar.name)
files=[]
for name in ['src','scripts','docs','gradle','.github']:
    directory=ROOT/name
    if directory.exists():files.extend(p for p in directory.rglob('*') if p.is_file() and '__pycache__' not in p.parts and p.suffix!='.pyc')
for name in ['README.md','LICENSE','TEMPLATE_LICENSE.txt','build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore']:
    if (ROOT/name).is_file():files.append(ROOT/name)
zip_path=out/f'TheNewDimension-{version}.zip'
with zipfile.ZipFile(zip_path,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
    def put(name,data,executable=False):
        info=zipfile.ZipInfo(name,date_time=(2026,10,21,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED
        info.external_attr=(0o100755 if executable else 0o100644)<<16
        archive.writestr(info,data)
    put('mods/'+jar.name,jar.read_bytes())
    put('LISEZ-MOI.txt',f'Minecraft Java 26.2 / NeoForge 26.2.0.93 / Java 25\n\nCopier mods/{jar.name} dans le dossier mods de votre instance Minecraft.\nRetirer les anciennes versions TND.\nLes nouvelles structures et les nouveaux minerais apparaissent dans les nouveaux chunks.\nLe dossier source contient le projet et son compte rendu de validation.\n'.encode())
    for file in sorted(files):put('source/'+str(file.relative_to(ROOT)),file.read_bytes(),file.name=='gradlew' or file.suffix=='.sh')
checks=''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in [out/jar.name,zip_path])
(out/f'SHA256SUMS-{version}').write_text(checks)
print(checks,end='')
