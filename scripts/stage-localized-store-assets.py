#!/usr/bin/env python3
"""Validate real native captures; stage exact PNG bytes without resampling."""
import argparse
from pathlib import Path
import shutil
import struct
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
ANDROID="{http://schemas.android.com/apk/res/android}"
LOCALES={"en":"en-US","hr":"hr-HR","de":"de-DE","fr":"fr-FR",
         "es":"es-ES","ar":"ar","he":"iw-IL","it":"it-IT","pt-BR":"pt-BR","sl":"sl","pl":"pl-PL","cs":"cs-CZ","ja":"ja-JP","ko":"ko-KR","zh-Hans":"zh-CN","sk":"sk","nl":"nl-NL","uk":"uk","sv":"sv-SE","da":"da-DK","nb":"no-NO","fi":"fi-FI","hu":"hu-HU","ro":"ro","bg":"bg","el":"el-GR","tr":"tr-TR","hi":"hi-IN","id":"id","vi":"vi"}
PAGES=("splash","home","game","pause","win","over","confirm","confirmEnd",
       "dailyWin","dailyFail","daily","missions","stats","historyDetail",
       "themes","settings","language","backup","help","privacy","about","gameRelaxed","gameDaily")
STORE_PAGES=("home","game","daily","missions","stats","themes","settings","language")
SIZES=("320x480","360x800","941x1672","1080x2400")

def validate_metadata(root=ROOT):
    config=ET.parse(root/"app/src/main/res/xml/locales_config.xml").getroot()
    tags={item.get(ANDROID+"name") for item in config.findall("locale")}
    assert tags==set(LOCALES), "Update Fastlane locale mapping alongside the picker"
    metadata=root/"fastlane/metadata/android"
    for tag,folder in LOCALES.items():
        for name,limit in (("title",30),("short_description",80),("full_description",4000)):
            path=metadata/folder/(name+".txt")
            value=path.read_text(encoding="utf-8").strip()
            assert 0<len(value)<=limit, f"{tag}: invalid {name} length"
            assert not any(word in value.lower() for word in ("todo:","lorem ipsum","development build")), f"{tag}: placeholder copy"
    print(f"Validated {len(LOCALES)} complete store metadata inventories.")

def png_dimensions(path):
    with path.open("rb") as file:
        header=file.read(24)
    assert len(header)==24 and header[:8]==b"\x89PNG\r\n\x1a\n" and header[12:16]==b"IHDR", f"Invalid PNG: {path}"
    return struct.unpack(">II",header[16:24])

def validate_capture(path,dimensions):
    assert path.is_file() and path.stat().st_size>1000, f"Missing or empty capture: {path}"
    assert png_dimensions(path)==dimensions, f"Wrong dimensions: {path}"

def stage(captures,output,full=False,root=ROOT,offset=0,count=30):
    validate_metadata(root)
    # The ADB destination varies depending on whether it already exists.
    if (captures/"locale-screenshots").is_dir():
        captures=captures/"locale-screenshots"
    sizes=SIZES if full else ("941x1672",)
    config=ET.parse(root/"app/src/main/res/xml/locales_config.xml").getroot()
    order=[item.get(ANDROID+"name") for item in config.findall("locale")]
    assert 0<=offset<len(order) and count>0
    selected=order[offset:offset+count]
    for tag in selected:
        folder=LOCALES[tag]
        for size in sizes:
            dims=tuple(map(int,size.split("x")))
            for page in PAGES:
                validate_capture(captures/tag/size/(page+".png"),dims)
        destination=output/"fastlane/metadata/android"/folder
        destination.mkdir(parents=True,exist_ok=True)
        for name in ("title","short_description","full_description"):
            shutil.copyfile(root/"fastlane/metadata/android"/folder/(name+".txt"),destination/(name+".txt"))
        screenshots=destination/"images/phoneScreenshots"
        screenshots.mkdir(parents=True,exist_ok=True)
        for index,page in enumerate(STORE_PAGES,1):
            source=captures/tag/"941x1672"/(page+".png")
            target=screenshots/f"{index:02d}-{page}.png"
            shutil.copyfile(source,target)
            assert source.read_bytes()==target.read_bytes()
            width,height=png_dimensions(target)
            assert 320<=min(width,height)<=max(width,height)<=3840
            assert max(width,height)<=2*min(width,height)
    print(f"Verified {len(selected)*len(sizes)*len(PAGES)} native captures; staged {len(selected)*len(STORE_PAGES)} unmodified Play-size screenshots.")

if __name__=="__main__":
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--metadata-only",action="store_true")
    parser.add_argument("--captures",type=Path,default=Path("locale-screenshots"))
    parser.add_argument("--output",type=Path,default=Path("localized-fastlane"))
    parser.add_argument("--full-viewports",choices=("true","false"),default="false")
    parser.add_argument("--locale-offset",type=int,default=0)
    parser.add_argument("--locale-count",type=int,default=30)
    args=parser.parse_args()
    if args.metadata_only:
        validate_metadata()
    else:
        stage(args.captures,args.output,args.full_viewports=="true",offset=args.locale_offset,count=args.locale_count)
