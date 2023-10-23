<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Vars" role="rule" edgeids="false" edgemode="directed">
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
        <node id="n52">
            <attr name="layout">
                <string>382 72 0 0</string>
            </attr>
        </node>
        <edge from="n52" to="n52">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n0" to="n52">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n53">
            <attr name="layout">
                <string>382 152 0 0</string>
            </attr>
        </node>
        <edge from="n53" to="n53">
            <attr name="label">
                <string>string:"Vars"</string>
            </attr>
        </edge>
        <edge from="n0" to="n53">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 240 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n54">
            <attr name="layout">
                <string>277 232 0 0</string>
            </attr>
        </node>
        <edge from="n54" to="n54">
            <attr name="label">
                <string>string:"x"</string>
            </attr>
        </edge>
        <edge from="n1" to="n54">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>277 312 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n55">
            <attr name="layout">
                <string>552 312 0 0</string>
            </attr>
        </node>
        <edge from="n55" to="n55">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n2" to="n55">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>62 400 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n56">
            <attr name="layout">
                <string>277 472 0 0</string>
            </attr>
        </node>
        <edge from="n56" to="n56">
            <attr name="label">
                <string>string:"y"</string>
            </attr>
        </edge>
        <edge from="n3" to="n56">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>277 392 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n57">
            <attr name="layout">
                <string>552 392 0 0</string>
            </attr>
        </node>
        <edge from="n57" to="n57">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n4" to="n57">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>62 560 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n58">
            <attr name="layout">
                <string>277 632 0 0</string>
            </attr>
        </node>
        <edge from="n58" to="n58">
            <attr name="label">
                <string>string:"sum"</string>
            </attr>
        </edge>
        <edge from="n5" to="n58">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>277 552 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n59">
            <attr name="layout">
                <string>552 552 0 0</string>
            </attr>
        </node>
        <edge from="n59" to="n59">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n6" to="n59">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n3" to="n4">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n5" to="n6">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
    </graph>
</gxl>
