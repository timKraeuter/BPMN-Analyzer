<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Activity" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 80 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:ActivityDiagram</string>
            </attr>
        </edge>
        <node id="n22">
            <attr name="layout">
                <string>382 72 0 0</string>
            </attr>
        </node>
        <edge from="n22" to="n22">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n0" to="n22">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n23">
            <attr name="layout">
                <string>382 152 0 0</string>
            </attr>
        </node>
        <edge from="n23" to="n23">
            <attr name="label">
                <string>string:"Activity"</string>
            </attr>
        </edge>
        <edge from="n0" to="n23">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
    </graph>
</gxl>
