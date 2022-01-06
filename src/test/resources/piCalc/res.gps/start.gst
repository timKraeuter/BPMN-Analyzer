<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="res_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 144 0 0</string>
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
                <string>262 144 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Restriction</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>522 72 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Name</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>522 152 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Process</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n1" to="n3">
            <attr name="label">
                <string>process</string>
            </attr>
        </edge>
        <edge from="n0" to="n1">
            <attr name="label">
                <string>res</string>
            </attr>
        </edge>
    </graph>
</gxl>
