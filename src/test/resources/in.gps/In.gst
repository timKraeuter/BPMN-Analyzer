<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="in" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 82 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>flag:go</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 175 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Coercion</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>277 175 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>507 175 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:In</string>
            </attr>
        </edge>
        <edge from="n2" to="n3">
            <attr name="label">
                <string>op</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>642 242 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <edge from="n3" to="n4">
            <attr name="label">
                <string>channel</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>642 322 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <edge from="n3" to="n5">
            <attr name="label">
                <string>payload</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>642 162 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <edge from="n3" to="n6">
            <attr name="label">
                <string>process</string>
            </attr>
        </edge>
    </graph>
</gxl>
