<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Vars_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>392 355 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:InitialNode</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 334 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:ActivityDiagram</string>
            </attr>
        </edge>
        <node id="n19">
            <attr name="layout">
                <string>392 195 0 0</string>
            </attr>
        </node>
        <edge from="n19" to="n19">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n1" to="n19">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n20">
            <attr name="layout">
                <string>392 275 0 0</string>
            </attr>
        </node>
        <edge from="n20" to="n20">
            <attr name="label">
                <string>string:"Vars"</string>
            </attr>
        </edge>
        <edge from="n1" to="n20">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>1147 464 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n21">
            <attr name="layout">
                <string>1392 472 0 0</string>
            </attr>
        </node>
        <edge from="n21" to="n21">
            <attr name="label">
                <string>string:"x"</string>
            </attr>
        </edge>
        <edge from="n2" to="n21">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>1392 392 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n22">
            <attr name="layout">
                <string>1667 392 0 0</string>
            </attr>
        </node>
        <edge from="n22" to="n22">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n3" to="n22">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>1147 304 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n23">
            <attr name="layout">
                <string>1392 312 0 0</string>
            </attr>
        </node>
        <edge from="n23" to="n23">
            <attr name="label">
                <string>string:"y"</string>
            </attr>
        </edge>
        <edge from="n4" to="n23">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>1392 232 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n24">
            <attr name="layout">
                <string>1667 232 0 0</string>
            </attr>
        </node>
        <edge from="n24" to="n24">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n5" to="n24">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>1157 624 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n25">
            <attr name="layout">
                <string>1392 552 0 0</string>
            </attr>
        </node>
        <edge from="n25" to="n25">
            <attr name="label">
                <string>string:"sum"</string>
            </attr>
        </edge>
        <edge from="n6" to="n25">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>1392 632 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n26">
            <attr name="layout">
                <string>1667 632 0 0</string>
            </attr>
        </node>
        <edge from="n26" to="n26">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n7" to="n26">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>392 435 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n27">
            <attr name="layout">
                <string>677 367 0 0</string>
            </attr>
        </node>
        <edge from="n27" to="n27">
            <attr name="label">
                <string>string:"Action1"</string>
            </attr>
        </edge>
        <edge from="n8" to="n27">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>677 447 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:SetVariableExpression</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>1117 152 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n28">
            <attr name="layout">
                <string>1392 152 0 0</string>
            </attr>
        </node>
        <edge from="n28" to="n28">
            <attr name="label">
                <string>int:5</string>
            </attr>
        </edge>
        <edge from="n10" to="n28">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>677 287 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:SetVariableExpression</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>1117 72 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n29">
            <attr name="layout">
                <string>1392 72 0 0</string>
            </attr>
        </node>
        <edge from="n29" to="n29">
            <attr name="label">
                <string>int:37</string>
            </attr>
        </edge>
        <edge from="n12" to="n29">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n13">
            <attr name="layout">
                <string>392 603 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n30">
            <attr name="layout">
                <string>677 531 0 0</string>
            </attr>
        </node>
        <edge from="n30" to="n30">
            <attr name="label">
                <string>string:"Action2"</string>
            </attr>
        </edge>
        <edge from="n13" to="n30">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n14">
            <attr name="layout">
                <string>880 611 0 0</string>
            </attr>
        </node>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:Sum</string>
            </attr>
        </edge>
        <node id="n15">
            <attr name="layout">
                <string>392 683 0 0</string>
            </attr>
        </node>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>type:FinalNode</string>
            </attr>
        </edge>
        <node id="n16">
            <attr name="layout">
                <string>122 418 0 0</string>
            </attr>
        </node>
        <edge from="n16" to="n16">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n17">
            <attr name="layout">
                <string>122 586 0 0</string>
            </attr>
        </node>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n18">
            <attr name="layout">
                <string>122 675 0 0</string>
            </attr>
        </node>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <edge from="n1" to="n0">
            <attr name="label">
                <string>start</string>
            </attr>
        </edge>
        <edge from="n2" to="n3">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n4" to="n5">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n6" to="n7">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n8" to="n9">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n9" to="n10">
            <attr name="label">
                <string>newValue</string>
            </attr>
        </edge>
        <edge from="n9" to="n2">
            <attr name="label">
                <string>var</string>
            </attr>
        </edge>
        <edge from="n8" to="n11">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n11" to="n12">
            <attr name="label">
                <string>newValue</string>
            </attr>
        </edge>
        <edge from="n11" to="n4">
            <attr name="label">
                <string>var</string>
            </attr>
        </edge>
        <edge from="n13" to="n14">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n14" to="n6">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n14" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n14" to="n4">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n16" to="n0">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n16" to="n8">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n17" to="n8">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n17" to="n13">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n18" to="n13">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n18" to="n15">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
    </graph>
</gxl>
