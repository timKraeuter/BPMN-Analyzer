<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Fork_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>462 712 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:InitialNode</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 860 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:ActivityDiagram</string>
            </attr>
        </edge>
        <node id="n17">
            <attr name="layout">
                <string>462 792 0 0</string>
            </attr>
        </node>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n1" to="n17">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n18">
            <attr name="layout">
                <string>462 872 0 0</string>
            </attr>
        </node>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>string:"Fork"</string>
            </attr>
        </edge>
        <edge from="n1" to="n18">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>462 392 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n19">
            <attr name="layout">
                <string>737 392 0 0</string>
            </attr>
        </node>
        <edge from="n19" to="n19">
            <attr name="label">
                <string>string:"Action1"</string>
            </attr>
        </edge>
        <edge from="n2" to="n19">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>462 624 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:ForkNode</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>462 312 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n20">
            <attr name="layout">
                <string>737 312 0 0</string>
            </attr>
        </node>
        <edge from="n20" to="n20">
            <attr name="label">
                <string>string:"Action1_1"</string>
            </attr>
        </edge>
        <edge from="n4" to="n20">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>462 472 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n21">
            <attr name="layout">
                <string>737 472 0 0</string>
            </attr>
        </node>
        <edge from="n21" to="n21">
            <attr name="label">
                <string>string:"Action1_2"</string>
            </attr>
        </edge>
        <edge from="n5" to="n21">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>462 232 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:JoinNode</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>462 152 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n22">
            <attr name="layout">
                <string>737 152 0 0</string>
            </attr>
        </node>
        <edge from="n22" to="n22">
            <attr name="label">
                <string>string:"Action2"</string>
            </attr>
        </edge>
        <edge from="n7" to="n22">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>462 72 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:FinalNode</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>122 695 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>122 615 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>122 535 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>122 775 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n13">
            <attr name="layout">
                <string>122 295 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n14">
            <attr name="layout">
                <string>122 455 0 0</string>
            </attr>
        </node>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n15">
            <attr name="layout">
                <string>122 169 0 0</string>
            </attr>
        </node>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n16">
            <attr name="layout">
                <string>122 80 0 0</string>
            </attr>
        </node>
        <edge from="n16" to="n16">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <edge from="n1" to="n0">
            <attr name="label">
                <string>start</string>
            </attr>
        </edge>
        <edge from="n9" to="n0">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n9" to="n2">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n10" to="n2">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n10" to="n3">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n11" to="n3">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n11" to="n4">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n12" to="n3">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n12" to="n5">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n13" to="n4">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n13" to="n6">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n14" to="n5">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n14" to="n6">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n15" to="n6">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n15" to="n7">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n16" to="n7">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n16" to="n8">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
    </graph>
</gxl>
