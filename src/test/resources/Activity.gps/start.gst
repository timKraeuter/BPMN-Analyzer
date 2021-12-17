<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Activity_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>392 152 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:InitialNode</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 80 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:ActivityDiagram</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>392 72 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n1" to="n8">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>392 232 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>392 392 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>392 312 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:FinalNode</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>122 169 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>122 249 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>122 392 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <edge from="n1" to="n0">
            <attr name="label">
                <string>start</string>
            </attr>
        </edge>
        <edge from="n5" to="n0">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n5" to="n2">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n6" to="n2">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n6" to="n3">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n7" to="n3">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n7" to="n4">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
    </graph>
</gxl>
