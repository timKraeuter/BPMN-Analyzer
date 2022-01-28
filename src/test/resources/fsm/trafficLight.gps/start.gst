<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="trafficLight_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n1">
            <attr name="layout">
                <string>62 80 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:StateMachine</string>
            </attr>
        </edge>
        <node id="n0">
            <attr name="layout">
                <string>420 152 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:State</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>337 72 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>string:"trafficLight"</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>672 152 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>string:"red"</string>
            </attr>
        </edge>
        <edge from="n1" to="n0">
            <attr name="label">
                <string>currentState</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n0" to="n3">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
    </graph>
</gxl>
