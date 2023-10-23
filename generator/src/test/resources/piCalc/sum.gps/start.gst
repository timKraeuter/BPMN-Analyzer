<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="sum" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 93 0 0</string>
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
                <string>477 93 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>262 93 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Coercion</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>707 93 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Sum</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>847 85 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>1088 85 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:In</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>1227 232 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>1227 72 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>1227 152 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>847 253 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:Summation</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>1077 253 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:Out</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>1227 392 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>1227 312 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <edge from="n2" to="n1">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
        <edge from="n1" to="n3">
            <attr name="label">
                <string>sum</string>
            </attr>
        </edge>
        <edge from="n4" to="n5">
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
        <edge from="n3" to="n4">
            <attr name="label">
                <string>arg1</string>
            </attr>
        </edge>
        <edge from="n9" to="n10">
            <attr name="label">
                <string>op</string>
            </attr>
        </edge>
        <edge from="n10" to="n6">
            <attr name="label">
                <string>channel</string>
            </attr>
        </edge>
        <edge from="n10" to="n11">
            <attr name="label">
                <string>payload</string>
            </attr>
        </edge>
        <edge from="n10" to="n12">
            <attr name="label">
                <string>process</string>
            </attr>
        </edge>
        <edge from="n3" to="n9">
            <attr name="label">
                <string>arg2</string>
            </attr>
        </edge>
        <edge from="n0" to="n2">
            <attr name="label">
                <string>c</string>
            </attr>
        </edge>
    </graph>
</gxl>
