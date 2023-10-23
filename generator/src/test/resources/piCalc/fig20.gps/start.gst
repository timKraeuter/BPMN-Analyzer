<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="fig20" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 300 0 0</string>
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
                <string>262 300 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Par</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>402 308 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>817 308 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>602 308 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:Coercion</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>1058 308 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:In</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>1197 152 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>1982 312 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>1197 232 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>1612 232 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>1397 232 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:Coercion</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>1842 232 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:Out</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>1982 152 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n13">
            <attr name="layout">
                <string>1982 232 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <node id="n14">
            <attr name="layout">
                <string>402 140 0 0</string>
            </attr>
        </node>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <node id="n15">
            <attr name="layout">
                <string>817 140 0 0</string>
            </attr>
        </node>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <node id="n16">
            <attr name="layout">
                <string>602 140 0 0</string>
            </attr>
        </node>
        <edge from="n16" to="n16">
            <attr name="label">
                <string>type:Coercion</string>
            </attr>
        </edge>
        <node id="n17">
            <attr name="layout">
                <string>1047 140 0 0</string>
            </attr>
        </node>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>type:Out</string>
            </attr>
        </edge>
        <node id="n18">
            <attr name="layout">
                <string>1197 72 0 0</string>
            </attr>
        </node>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>arg1</string>
            </attr>
        </edge>
        <edge from="n4" to="n3">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n3" to="n5">
            <attr name="label">
                <string>op</string>
            </attr>
        </edge>
        <edge from="n5" to="n6">
            <attr name="label">
                <string>channel</string>
            </attr>
        </edge>
        <edge from="n5" to="n7">
            <attr name="label">
                <string>payload</string>
            </attr>
        </edge>
        <edge from="n5" to="n8">
            <attr name="label">
                <string>process</string>
            </attr>
        </edge>
        <edge from="n10" to="n9">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n9" to="n11">
            <attr name="label">
                <string>op</string>
            </attr>
        </edge>
        <edge from="n11" to="n7">
            <attr name="label">
                <string>channel</string>
            </attr>
        </edge>
        <edge from="n11" to="n12">
            <attr name="label">
                <string>payload</string>
            </attr>
        </edge>
        <edge from="n11" to="n13">
            <attr name="label">
                <string>process</string>
            </attr>
        </edge>
        <edge from="n8" to="n10">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n2" to="n4">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n1" to="n14">
            <attr name="label">
                <string>arg2</string>
            </attr>
        </edge>
        <edge from="n16" to="n15">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n15" to="n17">
            <attr name="label">
                <string>op</string>
            </attr>
        </edge>
        <edge from="n17" to="n6">
            <attr name="label">
                <string>channel</string>
            </attr>
        </edge>
        <edge from="n17" to="n6">
            <attr name="label">
                <string>payload</string>
            </attr>
        </edge>
        <edge from="n17" to="n18">
            <attr name="label">
                <string>process</string>
            </attr>
        </edge>
        <edge from="n14" to="n16">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n0" to="n1">
            <attr name="label">
                <string>par</string>
            </attr>
        </edge>
    </graph>
</gxl>
