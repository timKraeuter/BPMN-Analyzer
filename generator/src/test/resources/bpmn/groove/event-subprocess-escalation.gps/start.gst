<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="event-subprocess-escalation_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 87 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:ProcessSnapshot</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>402 152 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>string:"Trigger escalation in event subprocess"</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>402 72 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Running</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>680 232 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>1127 232 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>string:"start_A_Flow_07yyyfd"</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>680 312 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>1127 312 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>string:"start_Gateway_02dcjtb_Flow_0txm8qa"</string>
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
                <string>position</string>
            </attr>
        </edge>
        <edge from="n0" to="n5">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
    </graph>
</gxl>
