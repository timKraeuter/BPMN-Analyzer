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
        <node id="n7">
            <attr name="layout">
                <string>382 72 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n0" to="n7">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>382 152 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>string:"Vars"</string>
            </attr>
        </edge>
        <edge from="n0" to="n8">
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
        <node id="n9">
            <attr name="layout">
                <string>277 232 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>string:"x"</string>
            </attr>
        </edge>
        <edge from="n1" to="n9">
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
        <node id="n10">
            <attr name="layout">
                <string>552 312 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n2" to="n10">
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
        <node id="n11">
            <attr name="layout">
                <string>277 472 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>string:"y"</string>
            </attr>
        </edge>
        <edge from="n3" to="n11">
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
        <node id="n12">
            <attr name="layout">
                <string>552 392 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n4" to="n12">
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
        <node id="n13">
            <attr name="layout">
                <string>277 632 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>string:"sum"</string>
            </attr>
        </edge>
        <edge from="n5" to="n13">
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
        <node id="n14">
            <attr name="layout">
                <string>552 552 0 0</string>
            </attr>
        </node>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n6" to="n14">
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
