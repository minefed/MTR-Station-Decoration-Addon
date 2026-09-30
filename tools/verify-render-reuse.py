#!/usr/bin/env python3
"""Compile the production text renderer/entity and exact production light-query body.

Only Minecraft/MTR boundary objects are fixtures. The original renderer is a frozen
baseline; candidate behavior always comes from the current production sources.
"""
import argparse
import os
from pathlib import Path
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "verification/render-reuse"


def light_method(source):
    signature = "    private static int[] getLights("
    assert source.count(signature) == 1, "Expected the production getLights method exactly once"
    start = source.index(signature)
    opening = source.index("{", start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end].replace("private static", "public static", 1)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--java-home", default=os.environ.get("JAVA_HOME"))
    parser.add_argument("--source-root", default=str(ROOT / "fabric/src/main/java"))
    parser.add_argument("--verify-forge-parity", action="store_true")
    args = parser.parse_args()
    java_home = Path(args.java_home) if args.java_home else None
    javac = str(java_home / "bin/javac") if java_home else shutil.which("javac")
    java = str(java_home / "bin/java") if java_home else shutil.which("java")
    if not javac or not java:
        raise SystemExit("A JDK with javac and java is required (set JAVA_HOME)")
    source_root = Path(args.source_root).resolve()
    if args.verify_forge_parity:
        for relative in ["top/mcmtr/mod/render/RenderCustomText.java",
                         "top/mcmtr/mod/blocks/BlockCustomTextBase.java",
                         "top/mcmtr/mixin/RenderRailsMixin.java"]:
            fabric = ROOT / "fabric/src/main/java" / relative
            forge = ROOT / "forge/src/main/java" / relative
            assert fabric.read_bytes() == forge.read_bytes(), "Fabric/Forge source mismatch: " + relative
        print("PASS: all three changed production sources have exact Fabric/Forge byte parity", flush=True)
    output = ROOT / "build/render-reuse-verification" / source_root.relative_to(ROOT).parts[0]
    generated = output / "generated"
    classes = output / "classes"
    generated.mkdir(parents=True, exist_ok=True)
    if classes.exists():
        shutil.rmtree(classes)
    classes.mkdir(parents=True, exist_ok=True)
    files = list((FIXTURES / "src").rglob("*.java"))
    files.append(FIXTURES / "baseline/BaselineRenderCustomText.java")
    files.extend(source_root / path for path in [
        "top/mcmtr/mod/render/RenderCustomText.java",
        "top/mcmtr/mod/blocks/BlockCustomTextBase.java",
    ])
    sources = {
        "BaselineLights": (FIXTURES / "baseline/getLights.java.txt").read_text(),
        "CandidateLights": (source_root / "top/mcmtr/mixin/RenderRailsMixin.java").read_text(),
    }
    for name, source in sources.items():
        path = generated / (name + ".java")
        path.write_text("package top.mcmtr.verification;\nimport org.mtr.mapping.holder.*;\n"
                        + "public final class " + name + " {\n" + light_method(source) + "\n}\n")
        files.append(path)
    # An argument file also works on Windows when the workspace has non-ASCII/spaced paths.
    arguments = output / "javac.args"
    arguments.write_text("\n".join('"' + str(path).replace('\\', '/') + '"' for path in files))
    subprocess.run([javac, "-encoding", "UTF-8", "--release", "17", "-d", str(classes), "@" + str(arguments)], check=True)
    subprocess.run([java, "-ea", "-XX:-OmitStackTraceInFastThrow", "-cp", str(classes), "top.mcmtr.verification.RenderReuseVerification"], check=True)


if __name__ == "__main__":
    main()
