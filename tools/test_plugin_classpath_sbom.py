import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("merge_plugin", Path(__file__).resolve().parents[1] / "scripts/merge-plugin-classpath-sbom.py")
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)

class PluginClasspathSBOMTest(unittest.TestCase):
    def fixture(self):
        agp = ["com.android.tools.build", "gradle", "9.5.2"]
        bc = ["org.bouncycastle", "bcprov-jdk18on", "1.85"]
        kgp = ["org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.4.21"]
        bom = {"bomFormat":"CycloneDX", "metadata":{"component":{"bom-ref":"root"}},
               "components":[{"group":bc[0],"name":bc[1],"version":bc[2],"bom-ref":"existing"}],
               "dependencies":[{"ref":"root","dependsOn":["existing"]}]}
        graph = {"schema":1,"roots":[agp],"components":[
            {"coordinate":agp,"dependencies":[bc,kgp]},{"coordinate":bc,"dependencies":[]},
            {"coordinate":kgp,"dependencies":[]}]}
        return bom, graph

    def test_merge_retains_existing_component_and_links_resolved_graph(self):
        bom, graph = self.fixture()
        result = mod.merge(bom, graph)
        self.assertEqual(3, len(result["components"]))
        agp = next(c["bom-ref"] for c in result["components"] if c["name"]=="gradle")
        edges = {e["ref"]:e["dependsOn"] for e in result["dependencies"]}
        self.assertEqual(["existing",agp], edges["root"])
        kgp = next(c["bom-ref"] for c in result["components"] if c["name"]=="kotlin-gradle-plugin")
        self.assertEqual(sorted(["existing",kgp]),edges[agp])

    def test_reject_old_plugin_version(self):
        bom, graph = self.fixture()
        old = ["org.bouncycastle","bcprov-jdk18on","1.80.2"]
        graph["components"][0]["dependencies"] = [old]
        graph["components"][1]["coordinate"] = old
        with self.assertRaisesRegex(ValueError, "unpatched"):
            mod.merge(bom,graph)

    def test_reject_new_high_severity_coordinates_in_either_scope(self):
        for coordinate in (
            ["org.jdom", "jdom2", "2.0.6"],
            ["org.bitbucket.b_c", "jose4j", "0.9.5"],
            ["com.fasterxml.jackson.core", "jackson-core", "2.22.2"],
            ["com.fasterxml.jackson.core", "jackson-databind", "2.22.2"],
        ):
            with self.subTest(coordinate=coordinate):
                bom, graph = self.fixture()
                graph["components"][0]["dependencies"].append(coordinate)
                graph["components"].append({"coordinate": coordinate, "dependencies": []})
                with self.assertRaisesRegex(ValueError, "unpatched"):
                    mod.merge(bom, graph)
                bom, graph = self.fixture()
                bom["components"].append({"group": coordinate[0], "name": coordinate[1],
                    "version": coordinate[2], "bom-ref": "old"})
                with self.assertRaisesRegex(ValueError, "unpatched"):
                    mod.merge(bom, graph)

    def test_reject_unpatched_kotlin_plugin(self):
        bom, graph = self.fixture()
        old = ["org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.2.10"]
        graph["components"][0]["dependencies"][1] = old
        graph["components"][2]["coordinate"] = old
        with self.assertRaisesRegex(ValueError, "patched Kotlin"):
            mod.merge(bom, graph)

    def test_reject_second_kotlin_plugin_version(self):
        bom, graph = self.fixture()
        bom["components"].append({"group":"org.jetbrains.kotlin", "name":"kotlin-gradle-plugin",
                                  "version":"2.2.10", "bom-ref":"old"})
        with self.assertRaisesRegex(ValueError, "Unexpected Kotlin"):
            mod.merge(bom, graph)

    def test_reject_missing_dependency(self):
        bom, graph = self.fixture()
        graph["components"].pop()
        with self.assertRaisesRegex(ValueError, "unresolved"):
            mod.merge(bom,graph)

if __name__ == "__main__":
    unittest.main()
