<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="ecmfa_usecase_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 85 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:ProcessSnapshot</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>392 152 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>string:"T-Junction Controller"</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>392 72 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Running</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>542 232 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>862 232 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>string:"controller_started_e1_66"</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>62 325 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:ProcessSnapshot</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>392 312 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>string:"Bus controller (B)"</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>392 392 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:Running</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>520 472 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>817 472 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>string:"Approaching_Junction_B_Request_TL_status_B_78"</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>62 565 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:ProcessSnapshot</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>392 712 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>string:"Bus controller (A)"</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>392 552 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:Running</string>
            </attr>
        </edge>
        <node id="n13">
            <attr name="layout">
                <string>520 632 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n14">
            <attr name="layout">
                <string>817 632 0 0</string>
            </attr>
        </node>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>string:"Approaching_Junction_A_Request_TL_status_A_88"</string>
            </attr>
        </edge>
        <edge from="n0" to="n1">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n0" to="n2">
            <attr name="label">
                <string>state</string>
            </attr>
        </edge>
        <edge from="n3" to="n4">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n0" to="n3">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
        <edge from="n5" to="n6">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n5" to="n7">
            <attr name="label">
                <string>state</string>
            </attr>
        </edge>
        <edge from="n8" to="n9">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n5" to="n8">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
        <edge from="n10" to="n11">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n10" to="n12">
            <attr name="label">
                <string>state</string>
            </attr>
        </edge>
        <edge from="n13" to="n14">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n10" to="n13">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
    </graph>
</gxl>
