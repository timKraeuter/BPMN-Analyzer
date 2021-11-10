<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 245 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>start</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>167 312 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>Token</string>
            </attr>
        </edge>
        <edge from="n0" to="n1">
            <attr name="label">
                <string>token</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>167 392 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>Token</string>
            </attr>
        </edge>
        <edge from="n0" to="n2">
            <attr name="label">
                <string>token</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>167 232 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>Token</string>
            </attr>
        </edge>
        <edge from="n0" to="n3">
            <attr name="label">
                <string>token</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>127 72 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>r1_preWork</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>297 72 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>r2_preWork</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>62 152 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>r1_postWork</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>247 152 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>r2_postWork</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>62 72 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>end</string>
            </attr>
        </edge>
    </graph>
</gxl>
