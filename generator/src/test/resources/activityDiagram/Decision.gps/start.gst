<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Decision" role="rule" edgeids="false" edgemode="directed">
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
        <node id="n37">
            <attr name="layout">
                <string>382 72 0 0</string>
            </attr>
        </node>
        <edge from="n37" to="n37">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n0" to="n37">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n38">
            <attr name="layout">
                <string>382 152 0 0</string>
            </attr>
        </node>
        <edge from="n38" to="n38">
            <attr name="label">
                <string>string:"Decision"</string>
            </attr>
        </edge>
        <edge from="n0" to="n38">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
    </graph>
</gxl>
