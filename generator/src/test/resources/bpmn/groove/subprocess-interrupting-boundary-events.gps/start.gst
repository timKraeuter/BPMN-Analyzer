<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="subprocess-interrupting-boundary-events_start" role="rule" edgeids="false" edgemode="directed">
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
                <string>string:"p1"</string>
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
                <string>407 232 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>592 232 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>string:"start -&gt; Subprocess1"</string>
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
                <string>string:"interruptTrigger"</string>
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
                <string>505 472 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>787 472 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>string:"start_trigger -&gt; interruptMessage"</string>
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
    </graph>
</gxl>
