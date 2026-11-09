package depgraph;

import java.util.List;

/** Checks the independently implemented metadata parsers. */
public final class MetadataTests {
    /** Prevents utility construction. */
    private MetadataTests() { }

    /** Runs focused checks for the JSON, version and marker parsers. */
    public static void run() {
        testJsonEscapes();
        testVersionConstraints();
        testEnvironmentMarkers();
        testRequirementParsing();
    }

    /** Verifies JSON escape decoding and numeric parsing. */
    private static void testJsonEscapes() {
        var document = Json.object(Json.parse(
                "{\"items\":[\"a\\n\\u0042\",true,null],\"v\":1.25}"));
        assert Json.array(document.get("items")).get(0).equals("a\nB");
    }

    /** Verifies version ordering and supported constraint operators. */
    private static void testVersionConstraints() {
        assert Versions.compare("1.10", "1.9") > 0;
        assert Versions.compare("1.0", "1") == 0;
        for (String condition : List.of(">=1,<3", "~=2.1", "==2.*", "!=1.9", "<=2.2", "===2.2")) {
            assert Versions.matches("2.2", condition) : condition;
        }
        assert !Versions.matches("3.0", "~=2.1");
        assert !Versions.matches("2.2", "!=2.*");
    }

    /** Verifies environment marker evaluation and extra filtering. */
    private static void testEnvironmentMarkers() {
        assert Markers.applies("python_version >= '3.12' and sys_platform == 'win32'");
        assert Markers.applies("extra == 'none' or (python_version >= '3' and os_name == 'nt')");
        assert !Markers.applies("python_version < '3.11'");
        assert !Markers.applies("extra == 'security'");
    }

    /** Verifies normalization and filtering of dependency declarations. */
    private static void testRequirementParsing() {
        assert Requirement.parse("PySocks; extra == 'socks'") == null;
        assert Requirement.parse("charset_normalizer (<4,>=2)").name().equals("charset-normalizer");
    }

}
