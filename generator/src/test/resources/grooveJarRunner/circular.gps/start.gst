<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph role="graph" edgeids="false" edgemode="directed" id="start">
        <attr name="$version">
            <string>curly</string>
        </attr>
        <node id="n0">
            <attr name="layout">
                <string>334 323 34 18</string>
            </attr>
        </node>
        <node id="n1">
            <attr name="layout">
                <string>329 197 36 36</string>
            </attr>
        </node>
        <node id="n2">
            <attr name="layout">
                <string>517 316 36 36</string>
            </attr>
        </node>
        <node id="n3">
            <attr name="layout">
                <string>327 469 36 36</string>
            </attr>
        </node>
        <node id="n4">
            <attr name="layout">
                <string>177 330 36 36</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>Buffer</string>
            </attr>
        </edge>
        <edge from="n0" to="n1">
            <attr name="label">
                <string>first</string>
            </attr>
        </edge>
        <edge from="n0" to="n4">
            <attr name="label">
                <string>last</string>
            </attr>
        </edge>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>Cell</string>
            </attr>
        </edge>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>empty</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>next</string>
            </attr>
        </edge>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>Cell</string>
            </attr>
        </edge>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>empty</string>
            </attr>
        </edge>
        <edge from="n2" to="n3">
            <attr name="label">
                <string>next</string>
            </attr>
        </edge>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>Cell</string>
            </attr>
        </edge>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>empty</string>
            </attr>
        </edge>
        <edge from="n3" to="n4">
            <attr name="label">
                <string>next</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>Cell</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>empty</string>
            </attr>
        </edge>
        <edge from="n4" to="n1">
            <attr name="label">
                <string>next</string>
            </attr>
        </edge>
    </graph>
</gxl>
